package io.jenkins.plugins.worktile;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class WTGlobalConfiguration extends GlobalConfiguration {
    public static final String PRODUCT_ENDPOINT = "https://open.worktile.com";
    public static final Logger logger = Logger.getLogger(WTGlobalConfiguration.class.getName());

    private String endpoint;
    private String clientId;
    private String clientSecret;

    public String getDefaultEndpont() {
        return WTGlobalConfiguration.PRODUCT_ENDPOINT;
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
            endpoint = WTGlobalConfiguration.PRODUCT_ENDPOINT;
        }
        String clientId = formatData.getString("clientId");
        String clientSecret = formatData.getString("clientSecret");

        setEndpoint(endpoint);
        setClientId(clientId);
        setClientSecret(clientSecret);

        WTGlobalConfiguration.logger.info(endpoint);
        WTGlobalConfiguration.logger.info(clientId);
        WTGlobalConfiguration.logger.info(clientSecret);

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

    public FormValidation doCheckClientSecret(@QueryParameter(value = "clientId", fixEmpty = true) String secretkey) {
        return WorktileUtils.isNotBlank(secretkey) ? FormValidation.ok()
                : FormValidation.error("secret key can not be empty");
    }

    public FormValidation doTestConnection(@QueryParameter(value = "endpoint", fixEmpty = true) String endpoint,
            @QueryParameter(value = "clientId", fixEmpty = true) String clientId,
            @QueryParameter(value = "clientSecret", fixEmpty = true) String clientSecret) throws IOException {

        String apiUrl = endpoint + "/auth/token" + "?grant_type=client_credentials" + "&client_id=" + clientId
                + "&client_secret=" + clientSecret;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(apiUrl).build();
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() ? FormValidation.ok("TestConnection Successful")
                    : FormValidation.error("connect api end error");
        }
    }

    @Nonnull
    public static WTGlobalConfiguration get() {
        return (WTGlobalConfiguration) Jenkins.get().getDescriptorOrDie(WTGlobalConfiguration.class);
    }
}