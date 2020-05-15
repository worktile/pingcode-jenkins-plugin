package io.jenkins.plugins.worktile.pipeline;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.antlr.v4.runtime.misc.NotNull;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

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
import io.jenkins.plugins.worktile.service.WTRestSession;

public class WTSendDeployStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;

    private String releaseName;
    private String environmentName;
    private String releaseURL;
    private String buildResult;

    private boolean failOnError;

    @DataBoundConstructor
    public WTSendDeployStep(@NotNull String releaseName, @NotNull String environmentName, String releaseURL) {
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

    public static class WTSendDeployStepExecution extends SynchronousNonBlockingStepExecution<Boolean> {
        private static final long serialVersionUID = 1L;

        private final WTSendDeployStep step;

        public WTSendDeployStepExecution(StepContext context, WTSendDeployStep step) {
            super(context);

            this.step = step;
        }

        public String handleEnvName(String name) throws IOException, WTRestException {
            WTRestSession session = new WTRestSession();
            WTEnvironmentSchema schema = session.getEnvironmentByName(name);
            if (schema == null) {
                schema = session.createEnvironment(new WTEnvironmentEntity(name));
            }
            return schema.id;
        }

        @Override
        public Boolean run() throws Exception {
            WorkflowRun build = getContext().get(WorkflowRun.class);
            TaskListener listener = getContext().get(TaskListener.class);
            WTLogger log = new WTLogger(listener);

            WTRestSession session = new WTRestSession();

            WTDeployEntity entity = new WTDeployEntity();
            entity.releaseName = WTHelper.renderStringByEnvVars(this.step.releaseName,
                    build.getEnvironment(TaskListener.NULL));
            ;
            entity.releaseUrl = WTHelper.renderStringByEnvVars(this.step.releaseURL,
                    build.getEnvironment(TaskListener.NULL));

            entity.startAt = WTHelper.toSafeTs(build.getStartTimeInMillis());
            entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
            entity.duration = build.getDuration();
            entity.status = this.step.buildResult == "success" ? "deployed" : "not_deployed";
            entity.workItemIdentifiers = WTHelper.resolveWorkItemsFromPipelineStep(build).toArray(new String[0]);

            try {
                entity.envId = handleEnvName(this.step.environmentName);
            } catch (Exception exception) {
                log.error(String.format("Create environment(%s) error; stack: %s", this.step.environmentName,
                        exception.getMessage()));
                if (this.step.failOnError) {
                    return false;
                }
            }

            try {
                session.createDeploy(entity);
                log.info("create deploy success: " + entity.releaseName);
            } catch (Exception exception) {
                log.error(String.format("Create deploy(%s) error; stack: %s ", this.step.releaseName,
                        exception.getMessage()));
                if (this.step.failOnError) {
                    return false;
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
            return "wtSendDeploy";
        }

        @NotNull
        public String getDisplayName() {
            return "send deploy result to worktile";
        }
    }
}
