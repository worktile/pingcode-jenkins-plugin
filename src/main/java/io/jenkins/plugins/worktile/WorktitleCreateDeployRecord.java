package io.jenkins.plugins.worktile;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;

public class WorktitleCreateDeployRecord extends Notifier {

    private String environmentName;

    @DataBoundConstructor
    public WorktitleCreateDeployRecord(String envName) {
        setEnvironmentName(envName);
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Publisher> {

        public Descriptor() {
            super(WorktitleCreateDeployRecord.class);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "worktile create deploy record";
        }

        @Override
        public WorktitleCreateDeployRecord newInstance(StaplerRequest request, JSONObject formdata) {
            return request.bindJSON(WorktitleCreateDeployRecord.class, formdata);
        }
    }
}