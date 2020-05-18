package io.jenkins.plugins.worktile.pipeline;

import com.google.common.collect.ImmutableSet;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.service.WTRestService;
import org.antlr.v4.runtime.misc.NotNull;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class WTSendDeployStep extends Step implements Serializable {
  private static final long serialVersionUID = 1L;

  private String releaseName;
  private String environmentName;
  private String releaseURL;
  private String buildResult;

  private boolean failOnError;

  @DataBoundConstructor
  public WTSendDeployStep(
      @NotNull String releaseName, @NotNull String environmentName, String releaseURL) {
    setReleaseName(releaseName);
    setEnvironmentName(environmentName);
    setReleaseURL(releaseURL);
  }

  public String getBuildResult() {
    return buildResult;
  }

  @DataBoundSetter
  public void setBuildResult(@NotNull String buildResult) {
    this.buildResult = buildResult;
  }

  public boolean isFailOnError() {
    return failOnError;
  }

  @DataBoundSetter
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  public String getReleaseURL() {
    return releaseURL;
  }

  @DataBoundSetter
  public void setReleaseURL(String releaseURL) {
    this.releaseURL = releaseURL;
  }

  public String getEnvironmentName() {
    return environmentName;
  }

  @DataBoundSetter
  public void setEnvironmentName(@NotNull String environmentName) {
    this.environmentName = environmentName;
  }

  public String getReleaseName() {
    return releaseName;
  }

  @DataBoundSetter
  public void setReleaseName(String releaseName) {
    this.releaseName = releaseName;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new WTSendDeployStepExecution(context, this);
  }

  public static class WTSendDeployStepExecution
      extends SynchronousNonBlockingStepExecution<Boolean> {
    private static final long serialVersionUID = 1L;

    private final WTSendDeployStep step;

    public WTSendDeployStepExecution(StepContext context, WTSendDeployStep step) {
      super(context);

      this.step = step;
    }

    @Override
    public Boolean run() throws Exception {
      WorkflowRun run = getContext().get(WorkflowRun.class);
      assert run != null;
      TaskListener listener = getContext().get(TaskListener.class);
      WTLogger log = new WTLogger(listener);

      WTDeployEntity entity = new WTDeployEntity();

      String status = WTHelper.statusOfRun(run);
      entity.status =
          status.equals("success")
              ? WTDeployEntity.Status.Deployed.getDeploy()
              : WTDeployEntity.Status.NotDeployed.getDeploy();

      String releaseUrl = this.step.getReleaseURL();
      try {
        EnvVars envVars = run.getEnvironment(TaskListener.NULL);
        entity.releaseName = envVars.expand(run.getFullDisplayName());
        entity.releaseUrl = releaseUrl != null ? envVars.expand(releaseUrl) : null;
      } catch (Exception e) {
        entity.releaseName = run.getFullDisplayName();
        entity.releaseUrl = releaseUrl;
      }

      String envId = null;
      try {
        envId = handleEnvName(this.step.environmentName);
      } catch (Exception exception) {
        log.error(
            String.format(
                "Create environment(%s) error, message %s",
                this.step.environmentName, exception.getMessage()));

        if (exception instanceof WTRestException) {
          if (!((WTRestException) exception).getCode().equals("100105") && this.step.failOnError) {
            throw new AbortException("create deploy environment error");
          }
        } else if (this.step.failOnError) {
          throw new AbortException("create deploy environment error " + exception.getMessage());
        }
      }
      entity.envId = envId;
      entity.duration = run.getDuration();
      entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
      entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
      entity.duration = run.getDuration();

      WTRestService session = new WTRestService();
      try {
        session.createDeploy(entity);
      } catch (Exception exception) {
        log.error(
            String.format(
                "Create deploy(%s) error; stack: %s ",
                this.step.releaseName, exception.getMessage()));
        if (this.step.failOnError) {
          throw new AbortException("create worktile deploy error " + exception.getMessage());
        }
      }

      return true;
    }

    public String handleEnvName(String name) throws IOException, WTRestException {
      WTRestService service = new WTRestService();
      WTEnvironmentSchema schema = service.getEnvironmentByName(name);
      if (schema == null) {
        schema = service.createEnvironment(new WTEnvironmentEntity(name));
      }
      return schema.id;
    }
  }

  @Extension
  public static class DescriptorImpl extends StepDescriptor {

    @Override
    public Set<Class<?>> getRequiredContext() {
      return ImmutableSet.of(Run.class, EnvVars.class, TaskListener.class, FilePath.class);
    }

    @Override
    public String getFunctionName() {
      return "wtSendDeploy";
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String getDisplayName() {
      return "send deploy result to worktile";
    }
  }
}
