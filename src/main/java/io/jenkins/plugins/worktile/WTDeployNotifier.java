package io.jenkins.plugins.worktile;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.service.WTRestSession;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;

public class WTDeployNotifier extends Notifier implements SimpleBuildStep {
  public static final Logger logger = Logger.getLogger(WTDeployNotifier.class.getName());

  private String environment;

  private String releaseName;

  private String releaseUrl;

  @DataBoundConstructor
  public WTDeployNotifier(
      final String environment, final String releaseName, final String releaseUrl) {
    setReleaseName(releaseName);
    setReleaseUrl(releaseUrl);
    setEnvironment(environment);
  }

  public String getEnvironment() {
    return environment;
  }

  @DataBoundSetter
  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getReleaseName() {
    return releaseName;
  }

  @DataBoundSetter
  public void setReleaseName(final String releaseName) {
    this.releaseName = releaseName;
  }

  @Override
  public void perform(
      @NotNull Run<?, ?> run,
      @NotNull FilePath workspace,
      @NotNull Launcher launcher,
      @NotNull TaskListener listener) {

    WTDeployEntity entity = new WTDeployEntity();
    String status = WTHelper.statusOfRun(run);

    entity.status =
        status.equals("success")
            ? WTDeployEntity.Status.Deployed.getDeploy()
            : WTDeployEntity.Status.NotDeployed.getDeploy();

    String releaseUrl = this.getReleaseUrl();
    try {
      EnvVars envVars = run.getEnvironment(TaskListener.NULL);
      entity.releaseName = envVars.expand(run.getFullDisplayName());
      entity.releaseUrl = releaseUrl != null ? envVars.expand(releaseUrl) : null;
    } catch (Exception e) {
      entity.releaseName = run.getFullDisplayName();
      entity.releaseUrl = releaseUrl;
    }

    WTRestSession session = new WTRestSession();
    try {
      session.createDeploy(entity);
    } catch (Exception error) {
      listener.getLogger().println("create deploy failure " + error.getMessage());
    }
  }

  public String getReleaseUrl() {
    return releaseUrl;
  }

  @DataBoundSetter
  public void setReleaseUrl(final String releaseUrl) {
    this.releaseUrl = releaseUrl;
  }

  @Extension
  public static final class Descriptor extends BuildStepDescriptor<Publisher> {
    public Descriptor() {
      load();
    }

    @Override
    public boolean isApplicable(@NotNull final Class<? extends AbstractProject> item) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Worktile deploy notifier";
    }

    @Override
    public WTDeployNotifier newInstance(
        final StaplerRequest request, @NotNull final JSONObject formData) {
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
        return FormValidation.error("release name can not be empty");
      }
      return FormValidation.ok();
    }

    public ListBoxModel doFillEnvironmentItems() {
      final ListBoxModel items = new ListBoxModel();
      WTRestSession session = new WTRestSession();
      try {
        WTPaginationResponse<WTEnvironmentSchema> schemas = session.listEnvironments();
        for (WTEnvironmentSchema schema : schemas.values) {
          items.add(schema.name, schema.id);
        }
      } catch (Exception exception) {
        WTDeployNotifier.logger.info("get environments error " + exception.getMessage());
      }
      return items;
    }
  }
}
