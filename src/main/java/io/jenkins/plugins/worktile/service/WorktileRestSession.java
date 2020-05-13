package io.jenkins.plugins.worktile.service;

import io.jenkins.plugins.worktile.WTEnvironment;
import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.model.*;

import java.io.IOException;

public class WorktileRestSession {
    public WorktileRestSession(String endpoint, String clientId, String clientSecret) {
        this.service = new WorktileRestService(endpoint, clientId, clientSecret);
    }

    public WorktileRestSession() {
        this(WTGlobalConfiguration.get().getEndpoint(), WTGlobalConfiguration.get().getClientId(),
                WTGlobalConfiguration.get().getClientSecret());
    }

    private final WorktileRestService service;

    public WTErrorEntity doConnectTest() throws IOException {
        return this.service.ping();
    }

    public WTErrorEntity createBuild(WTBuildEntity result) throws IOException {
        return this.service.createBuild(result);
    }

    public WTErrorEntity createEnvironment(WTEnvironment environment) throws IOException {
        return this.service.createEnvironment(environment);
    }

    public WTPaginationResponse<WTEnvSchema> listEnv() throws IOException, WTRestException {
        return this.service.listEnv();
    }

    public WTEnvSchema deleteEnv(String id) throws IOException, WTRestException {
        return this.service.deleteEnv(id);
    }

    public boolean createDeploy(WTDeployEntity entity) throws IOException, WTRestException {
        return this.service.createDeploy(entity);
    }
}
