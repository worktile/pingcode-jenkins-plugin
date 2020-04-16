package io.jenkins.plugins.worktile;

public class WorktileRestClient {
    public final String ENCODING = "utf-8";
    public final int CONNECT_TIMEOUT = 5000;
    public final int SOCKET_TIMEOUT = 5000;
    private String url;

    WorktileRestClient(String url) {
        setUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}