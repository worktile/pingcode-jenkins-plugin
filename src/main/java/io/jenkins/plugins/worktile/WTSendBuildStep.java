package io.jenkins.plugins.worktile;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;
import org.jetbrains.annotations.NotNull;
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

    private String buildResult;

    @DataBoundConstructor
    public WTSendBuildStep(String reviewPattern) {
        setReviewPattern(reviewPattern);
    }

    public String getBuildResult() {
        return buildResult;
    }

    @NotNull
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

    public static class WTSendBuildStepExecution extends SynchronousNonBlockingStepExecution<Boolean> {
        private static final long serialVersionUID = 1L;
        private final WTSendBuildStep step;

        public WTSendBuildStepExecution(StepContext context, WTSendBuildStep step) {
            super(context);
            this.step = step;
        }

        @Override
        public Boolean run() throws Exception {
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
