package io.jenkins.plugins.worktile;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.resolver.SecretResolver;
import io.jenkins.plugins.worktile.service.WTRestService;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@Extension
public class WTGlobalConfiguration extends GlobalConfiguration {
    public static final String DEFAULT_ENDPOINT = "https://open.worktile.com";

    public static final String WORKTILE_GLOBAL_CONFIG_ID = "worktile-global-configuration";

    public static final Logger logger = Logger.getLogger(WTGlobalConfiguration.class.getName());

    private String endpoint;
    private String clientId;
    private String credentialsId;

    public WTGlobalConfiguration() {
        load();
    }

    @Nonnull
    public static WTGlobalConfiguration get() {
        return (WTGlobalConfiguration) Jenkins.get().getDescriptorOrDie(WTGlobalConfiguration.class);
    }

    public String getDefaultEndpoint() {
        return WTGlobalConfiguration.DEFAULT_ENDPOINT;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
    }

    public String getClientId() {
        return clientId;
    }

    @DataBoundSetter
    public void setClientId(String clientId) {
        this.clientId = Util.fixEmptyAndTrim(clientId);
    }

    public String getEndpoint() {
        return endpoint;
    }

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        this.endpoint = Util.fixEmptyAndTrim(endpoint);
    }

    @Override
    public String getId() {
        return WORKTILE_GLOBAL_CONFIG_ID;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formatData) throws FormException {
        try {
            req.bindJSON(this, formatData);
        } catch (Exception e) {
            throw new FormException(e.getMessage(), e, Messages.WTGlobalConfig_GlobalConfigError());
        }
        save();
        return true;
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckEndpoint(@QueryParameter(value = "endpoint", fixEmpty = true) String endpoint) {
        if (WTHelper.isNotBlank(endpoint) && !WTHelper.isURL(endpoint)) {
            return FormValidation.error(Messages.WTGlobalConfig_OpenApiEndpointError());
        }
        return FormValidation.ok();
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckClientId(@QueryParameter(value = "clientId", fixEmpty = true) String clientId) {
        return WTHelper.isNotBlank(clientId) ? FormValidation.ok()
                : FormValidation.error(Messages.WTGlobalConfig_ClientIdError());
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckCredentialsId(
            @QueryParameter(value = "credentialsId", fixEmpty = true) String credentialsId) {
        return WTHelper.isNotBlank(credentialsId) ? FormValidation.ok()
                : FormValidation.error(Messages.WTGlobalConfig_CredentialsIdEmpty());
    }

    @POST
    @SuppressWarnings("unused")
    @Restricted(DoNotUse.class)
    public FormValidation doTestConnection(@QueryParameter(value = "endpoint", fixEmpty = true) String endpoint,
            @QueryParameter(value = "clientId", fixEmpty = true) String clientId,
            @QueryParameter(value = "credentialsId", fixEmpty = true) String credentialsId) throws IOException {

        // Check permission, only ADMINISTER
        Jenkins.get().hasPermission(Jenkins.ADMINISTER);

        if (StringUtils.isEmpty(credentialsId) || StringUtils.isEmpty(endpoint) || StringUtils.isEmpty(clientId)) {
            return FormValidation.error(Messages.WTGlobalConfig_AnyOfIdError());
        }

        Optional<String> secret = SecretResolver.getSecretOf(credentialsId);
        if (!secret.isPresent()) {
            return FormValidation.error(Messages.WTGlobalConfig_CredentialsIdNotSelectOrError());
        }
        WTRestService service = new WTRestService(WTHelper.apiV1(endpoint), clientId, secret.get());

        try {
            service.doConnectTest();
            return FormValidation.ok(Messages.WTGlobalConfig_DoTestConnectionSuccessfully());
        } catch (WTRestException e) {
            return FormValidation.error(Messages.WTGlobalConfig_DoTestConnectionFailure() + ": "
                    + Messages.WTGlobalConfig_ClientIdOrClientSecretError());
        } catch (Exception e) {
            return FormValidation.error(Messages.WTGlobalConfig_DoTestConnectionFailure() + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillCredentialsIdItems(@QueryParameter final String endpoint,
            @QueryParameter final String clientId, @QueryParameter final String credentialsId) {

        Jenkins jenkins = Jenkins.get();
        if (!jenkins.hasPermission(Jenkins.ADMINISTER)) {
            return new StandardListBoxModel().includeCurrentValue(credentialsId);
        }

        return new StandardListBoxModel().includeEmptyValue().includeMatchingAs(ACL.SYSTEM, Jenkins.get(),
                StringCredentials.class,
                URIRequirementBuilder.fromUri(StringUtils.defaultIfBlank(endpoint, DEFAULT_ENDPOINT)).build(),
                CredentialsMatchers.always());
    }
}
