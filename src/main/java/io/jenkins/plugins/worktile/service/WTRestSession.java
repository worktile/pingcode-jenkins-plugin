package io.jenkins.plugins.worktile.service;

import io.jenkins.plugins.worktile.MemoryTokenCache;
import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.model.*;
import io.jenkins.plugins.worktile.resolver.SecretResolver;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class WTRestSession {
  private final Logger log = Logger.getLogger(WTRestService.class.getName());

  private final WTTokenService tokenService;
  private final String clientId;
  private final String clientSecret;
  private final String baseURL;

  public WTRestSession() {
    this(
        WTHelper.apiV1(WTGlobalConfiguration.get().getEndpoint()),
        WTGlobalConfiguration.get().getClientId(),
        SecretResolver.getSecretOf(WTGlobalConfiguration.get().getCredentialsId()).get());
  }

  public WTRestSession(String baseURL, String clientId, String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.baseURL = baseURL;

    this.tokenService = new WTTokenService(baseURL, clientId, clientSecret);
  }

  public WTTokenEntity doConnectTest() throws IOException, WTRestException {
    return tokenService.getTokenFromApi();
  }

  public Object createBuild(WTBuildEntity entity) throws IOException, WTRestException {
    return this.getWTRestService().createBuild(entity);
  }

  private WTRestService getWTRestService() {
    WTTokenEntity token = MemoryTokenCache.get(clientId, clientSecret);
    if (token == null || token.isExpired()) {
      try {
        token = tokenService.getTokenFromApi();
        boolean putret = MemoryTokenCache.put(clientId, clientSecret, token);
        log.info("[INFO]: put token " + putret);
      } catch (Exception e) {
        log.warning("[ERROR]: get token from api" + e.getMessage());
      }
    }
    return new WTRestService(baseURL, Objects.requireNonNull(token).accessToken);
  }

  public Object createDeploy(WTDeployEntity entity) throws IOException, WTRestException {
    return this.getWTRestService().createDeploy(entity);
  }

  public WTPaginationResponse<WTEnvironmentSchema> listEnvironments()
      throws IOException, WTRestException {
    return this.getWTRestService().listEnvironments();
  }

  public WTEnvironmentSchema getEnvironmentByName(String name) throws IOException, WTRestException {
    return this.getWTRestService().getEnvironmentByName(name);
  }

  public WTEnvironmentSchema deleteEnvironment(String id) throws IOException, WTRestException {
    return this.getWTRestService().deleteEnvironment(id);
  }

  public WTEnvironmentSchema createEnvironment(WTEnvironmentEntity entity)
      throws IOException, WTRestException {
    return this.getWTRestService().createEnvironment(entity);
  }
}
