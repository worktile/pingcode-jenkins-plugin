package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

public class WorktileDeployNotifier extends Notifier {

    private String environmentName;

    private boolean relatedWorkItem;

    @DataBoundConstructor
    public WorktileDeployNotifier(String envName) {
        setEnvironmentName(envName);
    }

    public boolean isRelatedWorkItem() {
        return relatedWorkItem;
    }

    @DataBoundSetter
    public void setRelatedWorkItem(boolean relatedWorkItem) {
        this.relatedWorkItem = relatedWorkItem;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    @DataBoundSetter
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Publisher> {

        public Descriptor() {
            super(WorktileDeployNotifier.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "worktile deploy step";
        }

        @Override
        public WorktileDeployNotifier newInstance(StaplerRequest request, JSONObject formData) {
            assert request != null;
            return request.bindJSON(WorktileDeployNotifier.class, formData);
        }
    }
}
