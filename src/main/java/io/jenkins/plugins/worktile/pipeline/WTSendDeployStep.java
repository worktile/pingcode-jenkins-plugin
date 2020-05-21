package io.jenkins.plugins.worktile.pipeline;

import com.google.common.collect.ImmutableSet;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.service.WTRestService;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class WTSendDeployStep extends Step implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String releaseName;

  private final String environmentName;

  @DataBoundSetter
  private String releaseURL;

  @DataBoundSetter
  private boolean failOnError;

  @DataBoundSetter
  private String status;

  @DataBoundConstructor
  public WTSendDeployStep(String releaseName, String environmentName) {
    this.releaseName = releaseName;
    this.environmentName = environmentName;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new WTSendDeployStepExecution(context, this);
  }

  public static class WTSendDeployStepExecution extends SynchronousNonBlockingStepExecution<Boolean> {
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
      WTLogger wtLogger = new WTLogger(listener);

      WTRestService service = new WTRestService();
      String envId = null;
      try {
        envId = handleEnvName(this.step.environmentName, service);
      } catch (Exception exception) {
        wtLogger.error(exception.getMessage());
        if (exception instanceof WTRestException) {
          if (!((WTRestException) exception).getCode().equals("100105") && this.step.failOnError) {
            throw new AbortException(exception.getMessage());
          }
        } else if (this.step.failOnError) {
          throw new AbortException(exception.getMessage());
        }
      }

      WTDeployEntity entity = WTDeployEntity.from(run, this.step.status, this.step.releaseName, this.step.releaseURL,
          envId);

      wtLogger.info("Will send data to worktile: " + entity.toString());
      try {
        service.createDeploy(entity);
        wtLogger.info("Create worktile deploy record successfully.");
      } catch (Exception exception) {
        wtLogger.error(exception.getMessage());
        if (this.step.failOnError) {
          throw new AbortException(exception.getMessage());
        }
      }
      return true;
    }

    public String handleEnvName(String name, WTRestService service) throws IOException, WTRestException {
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
      return "worktileDeployRecord";
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String getDisplayName() {
      return "Send deploy result to worktile";
    }
  }
}
