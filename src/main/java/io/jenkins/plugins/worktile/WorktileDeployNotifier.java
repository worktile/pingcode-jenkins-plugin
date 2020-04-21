package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

public class WorktileDeployNotifier extends Notifier {

    private List<String> environments;

    @DataBoundConstructor
    public WorktileDeployNotifier(List<String> environments) {
       setEnvironments(environments);
    }

    public List<String> getEnvironments() {
        return environments;
    }

    @DataBoundSetter
    public void setEnvironments(List<String> environments) {
        this.environments = new ArrayList<>(environments);
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
            return "Worktile deploy notifier";
        }

        @Override
        public WorktileDeployNotifier newInstance(StaplerRequest request, @NotNull JSONObject formData) {
            assert request != null;
            return request.bindJSON(WorktileDeployNotifier.class, formData);
        }
    }
}
