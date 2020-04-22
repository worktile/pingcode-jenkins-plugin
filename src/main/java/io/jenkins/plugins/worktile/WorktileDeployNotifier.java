package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

public class WorktileDeployNotifier extends Notifier {

    private List<String> environments;

    private String releaseName;

    private String releaseUrl;

    @DataBoundConstructor
    public WorktileDeployNotifier(final List<String> environments, final String releaseName, final String releaseUrl) {
        setEnvironments(environments);
        setReleaseName(releaseName);
        setReleaseUrl(releaseUrl);
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

    public List<String> getEnvironments() {
        return environments;
    }

    @DataBoundSetter
    public void setEnvironments(final List<String> environments) {
        this.environments = new ArrayList<>(environments);
    }

    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Publisher> {
        public Descriptor() {
            super(WorktileDeployNotifier.class);
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
        public WorktileDeployNotifier newInstance(final StaplerRequest request, @NotNull final JSONObject formData) {
            assert request != null;
            return request.bindJSON(WorktileDeployNotifier.class, formData);
        }

        public FormValidation doCheckReleaseName(
                @QueryParameter(value = "releaseName", fixEmpty = true) String releaseName) {
            if (WorktileUtils.isBlank(releaseName))
                return FormValidation.error("release name can not be empty");
            return FormValidation.ok();
        }

        public ListBoxModel doFillEnvironmentsItems() {
            final ListBoxModel items = new ListBoxModel();
            items.add("hello", "world");
            return items;
        }
    }
}
