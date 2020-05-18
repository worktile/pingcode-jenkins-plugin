package io.jenkins.plugins.worktile.service;

import io.jenkins.plugins.worktile.client.TokenClient;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;
import io.jenkins.plugins.worktile.resolver.TokenResolver;

import java.io.IOException;

public class WTTokenService implements TokenClient {

  private final TokenResolver tokenResolver;

  public WTTokenService(String baseURL, String clientId, String clientSecret) {
    this.tokenResolver = new TokenResolver(baseURL, clientId, clientSecret);
  }

  @Override
  public WTTokenEntity getTokenFromApi() throws IOException, WTRestException {
    return this.tokenResolver.resolveToken();
  }
}
