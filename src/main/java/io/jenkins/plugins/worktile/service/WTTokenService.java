package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.client.TokenClient;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;
import io.jenkins.plugins.worktile.resolver.TokenResolver;

public class WTTokenService implements TokenClient {

    private TokenResolver tokenResolver;

    public WTTokenService(String baseURL, String clientId, String clientSecret) {
        this.tokenResolver = new TokenResolver(baseURL, clientId, clientSecret);
    }

    @Override
    public WTTokenEntity getTokenFromApi() throws IOException, WTRestException {
        return this.tokenResolver.resolveToken();
    }
}
