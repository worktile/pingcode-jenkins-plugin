package io.jenkins.plugins.worktile.service;

import io.jenkins.plugins.worktile.MemoryTokenStore;
import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.model.*;
import io.jenkins.plugins.worktile.resolver.SecretResolver;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class WTRestService {
  private final Logger log = Logger.getLogger(WTRestApiService.class.getName());

  private final WTTokenService tokenService;
  private final String clientId;
  private final String clientSecret;
  private final String baseURL;

  public WTRestService() {
    this(
        WTHelper.apiV1(WTGlobalConfiguration.get().getEndpoint()),
        WTGlobalConfiguration.get().getClientId(),
        SecretResolver.getSecretOf(WTGlobalConfiguration.get().getCredentialsId()).get());
  }

  public WTRestService(String baseURL, String clientId, String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.baseURL = baseURL;

    this.tokenService = new WTTokenService(baseURL, clientId, clientSecret);
  }

  public void doConnectTest() throws IOException, WTRestException {
    tokenService.getTokenFromApi();
  }

  public void createBuild(WTBuildEntity entity) throws IOException, WTRestException {
    this.getWTRestService().createBuild(entity);
  }

  private WTRestApiService getWTRestService() {
    WTTokenEntity token = MemoryTokenStore.get(clientId, clientSecret);
    if (token == null || token.isExpired()) {
      try {
        token = tokenService.getTokenFromApi();
        boolean putResult = MemoryTokenStore.put(clientId, clientSecret, token);
        log.info("[INFO]: put token " + putResult);
      } catch (Exception e) {
        log.warning("[ERROR]: get token from api error " + e.getMessage());
      }
    }
    return new WTRestApiService(baseURL, Objects.requireNonNull(token).accessToken);
  }

  public void createDeploy(WTDeployEntity entity) throws IOException, WTRestException {
    this.getWTRestService().createDeploy(entity);
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
