package io.jenkins.plugins.worktile.resolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.WTLogger;
import jenkins.MasterToSlaveFileCallable;
import jenkins.scm.RunWithSCM;

public class WorkItemResolver {
    public static final Logger logger = Logger.getLogger(WorkItemResolver.class.getName());
    public static final Pattern pattern = Pattern.compile("#[^(\\s|/)]*[A-Za-z0-9_]+-[0-9]+");
    public static final String VCSFolder = ".git";

    private final Set<String> collection = new HashSet<>();

    private final WTLogger wtLogger;
    private final Run<?, ?> run;
    private final FilePath workspace;
    private final TaskListener listener;

    public WorkItemResolver(final Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        this.run = run;
        this.workspace = workspace;
        this.listener = listener;
        this.wtLogger = new WTLogger(this.listener);
    }

    @SuppressWarnings("rawtypes")
    public static WorkItemResolver create(final Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        return new WorkItemResolver(run, workspace, listener);
    }

    public List<String> resolve() {
        collection.clear();
        fromChangeLog();
        fromEnvironment();
        try {
            fromScm();
        } catch (final Exception e) {
            wtLogger.info("Extract work items error " + e.getMessage());
        }
        final List<String> matches = WTHelper.matches(pattern, new ArrayList<>(collection), false, false);
        return WTHelper.formatWorkItems(matches);
    }

    @SuppressWarnings("rawtypes")
    public void fromChangeLog() {
        final RunWithSCM scm = toSCMRun();

        if (scm == null) {
            return;
        }

        final List changeLogSets = scm.getChangeSets();
        for (final Object changeLogSet : changeLogSets) {
            for (final Object set : (ChangeLogSet<? extends Entry>) changeLogSet) {
                final String msg = ((Entry) set).getMsg();
                if (msg != null) {
                    collection.add(msg);
                }
            }
        }
    }

    public void fromEnvironment() {
        final EnvVars envVars = WTHelper.safeEnvVars(run);
        if (envVars.get("GIT_BRANCH") != null) {
            collection.add(envVars.get("GIT_BRANCH"));
        }
        if (envVars.get("ghprbSourceBranch") != null) {
            collection.add(envVars.get("ghprbSourceBranch"));
        }
        if (envVars.get("ghprbPullTitle") != null) {
            collection.add(envVars.get("ghprbPullTitle"));
        }
        if (envVars.get("ghprbCommentBody") != null) {
            collection.add(envVars.get("ghprbCommentBody"));
        }
    }

    public void fromScm() throws IOException, InterruptedException, GitAPIException {
        if (run == null || workspace == null) {
            return;
        }
        final boolean isGit = workspace.child(VCSFolder).exists();
        if (!isGit) {
            wtLogger.info("unsupported vcs, current git only");
        }

        final FilePath gitStoreDir = workspace.child(VCSFolder);
        final String prActualCommit = run.getEnvironment(TaskListener.NULL).get("ghprbActualCommit");
        if (prActualCommit == null) {
            logger.info("can't get prActualCommit or currentHeadId");
            return;
        }
        List<String> messages = gitStoreDir
                .act(new GitCommitMessageCallback(listener, ObjectId.fromString(prActualCommit)));
        collection.addAll(messages);
    }

    @SuppressWarnings("rawtypes")
    private RunWithSCM<?, ?> toSCMRun() {
        RunWithSCM runWithScm = null;
        if (run instanceof AbstractBuild<?, ?>) {
            runWithScm = (AbstractBuild<?, ?>) run;
        } //
        else if (run instanceof WorkflowRun) {
            runWithScm = (WorkflowRun) run;
        }
        return runWithScm;
    }

    private static final class GitCommitMessageCallback extends MasterToSlaveFileCallable<List<String>> {
        private static final long serialVersionUID = 8799047890954988521L;
        private final TaskListener listener;
        private final ObjectId prHeadCommitId;

        public GitCommitMessageCallback(TaskListener listener, ObjectId prHeadCommitId) {
            this.listener = listener;
            this.prHeadCommitId = prHeadCommitId;
        }

        @Override
        public List<String> invoke(final File file, final VirtualChannel virtualChannel)
                throws IOException, InterruptedException {
            List<String> messages = new ArrayList<>();
            if (!file.exists() || !file.isDirectory()) {
                return messages;
            }
            try (FileRepository fileRepository = new FileRepository(file.getAbsolutePath())) {
                listener.getLogger().println("FileResponse path = " + file.getAbsolutePath());
                ObjectId currentHeadId = fileRepository.resolve("HEAD~^{commit}");
                if (currentHeadId == null) {
                    return messages;
                }
                Git git = new Git(fileRepository);
                try {
                    final Iterable<RevCommit> items = git.log().addRange(currentHeadId, prHeadCommitId).call();
                    for (final RevCommit commit : items) {
                        if (commit != null) {
                            messages.add(commit.getFullMessage());
                        }
                    }
                } catch (Exception e) {
                    listener.getLogger().println("collection message error: " + e.getMessage());
                }
                git.close();
                return messages;
            }
        }
    }
}
