package io.jenkins.plugins.worktile;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class WTEnvConfig extends AbstractDescribableImpl<WTEnvConfig> {
    private String envName;

    private String htmlUrl;

    @DataBoundConstructor
    public WTEnvConfig(String envName, String htmlUrl) {
        setEnvName(envName);
        setHtmlUrl(htmlUrl);
    }

    public String getEnvName() {
        return envName;
    }

    @DataBoundSetter
    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @DataBoundSetter
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<WTEnvConfig> {

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
