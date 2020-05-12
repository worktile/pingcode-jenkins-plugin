package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.service.WorktileRestSession;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WorktileBuildNotifier extends Notifier {
    private static final long serialVersionUID = 1L;

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
            WTErrorEntity error = session.createBuild(this.makeResult(build, listener));
            if (error.getMessage() != null) {
                listener.getLogger().println(error.toString());
            }
            listener.getLogger().println("Send build data to worktile open api");
        } catch (IOException error) {
            listener.getLogger().println("Create worktile build error " + error.getMessage());
        }
    }

    private WTBuildEntity makeResult(AbstractBuild<?, ?> build, BuildListener listener) throws IOException {
        WTBuildEntity result = new WTBuildEntity();
        result.name = build.getFullDisplayName();
        result.identifier = build.getId();
        result.jobUrl = build.getProject().getAbsoluteUrl();
        result.status = Objects.requireNonNull(build.getResult()).toString().toLowerCase();
        result.startAt = WorktileUtils.toSafeTs(build.getStartTimeInMillis());
        result.endAt = WorktileUtils.toSafeTs(System.currentTimeMillis());
        result.duration = build.getDuration();
        result.resultOverview = "";
        result.resultUrl = build.getProject().getAbsoluteUrl() + build.getNumber() + "/console";
        result.workItemIdentifiers = WorktileUtils.resolveWorkItems(build, listener).toArray(new String[0]);

        try {
            logger.info("start match overview " + this.getOverview());

            List<String> matched = WorktileUtils.getMatchSet(Pattern.compile(this.getOverview()), build.getLog(999),
                    true, true);

            result.resultOverview = matched.size() > 0 ? matched.get(0) : "";
        } catch (Exception error) {
            logger.info("match overview result error" + error.getMessage());
        }
        return result;
    }

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {

        public Descriptor() {
            super(WorktileBuildNotifier.class);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @NotNull
        @Override
        public String getDisplayName() {
            return "Worktile build notifier";
        }

        @Override
        public WorktileBuildNotifier newInstance(StaplerRequest request, @NotNull JSONObject formData)
                throws FormException {
            assert request != null;
            return request.bindJSON(WorktileBuildNotifier.class, formData);
        }
    }
}
