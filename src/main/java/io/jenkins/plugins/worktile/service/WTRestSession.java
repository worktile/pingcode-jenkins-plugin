package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;
import io.jenkins.plugins.worktile.resolver.TokenResolver;

public class WTRestSession {
    private final WTRestService service;

    private final TokenResolver tokenResolver;

    public WTRestSession(String endpoint, String clientId, String clientSecret) {
        this.service = new WTRestService(endpoint, clientId, clientSecret);
        this.tokenResolver = new TokenResolver(endpoint, clientId, clientSecret);
    }

    public WTRestSession() {
        this(WTGlobalConfiguration.get().getEndpoint(), WTGlobalConfiguration.get().getClientId(),
                WTGlobalConfiguration.get().getClientSecret());
    }

    public WTTokenEntity doConnectTest() throws IOException, WTRestException {
        return tokenResolver.resolveToken();
    }

    public Object createBuild(WTBuildEntity entity) throws IOException, WTRestException {
        return this.service.createBuild(entity);
    }

    public Object createDeploy(WTDeployEntity entity) throws IOException, WTRestException {
        return this.service.createDeploy(entity);
    }

    public WTPaginationResponse<WTEnvironmentSchema> listEnvironments() throws IOException, WTRestException {
        return this.service.listEnvironments();
    }

    public WTEnvironmentSchema deleteEnvironment(String id) throws IOException, WTRestException {
        return this.service.deleteEnvironment(id);
    }

    public WTEnvironmentSchema createEnvironment(WTEnvironmentEntity entity) throws IOException, WTRestException {
        return this.service.createEnvironment(entity);
    }
}
