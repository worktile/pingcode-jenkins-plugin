package io.jenkins.plugins.worktile.service;

public class WorktileRestSession {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public WorktileRestSession(String token) {
        setToken(token);
    }

    // public static WorktileRestSession from() throws IOException {
    // WTGlobalConfiguration config = WTGlobalConfiguration.get();
    // String endpoint = config.getEndpoint();
    // }
}
