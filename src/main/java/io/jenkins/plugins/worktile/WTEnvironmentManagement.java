package io.jenkins.plugins.worktile;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.service.WTRestSession;

public class WTEnvironmentManagement extends AbstractDescribableImpl<WTEnvironmentManagement> {

    private String name;

    private String htmlUrl;

    private String id;

    public WTEnvironmentManagement(String id, String name, String htmlUrl) {
        setId(id);
        setName(name);
        setHtmlUrl(htmlUrl);
    }

    @DataBoundConstructor
    public WTEnvironmentManagement(String name, String htmlUrl) {
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
    public static class DescriptorImpl extends Descriptor<WTEnvironmentManagement> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "add worktile deploy environment";
        }

        public FormValidation doSave(@QueryParameter(value = "name", fixEmpty = true) String name,
                @QueryParameter(value = "htmlUrl", fixEmpty = true) String htmlUrl) {
            if (WTHelper.isBlank(name)) {
                return FormValidation.error("name can't not be empty");
            }

            final WTEnvironmentEntity env = new WTEnvironmentEntity(name, htmlUrl);

            try {
                final WTRestSession session = new WTRestSession();
                final WTEnvironmentSchema schema = session.createEnvironment(env);
                return FormValidation.ok(String.format("save environment %s ok", schema.name));
            } catch (Exception error) {
                return FormValidation.error("save environment error " + error.getMessage());
            }
        }
    }
}
