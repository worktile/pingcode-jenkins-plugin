package io.jenkins.plugins.worktile.service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hudson.XmlFile;
import io.jenkins.plugins.worktile.model.BuildResult;
import io.jenkins.plugins.worktile.model.WTError;
import io.jenkins.plugins.worktile.model.WorktileToken;
import jenkins.model.Jenkins;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WorktileRestService implements WorktileRestClient, WorktileTokenable {
    public static final Logger logger = Logger.getLogger(WorktileRestService.class.getName());

    private final String verPrefix = "v1";
    private OkHttpClient httpClient;
    private WorktileToken token;

    private final String ApiPath;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String endpoint;
    private String clientId;
    private String clientSecret;

    public WorktileRestService(String endpoint, String clientId, String clientSecret) {
        this.endpoint = endpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        this.setHttpClient(builder.build());

        StringBuilder apiBuilder = new StringBuilder(this.endpoint);
        if (endpoint.endsWith("/")) {
            apiBuilder.append(verPrefix);
        } else {
            apiBuilder.append("/" + verPrefix);
        }
        this.ApiPath = apiBuilder.toString();
    }

    public String getApiPath() {
        return ApiPath;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String createBuild(BuildResult result) throws IOException {
        WorktileToken token = this.getToken();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = this.gson.toJson(result);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(this.getApiPath() + "/build/builds")
                .addHeader("Authorization", "Bearer " + token.getAccessToken()).post(body).build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    public boolean ping() throws IOException {
        WorktileToken token = this.getToken();

        Request request = new Request.Builder().url(this.getApiPath() + "/auth/ping")
                .addHeader("Authorization", "Bearer " + token.getAccessToken()).build();
        try (Response response = this.httpClient.newCall(request).execute()) {
            WTError err = this.gson.fromJson(Objects.requireNonNull(response.body()).string(), WTError.class);
            return err != null;
        }
    }

    @Override
    public void createRelease() throws IOException {
    }

    @Override
    public WorktileToken getToken() throws IOException {
        if (this.token != null) {
            return this.token;
        }
        this.token = this.getTokenFromDisk();
        if (this.token == null) {
            this.token = this.getTokenFromApi();
        }
        return this.token;
    }

    @Override
    public void saveToken(WorktileToken token) throws IOException {
        XmlFile file = getConfigFile();
        try {
            file.write(token);
        } catch (Exception e) {
            logger.info("write token error" + e.getMessage());
        }
    }

    private WorktileToken getTokenFromApi() throws IOException {
        String path = String.format(
                this.getApiPath() + "/auth/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                this.clientId, this.clientSecret);

        Request request = new Request.Builder().url(path).build();
        try (Response response = this.httpClient.newCall(request).execute()) {
            this.token = gson.fromJson(Objects.requireNonNull(response.body()).string(), WorktileToken.class);
        }
        if (this.token != null) {
            this.saveToken(this.token);
        }
        return this.token;
    }

    private WorktileToken getTokenFromDisk() throws IOException {
        XmlFile file = getConfigFile();
        if (!file.exists()) {
            logger.warning("worktile token file not found");
            return null;
        }
        try {
            this.token = (WorktileToken) file.unmarshal(this.token);
        } catch (Exception error) {
            logger.warning("file.unmarshal to this.token error = " + error.getMessage());
        }

        if (this.token != null) {
            if (System.currentTimeMillis() / 1000 > this.token.getExpiresIn()) {
                logger.info("token in cache file is out of date, the token will be set null");
                this.token = null;
            }
        }
        return this.token;
    }

    private XmlFile getConfigFile() {
        File file = new File(Objects.requireNonNull(Jenkins.getInstanceOrNull()).getRootDir(), "worktile.token.xml");
        return new XmlFile(file);
    }
}
