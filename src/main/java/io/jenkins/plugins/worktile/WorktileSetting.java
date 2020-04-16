package io.jenkins.plugins.worktile;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class WorktileSetting {
    public final static String DEFAULT_URL = "https://open.worktile.com/";

    @DataBoundConstructor
    public WorktileSetting(String url, String accessKey, String secretKey) {
        setUrl(url);
        setAccessKey(accessKey);
        setSecretKey(secretKey);
    }

    private String url;

    private String accessKey;

    private String secretKey;

    public String getUrl() {
        return url;
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecretKey() {
        return secretKey;
    }

    @DataBoundSetter
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    @DataBoundSetter
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

}