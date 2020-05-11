package io.jenkins.plugins.worktile;

import java.util.HashMap;
import java.util.Map;

public class WTApiConnection {
    public static final int MAX_RETRIES = 5;
    public static final int DEFAULT_READ_TIMEOUT = 5;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5;
    public static final String DEFAULT_BASE_URL = "https://open.worktile.com";

    private final String clientId;
    private final String clientSecret;
    private final String accessToken;
    private final String userAgent;
    private final String baseUrl;
    private final int readTimeout;
    private final int connectionTimeout;
    private final Map<String, String> customHeaders;

    public WTApiConnection(String baseUrl, String clientId, String clientSecret, String accessToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
        this.userAgent = "JenkinsClient";
        this.customHeaders = new HashMap<String, String>();
        this.readTimeout = DEFAULT_READ_TIMEOUT;
        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        this.baseUrl = baseUrl;
    }

    public WTApiConnection(String accessToken) {
        this(null, null, null, accessToken);
    }

    public WTApiConnection(String clientId, String clientSecert) {
        this(null, clientId, clientSecert, null);
    }
}
