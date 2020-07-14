package io.jenkins.plugins.worktile.pipeline;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestService;

public class WTSendBuildStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;

    @DataBoundSetter
    private String overviewPattern;

    @DataBoundSetter
    private String defaultSummary;

    @DataBoundSetter
    private boolean failOnError;

    @DataBoundSetter
    private String status;

    @DataBoundSetter
    private String resultURL;

    @DataBoundConstructor
    public WTSendBuildStep() {
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
            WorkflowRun run = getContext().get(WorkflowRun.class);
            TaskListener listener = getContext().get(TaskListener.class);
            FilePath workspace = getContext().get(FilePath.class);

            WTLogger logger = new WTLogger(listener);
            WTBuildEntity entity = WTBuildEntity.from(run, //
                    workspace, //
                    listener, //
                    step.status, //
                    step.overviewPattern, //
                    step.defaultSummary, //
                    step.resultURL);
            WTRestService service = new WTRestService();
            logger.info("Will send data to worktile: " + entity.toString());
            try {
                service.createBuild(entity);
                logger.info("Create worktile build record successfully.");
            } catch (Exception exception) {
                logger.error(exception.getMessage());
                if (this.step.failOnError) {
                    throw new AbortException(exception.getMessage());
                }
            }
            return true;
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, EnvVars.class, TaskListener.class, FilePath.class, Launcher.class);
        }

        @Override
        public String getFunctionName() {
            return "worktileBuildRecord";
        }

        @NotNull
        public String getDisplayName() {
            return "Send build result to worktile";
        }
    }
}
