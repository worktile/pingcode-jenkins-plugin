package io.jenkins.plugins.worktile.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestSession;

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
            WorkflowRun build = getContext().get(WorkflowRun.class);
            TaskListener listener = getContext().get(TaskListener.class);
            WTLogger logger = new WTLogger(listener);

            String status = Objects.requireNonNull(build.getResult()).toString().toLowerCase();

            List<String> array = new ArrayList<>();
            List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = build.getChangeSets();
            changeLogSets.forEach(changeLogSet -> {
                for (Object change : changeLogSet) {
                    ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
                    array.add(entry.getMsg());
                }
            });
            WTBuildEntity.Builder builder = new WTBuildEntity.Builder()
                    .withName(build.getFullDisplayName().replace(" #", "-")).withIdentifier(build.getId())
                    .withJobUrl(build.getAbsoluteUrl() + build.getNumber() + "/")
                    .withRusultUrl(build.getAbsoluteUrl() + build.getNumber() + "/console")
                    .withStatus(status == "success" ? "success" : "failure")
                    .withStartAt(WTHelper.toSafeTs(build.getStartTimeInMillis()))
                    .withWorkItemIdentifiers(array.toArray(new String[0]))
                    .withEndAt(WTHelper.toSafeTs(System.currentTimeMillis())).withDuration(build.getDuration());

            WTRestSession session = new WTRestSession();
            try {
                session.createBuild(builder.build());
            } catch (Exception exception) {
                logger.info(exception.getMessage());
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
