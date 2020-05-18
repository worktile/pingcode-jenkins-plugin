package io.jenkins.plugins.worktile;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestService;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;

public class WTBuildNotifier extends Notifier {

  private String overview;

  @DataBoundConstructor
  public WTBuildNotifier(String overview) {
    setOverview(overview);
  }

  @Override
  public boolean perform(
      @Nonnull AbstractBuild<?, ?> run, @Nonnull Launcher launcher, @NotNull BuildListener listener)
      throws IOException, InternalError {
    this.createBuild(run, listener);
    return true;
  }

  private void createBuild(@Nonnull AbstractBuild<?, ?> run, @Nonnull TaskListener listener)
      throws IOException {
    String status = WTHelper.statusOfRun(run);
    WTBuildEntity entity = new WTBuildEntity();
    entity.name = run.getFullDisplayName();
    entity.identifier = run.getId();
    entity.jobUrl = run.getAbsoluteUrl();
    entity.resultUrl = run.getAbsoluteUrl() + "console";
    entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
    entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
    entity.duration = run.getDuration();
    entity.status =
        status.equals("success")
            ? WTBuildEntity.Status.Success.getValue()
            : WTBuildEntity.Status.Failure.getValue();
    entity.resultOverview = WTHelper.resolveOverview(run, getOverview());

    try {
      EnvVars vars = run.getEnvironment(TaskListener.NULL);
      entity.workItemIdentifiers =
          WTHelper.extractWorkItemsFromSCM(run, vars).toArray(new String[0]);
    } catch (Exception exception) {
      // do nothing
    }

    WTRestService service = new WTRestService();
    try {
      service.createBuild(entity);
      listener.getLogger().println("Send build data to worktile open api");
    } catch (Exception error) {
      listener.getLogger().println("Create worktile build error " + error.getMessage());
    }
  }

  public String getOverview() {
    return overview;
  }

  @DataBoundSetter
  public void setOverview(String overview) {
    this.overview = overview;
  }

  @Override
  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  @Extension
  public static class Descriptor extends BuildStepDescriptor<Publisher> {
    public Descriptor() {
      super(WTBuildNotifier.class);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @NotNull
    @Override
    public String getDisplayName() {
      return "Worktile build notifier";
    }

    @Override
    public WTBuildNotifier newInstance(
        @NotNull StaplerRequest request, @NotNull JSONObject formData) throws FormException {
      return request.bindJSON(WTBuildNotifier.class, formData);
    }
  }
}
