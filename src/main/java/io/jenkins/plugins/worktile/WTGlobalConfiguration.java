package io.jenkins.plugins.worktile;

import javax.annotation.Nonnull;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class WTGlobalConfiguration extends GlobalConfiguration {
    public static final String PRODUCT_ENDPOINT = "https://open.worktile.com";

    private String endpoint;
    private String clientId;
    private String secretKey;

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        this.endpoint = Util.fixEmptyAndTrim(endpoint);
    }

    @DataBoundSetter
    public void setSecretKey(String secretKey) {
        this.secretKey = Util.fixEmptyAndTrim(secretKey);
    }

    @DataBoundSetter
    public void setClientId(String clientId) {
        this.clientId = Util.fixEmptyAndTrim(clientId);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public WTGlobalConfiguration() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formatData) throws FormException {
        String endpoint = formatData.getString("endpoint");
        if (WorktileUtils.isBlank(endpoint)) {
            endpoint = WTGlobalConfiguration.PRODUCT_ENDPOINT;
        }
        setEndpoint(endpoint);
        setClientId(formatData.getString("clientId"));
        setSecretKey(formatData.getString("secretKey"));
        save();
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

    public FormValidation doCheckSecretKey(@QueryParameter(value = "clientId", fixEmpty = true) String secretkey) {
        return WorktileUtils.isNotBlank(secretkey) ? FormValidation.ok()
                : FormValidation.error("secret key can not be empty");
    }

    @Nonnull
    public static WTGlobalConfiguration get() {
        return (WTGlobalConfiguration) Jenkins.get().getDescriptorOrDie(WTGlobalConfiguration.class);
    }
}
