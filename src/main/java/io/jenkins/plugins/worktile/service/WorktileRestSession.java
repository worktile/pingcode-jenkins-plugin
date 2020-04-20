package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.model.BuildResult;

public class WorktileRestSession {
    public WorktileRestSession(String endpoint, String clientId, String clientSecret) {
        this.service = new WorktileRestService(endpoint, clientId, clientSecret);
    }

    public WorktileRestSession() {
        this(WTGlobalConfiguration.get().getEndpoint(), WTGlobalConfiguration.get().getClientId(),
                WTGlobalConfiguration.get().getClientSecret());
    }

    private final WorktileRestService service;

    public boolean doConnectTest() throws IOException {
        return this.service.ping();
    }

    public String createBuild(BuildResult result) throws IOException {
        return this.service.createBuild(result);
    }
}
