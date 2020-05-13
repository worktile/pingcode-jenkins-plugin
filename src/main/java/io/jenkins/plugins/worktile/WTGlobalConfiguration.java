package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.service.WorktileRestSession;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class WTGlobalConfiguration extends GlobalConfiguration {
    public static final String DEFAULT_ENDPOINT = "https://open.worktile.com";

    public static final Logger logger = Logger.getLogger(WTGlobalConfiguration.class.getName());

    private String endpoint;
    private String clientId;
    private String credentialsId;

    private transient List<WTEnvConfig> envConfigs;

    public String getDefaultEndpoint() {
        return WTGlobalConfiguration.DEFAULT_ENDPOINT;
    }

    public List<WTEnvConfig> getEnvConfigs() {
        return envConfigs;
    }

    @DataBoundSetter
    public void setEnvConfigs(List<WTEnvConfig> envConfigs) {
        this.envConfigs = envConfigs;
    }

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        this.endpoint = Util.fixEmptyAndTrim(endpoint);
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
    }

    @DataBoundSetter
    public void setClientId(String clientId) {
        this.clientId = Util.fixEmptyAndTrim(clientId);
    }

    public String getClientSecret() {
        return credentialsId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public WTGlobalConfiguration() {
        load();
        this.envConfigs = new ArrayList<>();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formatData) throws FormException {
        try {
            req.bindJSON(this, formatData);
        } catch (Exception e) {
            throw new FormException(e.getMessage(), e, "globalConfig");
        }
        save();
        WorktileUtils.RemoveTokenFile();
        return true;
    }

    public FormValidation doCheckEndpoint(@QueryParameter(value = "endpoint", fixEmpty = true) String endpoint) {
        if (WorktileUtils.isNotBlank(endpoint) && !WorktileUtils.isURL(endpoint)) {
            return FormValidation.error("endpoint format error");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckClientId(@QueryParameter(value = "clientId", fixEmpty = true) String clientId) {
        return WorktileUtils.isNotBlank(clientId) ? FormValidation.ok()
                : FormValidation.error("client id can not be empty");
    }

    public FormValidation doCheckCredentialsId(
            @QueryParameter(value = "credentialsId", fixEmpty = true) String credentialsId) {
        return WorktileUtils.isNotBlank(credentialsId) ? FormValidation.ok()
                : FormValidation.error("credentialsId can not be empty");
    }

    public FormValidation doTestConnection(@QueryParameter(value = "endpoint", fixEmpty = true) String endpoint,
            @QueryParameter(value = "clientId", fixEmpty = true) String clientId,
            @QueryParameter(value = "credentialsId", fixEmpty = true) String credentialsId) throws IOException {

        Optional<String> secret = SecretResolver.getSecretOf(credentialsId);
        if (!secret.isPresent()) {
            return FormValidation.error("secret not found or wrong");
        }

        WorktileRestSession session = new WorktileRestSession(endpoint, clientId, secret.get());

        try {
            WTErrorEntity err = session.doConnectTest();
            if (err.getMessage() == null) {
                save();
                return FormValidation.ok("Connect Worktile API Successfully");
            }
            return FormValidation.error(err.getMessage());
        } catch (Exception e) {
            WTGlobalConfiguration.logger.warning("test connect error");
            return FormValidation.error("Connect Worktile API Error, err : " + e.getMessage());
        }
    }

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter final String endpoint,
            @QueryParameter final String clientId, @QueryParameter final String credentialsId) {

        return new StandardListBoxModel().includeEmptyValue().includeMatchingAs(ACL.SYSTEM, Jenkins.get(),
                StringCredentials.class,
                URIRequirementBuilder.fromUri(StringUtils.defaultIfBlank(endpoint, DEFAULT_ENDPOINT)).build(),
                CredentialsMatchers.always());
    }

    @Nonnull
    public static WTGlobalConfiguration get() {
        return (WTGlobalConfiguration) Jenkins.get().getDescriptorOrDie(WTGlobalConfiguration.class);
    }
}
