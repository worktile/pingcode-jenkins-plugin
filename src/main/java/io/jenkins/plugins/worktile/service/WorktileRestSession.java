package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTGlobalConfiguration;
import io.jenkins.plugins.worktile.model.BuildResult;
import io.jenkins.plugins.worktile.model.WTError;

public class WorktileRestSession {
    public WorktileRestSession(String endpoint, String clientId, String clientSecret) {
        this.service = new WorktileRestService(endpoint, clientId, clientSecret);
    }

    public WorktileRestSession() {
        this(WTGlobalConfiguration.get().getEndpoint(), WTGlobalConfiguration.get().getClientId(),
                WTGlobalConfiguration.get().getClientSecret());
    }

    private final WorktileRestService service;

    public WTError doConnectTest() throws IOException {
        return this.service.ping();
    }

    public WTError createBuild(BuildResult result) throws IOException {
        return this.service.createBuild(result);
    }
}
