package io.jenkins.plugins.worktile.resolver;

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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkItemResolver {
    public static final Logger logger = Logger.getLogger(WorkItemResolver.class.getName());
    public static final Pattern pattern = Pattern.compile("#[^(\\s|/)]*[A-Za-z0-9_-]{0,10}-[0-9]+");
    public static final String VCSFolder = ".git";

    private final Set<String> collection = new HashSet<>();

    private final WTLogger wtLogger;
    private final Run<?, ?> run;
    private final FilePath workspace;
    private final TaskListener listener;

    private final boolean isTagged;

    public WorkItemResolver(
            final Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        this(run, workspace, listener, false);
    }

    public WorkItemResolver(
            final Run<?, ?> run, final FilePath workspace, final TaskListener listener, boolean isTagged) {
        this.run = run;
        this.workspace = workspace;
        this.listener = listener;
        this.wtLogger = new WTLogger(this.listener);
        this.isTagged = isTagged;
    }

    public List<String> resolve() {
        collection.clear();
        fromChangeLog();
        fromEnvironment();
        try {
            fromScm();
        } catch (final Exception e) {
            wtLogger.info("Extract work items error from message body " + e.getMessage());
        }

        if (isTagged) {
            try {
                fromTag();
            } catch (final Exception e) {
                wtLogger.info("Extract work items error from tag " + e.getMessage());
            }
        }
        Set<String> sets = new HashSet<>();
        collection.forEach(item -> {
            Matcher matcher = pattern.matcher(item);
            while (matcher.find()) {
                sets.add(matcher.group().toUpperCase());
            }
        });
        return WTHelper.formatWorkItems(new ArrayList<>(sets));
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
            logger.info("prActualCommit is null, please use pull request builder trigger the build");
            return;
        }
        List<String> messages = gitStoreDir
                .act(new GitCommitMessageCallback(listener, ObjectId.fromString(prActualCommit)));
        collection.addAll(messages);
    }

    public void fromTag() throws IOException, InterruptedException, GitAPIException {
        if (run == null || workspace == null) {
            return;
        }
        final boolean isGit = workspace.child(VCSFolder).exists();
        if (!isGit) {
            wtLogger.info("Unsupported vcs, current git only");
        }
        final FilePath gitStoreDir = workspace.child(VCSFolder);
        List<String> messages = gitStoreDir.act(new GitTagsCallback(listener));
        collection.addAll(messages);
    }

    @SuppressWarnings("rawtypes")
    private RunWithSCM<?, ?> toSCMRun() {
        RunWithSCM runWithScm = null;
        if (run instanceof AbstractBuild<?, ?>) {
            runWithScm = (AbstractBuild<?, ?>) run;
        } else if (run instanceof WorkflowRun) {
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

    private static final class GitTagsCallback extends MasterToSlaveFileCallable<List<String>> {
        private static final long serialVersionUID = -247109644349075954L;

        private final TaskListener listener;

        public GitTagsCallback(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public List<String> invoke(final File file, final VirtualChannel virtualChannel)
                throws IOException, InterruptedException {
            List<String> messages = new ArrayList<>();
            WTLogger wtLogger = new WTLogger(listener);
            if (!file.exists() || !file.isDirectory()) {
                return messages;
            }
            try (FileRepository fileRepository = new FileRepository(file.getAbsolutePath())) {
                List<Ref> tags = null;
                Git git = new Git(fileRepository);
                try {
                    tags = git.tagList().call();
                    this.sort(tags, new RevWalk(fileRepository));
                    if (!tags.isEmpty()) {
                        Ref tag0 = tags.get(0);
                        Ref peeledRef0 = fileRepository.getRefDatabase().peel(tag0);
                        wtLogger.info("current tag = " + tag0.getName());
                        ObjectId utilId = peeledRef0.getPeeledObjectId() != null ? peeledRef0.getPeeledObjectId()
                                : tag0.getObjectId();

                        Ref tag1 = tags.get(1);
                        ObjectId startId = null;
                        if (tag1 != null) {
                            wtLogger.info("previous tag = " + tag1.getName());
                            Ref peeledRef1 = fileRepository.getRefDatabase().peel(tag1);
                            startId = peeledRef1.getPeeledObjectId() != null ? peeledRef1.getPeeledObjectId()
                                    : tag1.getObjectId();
                        }

                        LogCommand log = git.log().addRange(startId, utilId);
                        Iterable<RevCommit> logs = log.call();
                        for (RevCommit commit : logs) {
                            if (commit != null) {
                                String message = commit.getFullMessage();
                                if (message != null) {
                                    messages.add(message);
                                }
                            }
                        }
                    }
                } catch (Exception exception) {
                    wtLogger.error("get commit message in tag error");
                }
                git.close();
            }
            return messages;
        }

        private void sort(List<Ref> tags, RevWalk walk) {
            tags.sort((t1, t2) -> {
                Date d1;
                Date d2;
                try {
                    d1 = walk.parseCommit(t1.getObjectId()).getCommitterIdent().getWhen();
                    d2 = walk.parseCommit(t2.getObjectId()).getCommitterIdent().getWhen();
                    return d2.compareTo(d1);
                } catch (Exception e) {
                    logger.info("sort tags error: " + e.getMessage());
                }
                return 0;
            });
        }
    }
}
