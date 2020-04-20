package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.worktile.model.BuildResult;
import io.jenkins.plugins.worktile.model.WTError;
import io.jenkins.plugins.worktile.service.WorktileRestSession;
import net.sf.json.JSONObject;

public class WorktileBuildNotifier extends Notifier {
    private static final long serialVersionUID = 699563338312232812L;

    public static final Logger logger = Logger.getLogger(WorktileBuildNotifier.class.getName());

    private String overview;

    @DataBoundConstructor
    public WorktileBuildNotifier(String overview) {
        setOverview(overview);
    }

    public String getOverview() {
        return overview;
    }

    @DataBoundSetter
    public void setOverview(String overview) {
        this.overview = overview;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InternalError {
        this.createBuild(build, launcher, listener);
        return true;
    }

    private void createBuild(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        WorktileRestSession session = new WorktileRestSession();
        try {
            WTError error = session.createBuild(this.makeResult(build, listener));
            if (error != null) {
                listener.getLogger().println(error.getCode() + " " + error.getMessage());
            }
            listener.getLogger().println("Send build data to worktile api");
        } catch (IOException error) {
            listener.getLogger().println("create worktile build error");
        }
        return;
    }

    private BuildResult makeResult(AbstractBuild<?, ?> build, BuildListener listener) throws IOException {
        BuildResult result = new BuildResult();
        result.name = build.getFullDisplayName();
        result.identifier = build.getId();
        result.jobUrl = build.getProject().getAbsoluteUrl();
        result.status = build.getResult().toString().toLowerCase();
        result.startAt = WorktileUtils.toSafeTs(build.getStartTimeInMillis());
        result.endAt = WorktileUtils.toSafeTs(System.currentTimeMillis());
        result.duration = build.getDuration();
        result.resultOverview = build.getLog();
        result.resultUrl = build.getAbsoluteUrl();

        HashSet<String> set = new HashSet<>();
        ChangeLogSet changes = build.getChangeSet();
        for (Object change : changes) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
            set.add(entry.getMsg());
        }
        try {
            String branch = build.getEnvironment(listener).get("GIT_BRANCH");
            set.add(branch);
        } catch (Exception error) {
            logger.info("Get $GIT_BRANCH error");
        }
        result.workItems = WorktileUtils.getWorkItems(set.toArray(new String[set.size()]));

        try {
            String[] matched = WorktileUtils.getMatchSet(Pattern.compile(this.getOverview()),
                    build.getLog().split("\n"), true);
            result.resultOverview = matched.length > 0 ? matched[0] : "";
        } catch (Exception error) {
            logger.info("match overview result error" + error.getMessage());
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {

        public Descriptor() {
            super(WorktileBuildNotifier.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Worktile create build records step";
        }

        @Override
        public WorktileBuildNotifier newInstance(StaplerRequest request, JSONObject formData) throws FormException {
            return request.bindJSON(WorktileBuildNotifier.class, formData);
        }
    }
}
