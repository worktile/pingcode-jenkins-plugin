package io.jenkins.plugins.worktile.service;

import java.io.IOException;

public class WorktileRestSession {
    public WorktileRestSession(String endpoint, String clientId, String clientSecret) {
        this.service = new WorktileRestService(endpoint, clientId, clientSecret);
    }

    private final WorktileRestService service;

    public boolean doConnectTest() throws IOException {
        return this.service.ping();
    }
}
