package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.service.WorktileRestSession;

public class WTEnvConfig extends AbstractDescribableImpl<WTEnvConfig> {

    private static final Logger logger = Logger.getLogger(WTEnvConfig.class.getName());

    private String name;

    private String htmlUrl;

    @DataBoundConstructor
    public WTEnvConfig(String name, String htmlUrl) {
        setName(name);
        setHtmlUrl(htmlUrl);
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String envName) {
        this.name = envName;
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return "deploy environments management";
        }

        public FormValidation doSave(@QueryParameter(value = "name", fixEmpty = true) String name,
                @QueryParameter(value = "htmlUrl", fixEmpty = true) String htmlUrl) {

            if (WorktileUtils.isBlank(name)) {
                return FormValidation.error("name can't not be empty");
            }
            final WTEnvironment env = new WTEnvironment(name, htmlUrl);
            try {
                final WorktileRestSession session = new WorktileRestSession();
                final WTErrorEntity error = session.createEnvironment(env);
                return error.getMessage() == null ? FormValidation.ok("save environment ok")
                        : FormValidation.error(error.getMessage());
            } catch (Exception error) {
                return FormValidation.error("save environment error " + error.getMessage());
            }
        }
    }
}
