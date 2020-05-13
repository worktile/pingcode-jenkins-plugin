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
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.WorktileUtils;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.service.WorktileRestSession;

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

            assert build != null;
            TaskListener listener = getContext().get(TaskListener.class);
            WTLogger logger = new WTLogger(listener);

            WTBuildEntity buildEntity = new WTBuildEntity();

            buildEntity.name = build.getFullDisplayName().replace(" #", "-");
            buildEntity.identifier = build.getId();
            buildEntity.status = this.step.buildResult;

            List<String> resultOverview = WorktileUtils.getMatchSet(Pattern.compile(this.step.reviewPattern),
                    build.getLog(999), true, true);
            buildEntity.resultOverview = resultOverview.size() > 0 ? resultOverview.get(0) : "";

            buildEntity.jobUrl = build.getParent().getAbsoluteUrl() + build.getNumber() + "/";
            buildEntity.resultUrl = build.getParent().getAbsoluteUrl() + build.getNumber() + "/console";

            // TODO: merge with FreeStyle Build
            List<String> array = new ArrayList<>();
            List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = build.getChangeSets();
            changeLogSets.forEach(changeLogSet -> {
                for (Object change : changeLogSet) {
                    ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
                    array.add(entry.getMsg());
                }
            });
            buildEntity.workItemIdentifiers = array.toArray(new String[0]);
            buildEntity.startAt = WorktileUtils.toSafeTs(build.getStartTimeInMillis());
            buildEntity.endAt = WorktileUtils.toSafeTs(build.getTimeInMillis());
            buildEntity.duration = build.getDuration();

            WorktileRestSession session = new WorktileRestSession();
            try {
                WTErrorEntity error = session.createBuild(buildEntity);
                if (error != null) {
                    logger.info("save build error " + "code: " + error.getCode() + " message: " + error.getMessage());
                }
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
