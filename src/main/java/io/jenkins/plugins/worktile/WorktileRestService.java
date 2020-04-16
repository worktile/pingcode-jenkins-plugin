package io.jenkins.plugins.worktile;

public class WorktileRestService {
    public static final String API_VER = "v1";
    private String url;
    private String apiKey;
    private String secretKey;

    public WorktileRestService(String baseUrl, String apiKey, String secretKey) {
        setUrl(baseUrl);
        setApiKey(apiKey);
        setSecretKey(secretKey);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}