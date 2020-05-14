package io.jenkins.plugins.worktile.resolver;

import java.io.IOException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.log4j.Logger;

import io.jenkins.plugins.worktile.client.ApiConnection;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;

public class TokenResolver {
    private final Logger logger = Logger.getLogger(TokenResolver.class.getName());

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String clientId;
    private String clientSecret;
    private String baseURL;
    private final ApiConnection apiConnection = new ApiConnection();

    public TokenResolver(String baseURL, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseURL = baseURL;
    }

    public WTTokenEntity resolveToken() throws IOException, WTRestException {

        String path = String.format(
                this.baseURL + "/auth/token?grant_type=client_credentials&client_id=%s&client_secret=%s", this.clientId,
                this.clientSecret);

        String json = this.apiConnection.executeGet(path);
        return gson.fromJson(json, WTTokenEntity.class);
    }
}
