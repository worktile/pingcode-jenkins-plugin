package io.jenkins.plugins.worktile.model;

public class WorktileToken {
    private String accessToken;
    private String tokenType;
    private long expiresIn;

    public WorktileToken(String accessToken, String tokenType, long expiresIn) {
        setAccessToken(accessToken);
        setTokenType(tokenType);
        setExpiresIn(expiresIn);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
