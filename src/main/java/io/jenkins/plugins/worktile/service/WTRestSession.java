package io.jenkins.plugins.worktile.service;

import java.io.IOException;
import java.util.logging.Logger;

import io.jenkins.plugins.worktile.MemoryTokenCache;
import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;
import io.jenkins.plugins.worktile.resolver.SecretResolver;

public class WTRestSession {
    private final Logger log = Logger.getLogger(WTRestService.class.getName());

    private final WTTokenService tokenService;
    private String clientId;
    private String clientSecret;
    private String baseURL;

    public WTRestSession(String baseURL, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseURL = baseURL;

        this.tokenService = new WTTokenService(baseURL, clientId, clientSecret);
    }

    public WTRestSession() {
        this(WTHelper.apiV1(WTGlobalConfiguration.get().getEndpoint()), WTGlobalConfiguration.get().getClientId(),
                SecretResolver.getSecretOf(WTGlobalConfiguration.get().getClientSecret()).get());
    }

    private WTRestService getWTRestService() {
        WTTokenEntity token = MemoryTokenCache.get(clientId, clientSecret);
        if (token == null || token.isExpired()) {
            try {
                token = tokenService.getTokenFromApi();
                boolean putret = MemoryTokenCache.put(clientId, clientSecret, token);
                log.info("[INFO]: put token " + putret);
            } catch (Exception e) {
                log.warning("[ERROR] => : get token from api" + e.getMessage());
            }
        }
        return new WTRestService(baseURL, token.accessToken);
    }

    public WTTokenEntity doConnectTest() throws IOException, WTRestException {
        return tokenService.getTokenFromApi();
    }

    public Object createBuild(WTBuildEntity entity) throws IOException, WTRestException {
        return this.getWTRestService().createBuild(entity);
    }

    public Object createDeploy(WTDeployEntity entity) throws IOException, WTRestException {
        return this.getWTRestService().createDeploy(entity);
    }

    public WTPaginationResponse<WTEnvironmentSchema> listEnvironments() throws IOException, WTRestException {
        return this.getWTRestService().listEnvironments();
    }

    public WTEnvironmentSchema deleteEnvironment(String id) throws IOException, WTRestException {
        return this.getWTRestService().deleteEnvironment(id);
    }

    public WTEnvironmentSchema createEnvironment(WTEnvironmentEntity entity) throws IOException, WTRestException {
        return this.getWTRestService().createEnvironment(entity);
    }
}
