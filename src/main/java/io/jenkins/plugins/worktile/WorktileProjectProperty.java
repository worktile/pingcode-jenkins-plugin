package io.jenkins.plugins.worktile;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;

import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

public class WorktileProjectProperty extends JobProperty<Job<?, ?>> {
    private String endpoint;
    private String accessKey;
    private String secretKey;

    @DataBoundConstructor
    public WorktileProjectProperty(String endpoint) {
        setEndpoint(endpoint);
    }

    public String getSecretKey() {
        return secretKey;
    }

    @DataBoundSetter
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    @DataBoundSetter
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean testConnect() {
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends Job> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "who ami";
        }
    }
}