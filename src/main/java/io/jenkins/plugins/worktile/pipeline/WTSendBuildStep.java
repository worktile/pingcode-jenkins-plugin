package io.jenkins.plugins.worktile.pipeline;

import com.google.common.collect.ImmutableSet;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestService;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.*;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class WTSendBuildStep extends Step implements Serializable {
  private static final long serialVersionUID = 1L;

  private String reviewPattern;

  private String buildResult;

  private boolean failOnError;

  @DataBoundConstructor
  public WTSendBuildStep(String reviewPattern, String buildResult, boolean failOnError) {
    setBuildResult(Objects.requireNonNull(buildResult));
    setReviewPattern(reviewPattern);
    setFailOnError(failOnError);
  }

  public boolean isFailOnError() {
    return failOnError;
  }

  @DataBoundSetter
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  public String getBuildResult() {
    return buildResult;
  }

  @DataBoundSetter
  public void setBuildResult(String buildResult) {
    this.buildResult = buildResult;
  }

  public String getReviewPattern() {
    return reviewPattern;
  }

  @DataBoundSetter
  public void setReviewPattern(String reviewPattern) {
    this.reviewPattern = reviewPattern;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new WTSendBuildStepExecution(context, this);
  }

  public static class WTSendBuildStepExecution
      extends SynchronousNonBlockingStepExecution<Boolean> {
    private static final long serialVersionUID = 1L;

    private final WTSendBuildStep step;

    public WTSendBuildStepExecution(StepContext context, WTSendBuildStep step) {
      super(context);
      this.step = step;
    }

    @Override
    public Boolean run() throws Exception {
      WorkflowRun run = getContext().get(WorkflowRun.class);
      TaskListener listener = getContext().get(TaskListener.class);
      WTLogger logger = new WTLogger(listener);

      WTBuildEntity entity = WTBuildEntity.from(run, this.step.reviewPattern);
      WTRestService service = new WTRestService();
      try {
        service.createBuild(entity);
      } catch (Exception exception) {
        logger.error("create build error : " + exception.getMessage());
        if (this.step.failOnError) {
          throw new AbortException();
        }
      }
      return true;
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
      return "wtSendBuild";
    }

    @NotNull
    public String getDisplayName() {
      return "send build result to worktile";
    }
  }
}
