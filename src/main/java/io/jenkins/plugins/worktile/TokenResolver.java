package io.jenkins.plugins.worktile;

import java.io.IOException;

import io.jenkins.plugins.worktile.client.ApiConnection;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;

public class TokenResolver {
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

        return this.apiConnection.executeGet(path);
    }
}
