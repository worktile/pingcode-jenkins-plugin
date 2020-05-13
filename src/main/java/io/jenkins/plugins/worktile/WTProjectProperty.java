package io.jenkins.plugins.worktile;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.worktile.model.WTEnvEntity;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.service.WorktileRestSession;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

public class WTProjectProperty extends JobProperty<Job<?, ?>> {
    private String name;
    private String htmlUrl;

    @DataBoundConstructor
    public WTProjectProperty(final String name, final String htmlUrl) {
        setName(name);
        setHtmlUrl(htmlUrl);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @DataBoundSetter
    public void setHtmlUrl(final String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {
        public final Logger logger = Logger.getLogger(WTProjectProperty.class.getName());

        @Override
        public boolean isApplicable(final Class<? extends Job> jobType) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Worktile environment sync";
        }

        public FormValidation doSyncEnv(@QueryParameter(value = "name", fixEmpty = true) final String name,
                @QueryParameter(value = "htmlUrl", fixEmpty = true) final String htmlUrl) {
            if (WorktileUtils.isBlank(name)) {
                return FormValidation.error("name can't not be empty");
            }
            final WTEnvEntity env = new WTEnvEntity(name, htmlUrl);
            final WorktileRestSession session = new WorktileRestSession();
            try {
                WTEnvSchema envSchema = session.createEnvironment(env);
                return FormValidation.ok(String.format("create %s successfully", envSchema.name));
            } catch (final Exception error) {
                return FormValidation.error("Sync environment error " + error.getMessage());
            }
        }
    }
}
