package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.service.WTRestService;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;

public class WTBuildNotifier extends Notifier implements SimpleBuildStep {

    private String overview;

    @DataBoundConstructor
    public WTBuildNotifier(String overview) {
        setOverview(overview);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
            @Nonnull TaskListener listener) throws IOException, InternalError {
        this.createBuild(run, workspace, listener);
    }

    private void createBuild(Run<?, ?> run, FilePath workspace, @Nonnull TaskListener listener) throws IOException {
        WTLogger logger = new WTLogger(listener);
        WTBuildEntity entity = WTBuildEntity.from(run, workspace, listener, getOverview());

        WTRestService service = new WTRestService();
        logger.info("Will send data to worktile: " + entity.toString());
        try {
            service.createBuild(entity);
            logger.info("Create worktile build record successfully.");
        } //
        catch (Exception error) {
            logger.error(error.getMessage());
        }
    }

    public String getOverview() {
        return overview;
    }

    @DataBoundSetter
    public void setOverview(String overview) {
        this.overview = overview;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
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
            return Messages.WTBuildNotifier_DisplayName();
        }

        @Override
        public WTBuildNotifier newInstance(StaplerRequest request, @NotNull JSONObject formData) throws FormException {
            assert request != null;
            return request.bindJSON(WTBuildNotifier.class, formData);
        }
    }
}
