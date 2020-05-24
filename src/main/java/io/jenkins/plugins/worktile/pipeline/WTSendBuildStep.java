package io.jenkins.plugins.worktile.pipeline;

import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.worktile.WTLogger;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestService;
import jenkins.MasterToSlaveFileCallable;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.*;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class WTSendBuildStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;

    @DataBoundSetter
    private String overviewPattern;

    @DataBoundSetter
    private boolean failOnError;

    @DataBoundSetter
    private String status;

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

            /* start: try get scm information */
            // FilePath workspace = getContext().get(FilePath.class);
            // Launcher launcher = getContext().get(Launcher.class);
            //
            // assert run != null;
            // boolean isGit = false;
            // assert workspace != null;
            //
            // FilePath scmStubDir = null;
            //
            // if(workspace.child(".git").exists()) {
            // isGit = true;
            // scmStubDir = workspace.child(".git");
            // }
            // if(!isGit) {
            // logger.error("not found .git folder");
            // }
            //
            // assert scmStubDir != null;
            // ObjectId currentCommitId = scmStubDir.act(new
            // GitInformationCallable(listener));
            //
            // logger.info("latest commit Id is " + currentCommitId.toString());
            // String prHeadCommit =
            // run.getEnvironment(TaskListener.NULL).get("ghprbActualCommit");
            //
            // if(prHeadCommit == null) {
            // logger.info("can not get prHeadCommit");
            // }
            // logger.info("scmStubDir = " + workspace.absolutize().getName() + "/" +
            // scmStubDir.getName());
            // Repository fileRepository = new
            // FileRepository("/Users/cheerfyt/.jenkins/workspace/debug-CI/.git");
            // Git git = new Git(fileRepository);
            //
            // assert prHeadCommit != null;
            // logger.info("pr header = " + prHeadCommit + " current = " +
            // currentCommitId.toString());
            //
            // Iterable<RevCommit> items = git.log().addRange(currentCommitId,
            // ObjectId.fromString(prHeadCommit)).call();
            //
            // for(RevCommit item : items) {
            // if(item != null) {
            // String message = item.getFullMessage();
            // logger.info("commit message = " + message);
            // }
            // }
            /* end: get scm information */

            WTBuildEntity entity = WTBuildEntity.from(run, workspace, listener, step.status, step.overviewPattern);
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

    private static class GitInformationCallable extends MasterToSlaveFileCallable<ObjectId> {
        private final TaskListener listener;

        public GitInformationCallable(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public ObjectId invoke(File file, VirtualChannel virtualChannel) throws IOException {
            if (!file.exists() || !file.isDirectory()) {
                return null;
            }
            WTLogger wtLogger = new WTLogger(listener);
            wtLogger.info("invoke dir = " + file.getAbsolutePath());
            Repository gitRepository = new FileRepository(file.getAbsoluteFile());
            ObjectId objectId = gitRepository.resolve("HEAD~^{commit}");
            if (objectId == null) {
                wtLogger.info("cant resolve latest commit sha");
                return null;
            }
            return objectId;
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
