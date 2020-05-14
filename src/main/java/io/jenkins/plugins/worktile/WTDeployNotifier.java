package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.service.WTRestSession;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Objects;
import java.util.logging.Logger;

public class WTDeployNotifier extends Notifier {
    public static final Logger logger = Logger.getLogger(WTDeployNotifier.class.getName());

    private String environment;

    private String releaseName;

    private String releaseUrl;

    @DataBoundConstructor
    public WTDeployNotifier(final String environment, final String releaseName, final String releaseUrl) {
        setReleaseName(releaseName);
        setReleaseUrl(releaseUrl);
        setEnvironment(environment);
    }

    public String getEnvironment() {
        return environment;
    }

    @DataBoundSetter
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getReleaseName() {
        return releaseName;
    }

    @DataBoundSetter
    public void setReleaseName(final String releaseName) {
        this.releaseName = releaseName;
    }

    public String getReleaseUrl() {
        return releaseUrl;
    }

    @DataBoundSetter
    public void setReleaseUrl(final String releaseUrl) {
        this.releaseUrl = releaseUrl;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        performCreateDeploy(build, launcher, listener);
        return true;
    }

    private void performCreateDeploy(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        WTRestSession session = new WTRestSession();
        try {
            session.createDeploy(makeEntity(build, listener));
        } catch (Exception error) {
            listener.getLogger().println("create deploy failure " + error.getMessage());
        }
    }

    private WTDeployEntity makeEntity(AbstractBuild<?, ?> build, BuildListener listener) {
        WTDeployEntity entity = new WTDeployEntity();
        entity.releaseName = WTHelper.renderTemplateString(getReleaseName(), build);
        entity.releaseUrl = WTHelper.renderTemplateString(getReleaseUrl(), build);
        entity.envId = getEnvironment();
        entity.startAt = WTHelper.toSafeTs(build.getStartTimeInMillis());
        String buildResult = Objects.requireNonNull(build.getResult()).toString().toLowerCase();
        entity.status = buildResult.equals("success") ? "deployed" : "not_deployed";
        entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
        entity.duration = build.getDuration();
        entity.workItemIdentifiers = WTHelper.resolveWorkItems(build, listener).toArray(new String[0]);
        return entity;
    }

    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Publisher> {
        public Descriptor() {
            load();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(@NotNull final Class<? extends AbstractProject> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Worktile deploy notifier";
        }

        @Override
        public WTDeployNotifier newInstance(final StaplerRequest request, @NotNull final JSONObject formData) {
            assert request != null;
            return request.bindJSON(WTDeployNotifier.class, formData);
        }

        public FormValidation doCheckReleaseName(
                @QueryParameter(value = "releaseName", fixEmpty = true) String releaseName) {
            if (WTHelper.isBlank(releaseName)) {
                return FormValidation.error("release name can not be empty");
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillEnvironmentItems() {
            final ListBoxModel items = new ListBoxModel();
            WTRestSession session = new WTRestSession();
            try {
                WTPaginationResponse<WTEnvironmentSchema> schemas = session.listEnvironments();
                for (WTEnvironmentSchema schema : schemas.values) {
                    items.add(schema.name, schema.id);
                }
            } catch (Exception exception) {
                WTDeployNotifier.logger.info("get environments error " + exception.getMessage());
            }
            return items;
        }

        @Override
        public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
            save();
            return super.configure(request, formData);
        }
    }
}
