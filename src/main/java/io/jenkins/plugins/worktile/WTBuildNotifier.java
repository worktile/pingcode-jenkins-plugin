package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestSession;
import net.sf.json.JSONObject;

public class WTBuildNotifier extends Notifier {
    private static final long serialVersionUID = 1L;

    public static final Logger logger = Logger.getLogger(WTBuildNotifier.class.getName());

    private String overview;

    @DataBoundConstructor
    public WTBuildNotifier(String overview) {
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
        WTRestSession session = new WTRestSession();
        try {
            session.createBuild(this.makeResult(build, listener));
            listener.getLogger().println("Send build data to worktile open api");
        } catch (Exception error) {
            listener.getLogger().println("Create worktile build error " + error.getMessage());
        }
    }

    private WTBuildEntity makeResult(AbstractBuild<?, ?> build, BuildListener listener) throws IOException {
        String status = Objects.requireNonNull(build.getResult()).toString().toLowerCase();
        WTBuildEntity.Builder builder = new WTBuildEntity.Builder()
                .withName(build.getFullDisplayName().replace(" #", "-")).withIdentifier(build.getId())
                .withJobUrl(build.getProject().getAbsoluteUrl() + build.getNumber() + "/")
                .withRusultUrl(build.getProject().getAbsoluteUrl() + build.getNumber() + "/console")
                .withStatus(status == "success" ? "success" : "failure")
                .withStartAt(WTHelper.toSafeTs(build.getStartTimeInMillis()))
                .withWorkItemIdentifiers(WTHelper.resolveWorkItems(build, listener).toArray(new String[0]))
                .withEndAt(WTHelper.toSafeTs(System.currentTimeMillis())).withDuration(build.getDuration());

        try {
            List<String> matched = WTHelper.getMatchSet(Pattern.compile(this.getOverview()), build.getLog(999), true,
                    true);
            builder.withResultOvervier(matched.size() > 0 ? matched.get(0) : "");
        } catch (Exception error) {
            logger.info("match overview result error" + error.getMessage());
        }
        return builder.build();
    }

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {

        public Descriptor() {
            super(WTBuildNotifier.class);
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
        public WTBuildNotifier newInstance(StaplerRequest request, @NotNull JSONObject formData) throws FormException {
            assert request != null;
            return request.bindJSON(WTBuildNotifier.class, formData);
        }
    }
}
