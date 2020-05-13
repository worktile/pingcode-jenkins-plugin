package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.worktile.model.WTEnvEntity;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.service.WorktileRestSession;

public class WTEnvConfig extends AbstractDescribableImpl<WTEnvConfig> {

    private String name;

    private String htmlUrl;

    private String id;

    public WTEnvConfig(String id, String name, String htmlUrl) {
        setId(id);
        setName(name);
        setHtmlUrl(htmlUrl);
    }

    @DataBoundConstructor
    public WTEnvConfig(String name, String htmlUrl) {
        this(null, name, htmlUrl);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
            return "add worktile deploy environment";
        }

        public FormValidation doSave(@QueryParameter(value = "name", fixEmpty = true) String name,
                @QueryParameter(value = "htmlUrl", fixEmpty = true) String htmlUrl) {
            if (WorktileUtils.isBlank(name)) {
                return FormValidation.error("name can't not be empty");
            }

            final WTEnvEntity env = new WTEnvEntity(name, htmlUrl);

            try {
                final WorktileRestSession session = new WorktileRestSession();
                final WTEnvSchema error = session.createEnvironment(env);
                return FormValidation.ok("save environment ok");
            } catch (Exception error) {
                return FormValidation.error("save environment error " + error.getMessage());
            }
        }
    }
}
