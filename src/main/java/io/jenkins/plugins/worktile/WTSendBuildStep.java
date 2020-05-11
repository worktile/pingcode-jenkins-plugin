package io.jenkins.plugins.worktile;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

public class WTSendBuildStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reviewPattern;

    @DataBoundConstructor
    public WTSendBuildStep(String reviewPattern) {
        setReviewPattern(reviewPattern);
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

    public static class WTSendBuildStepExecution extends SynchronousNonBlockingStepExecution<Boolean> {
        private static final long serialVersionUID = 1L;

        private final WTSendBuildStep buildStep;

        public WTSendBuildStepExecution(StepContext context, WTSendBuildStep step) {
            super(context);
            this.buildStep = step;
        }

        @Override
        public Boolean run() throws Exception {
            final TaskListener listener = getContext().get(TaskListener.class);
            final Run<?, ?> run = getContext().get(Run.class);

            WTLogger logger = new WTLogger(listener);
            Result buildResult = run.getResult();
            String result = buildResult != null ? buildResult.toString() : buildResult.SUCCESS.toString();

            RunWrapper wrapper = new RunWrapper(run, true);

            logger.info("build result => " + wrapper.getCurrentResult());
            logger.info("reviewPattern => " + this.buildStep.reviewPattern);
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

        public String getDisplayName() {
            return "send build result to worktile";
        }
    }
}
