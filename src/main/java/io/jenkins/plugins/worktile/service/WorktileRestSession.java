package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTEnvironment;
import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTErrorEntity;

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
}
