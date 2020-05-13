package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvEntity;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;
import io.jenkins.plugins.worktile.resolver.TokenResolver;

public class WorktileRestSession {
    private final WorktileRestService service;

    private final TokenResolver tokenResolver;

    public WorktileRestSession(String endpoint, String clientId, String clientSecret) {
        this.service = new WorktileRestService(endpoint, clientId, clientSecret);
        this.tokenResolver = new TokenResolver(endpoint, clientId, clientSecret);
    }

    public WorktileRestSession() {
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

    public WTPaginationResponse<WTEnvSchema> listEnvironments() throws IOException, WTRestException {
        return this.service.listEnvironments();
    }

    public WTEnvSchema deleteEnvironment(String id) throws IOException, WTRestException {
        return this.service.deleteEnvironment(id);
    }

    public WTEnvSchema createEnvironment(WTEnvEntity entity) throws IOException, WTRestException {
        return this.service.createEnvironment(entity);
    }
}
