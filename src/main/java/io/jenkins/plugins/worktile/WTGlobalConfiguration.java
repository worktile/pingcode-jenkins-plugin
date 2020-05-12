package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.service.WorktileRestSession;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class WTGlobalConfiguration extends GlobalConfiguration {
    public static final String DEFAULT_ENDPOINT = "https://open.worktile.com";
    public static final Logger logger = Logger.getLogger(WTGlobalConfiguration.class.getName());

    private String endpoint;
    private String clientId;
    private String clientSecret;

    private transient WTEnvConfig evnConfig;
    private List<WTEnvConfig> envConfigs;

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

    public WTEnvConfig getEvnConfig() {
        return evnConfig;
    }

    public void setEvnConfig(WTEnvConfig evnConfig) {
        this.evnConfig = evnConfig;
    }

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        this.endpoint = Util.fixEmptyAndTrim(endpoint);
    }

    @DataBoundSetter
    public void setClientSecret(String clientSecret) {
        this.clientSecret = Util.fixEmptyAndTrim(clientSecret);
    }

    @DataBoundSetter
    public void setClientId(String clientId) {
        this.clientId = Util.fixEmptyAndTrim(clientId);
    }

    public String getClientSecret() {
        return clientSecret;
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
            endpoint = WTGlobalConfiguration.DEFAULT_ENDPOINT;
        }
        String clientId = formatData.getString("clientId");
        String clientSecret = formatData.getString("clientSecret");
        setEndpoint(endpoint);
        setClientId(clientId);
        setClientSecret(clientSecret);
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

    public FormValidation doCheckClientSecret(@QueryParameter(value = "clientId", fixEmpty = true) String secretkey) {
        return WorktileUtils.isNotBlank(secretkey) ? FormValidation.ok()
                : FormValidation.error("secret key can not be empty");
    }

    public FormValidation doTestConnection(@QueryParameter(value = "endpoint", fixEmpty = true) String endpoint,
            @QueryParameter(value = "clientId", fixEmpty = true) String clientId,
            @QueryParameter(value = "clientSecret", fixEmpty = true) String clientSecret) throws IOException {

        WorktileRestSession session = new WorktileRestSession(endpoint, clientId, clientSecret);

        try {
            WTErrorEntity err = session.doConnectTest();
            return err.getMessage() == null ? FormValidation.ok("Connect Worktile API Successfully")
                    : FormValidation.error(err.getMessage());
        } catch (Exception e) {
            WTGlobalConfiguration.logger.warning("test connect error");
            return FormValidation.error("Connect Worktile API Error, err : " + e.getMessage());
        }

    }

    @Nonnull
    public static WTGlobalConfiguration get() {
        return (WTGlobalConfiguration) Jenkins.get().getDescriptorOrDie(WTGlobalConfiguration.class);
    }
}
