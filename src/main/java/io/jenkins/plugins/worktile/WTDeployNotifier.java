package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.service.WTRestService;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.logging.Logger;

public class WTDeployNotifier extends Notifier implements SimpleBuildStep {
  private static final Logger logger = Logger.getLogger(WTDeployNotifier.class.getName());

  private String environmentName;

  private String releaseName;

  private String releaseUrl;

  @DataBoundConstructor
  public WTDeployNotifier(final String releaseName, final String environmentName, final String releaseUrl) {
    setReleaseName(releaseName);
    setReleaseUrl(releaseUrl);
    setEnvironmentName(environmentName);
  }

  public String handleEnvName(String name, WTRestService service) throws IOException, WTRestException {
    WTEnvironmentSchema schema = service.getEnvironmentByName(name);
    if (schema == null) {
      schema = service.createEnvironment(new WTEnvironmentEntity(name));
    }
    return schema.id;
  }

  @Override
  public void perform(@NotNull Run<?, ?> run, @NotNull FilePath workspace, @NotNull Launcher launcher,
      @NotNull TaskListener listener) {
    WTLogger wtLogger = new WTLogger(listener);

    WTRestService service = new WTRestService();
    String envId = null;
    try {
      envId = handleEnvName(this.environmentName, service);
    } catch (Exception exception) {
      wtLogger.error(exception.getMessage());
      if (exception instanceof WTRestException) {
        if (!((WTRestException) exception).getCode().equals("100105")) {
          wtLogger.error(exception.getMessage());
        }
      }
    }
    WTDeployEntity entity = WTDeployEntity.from(run, getReleaseName(), getReleaseUrl(), envId);
    wtLogger.info("Will send data to worktile: " + entity.toString());
    try {
      service.createDeploy(entity);
      wtLogger.info("Create worktile deploy record successfully.");
    } catch (Exception error) {
      wtLogger.error(error.getMessage());
    }
  }

  public String getReleaseName() {
    return releaseName;
  }

  @DataBoundSetter
  public void setReleaseName(final String releaseName) {
    this.releaseName = Util.fixEmptyAndTrim(releaseName);
  }

  public String getReleaseUrl() {
    return releaseUrl;
  }

  public String getEnvironmentName() {
    return environmentName;
  }

  @DataBoundSetter
  public void setEnvironmentName(String environment) {
    this.environmentName = Util.fixEmptyAndTrim(environment);
  }

  @DataBoundSetter
  public void setReleaseUrl(final String releaseUrl) {
    this.releaseUrl = Util.fixEmptyAndTrim(releaseUrl);
  }

  @Extension
  public static final class Descriptor extends BuildStepDescriptor<Publisher> {
    public Descriptor() {
      load();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isApplicable(@NotNull final Class<? extends AbstractProject> item) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return Messages.WTDeployNotifier_DisplayName();
    }

    @Override
    public WTDeployNotifier newInstance(final StaplerRequest request, @NotNull final JSONObject formData) {
      assert request != null;
      return request.bindJSON(WTDeployNotifier.class, formData);
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
      save();
      return super.configure(request, formData);
    }

    public FormValidation doCheckReleaseName(
        @QueryParameter(value = "releaseName", fixEmpty = true) String releaseName) {
      if (WTHelper.isBlank(releaseName)) {
        return FormValidation.error(Messages.WTDeployNotifier_RelaseNameEmpty());
      }
      return FormValidation.ok();
    }

    public FormValidation doCheckEnvironmentName(
        @QueryParameter(value = "environmentName", fixEmpty = true) String environmentName) {
      if (WTHelper.isBlank(environmentName)) {
        return FormValidation.error(Messages.WTDeployNotifier_EnvironmentEmpty());
      }
      return FormValidation.ok();
    }
  }
}
