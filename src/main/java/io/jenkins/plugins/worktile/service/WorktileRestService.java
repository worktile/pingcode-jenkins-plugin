package io.jenkins.plugins.worktile.service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import hudson.XmlFile;
import io.jenkins.plugins.worktile.WTEnvironment;
import io.jenkins.plugins.worktile.WorktileUtils;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;
import jenkins.model.Jenkins;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

// TODO: refact executeGet, executePost
public class WorktileRestService implements WorktileRestClient, WorktileTokenable {
    public static final Logger logger = Logger.getLogger(WorktileRestService.class.getName());

    private final String verPrefix = "v1";
    private OkHttpClient httpClient;
    private WTTokenEntity token;

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
    public WTErrorEntity createBuild(WTBuildEntity entity) throws IOException {
        String path = this.getApiPath() + "/build/builds";
        // String path = "https://request.worktile.com/cjynOjmRG";
        return this.executePost(path, entity, true);
    }

    @Override
    public WTErrorEntity createEnvironment(WTEnvironment environment) throws IOException {
        String path = this.getApiPath() + "/release/environments";
        return this.executePost(path, environment, true);
    }

    @Override
    public WTPaginationResponse<WTEnvSchema> listEnv() throws IOException, WTRestException {
        String path = this.getApiPath() + "/release/environments?page_index=0&page_size=100";

        Builder requestBuilder = new Request.Builder().url(path);
        WTTokenEntity token = this.getToken();
        requestBuilder.addHeader("Authorization", "Bearer " + token.getAccessToken());

        try (Response response = this.httpClient.newCall(requestBuilder.build()).execute()) {
            String jsonString = Objects.requireNonNull(response.body()).string();
            tryThrowWTRestException(jsonString);
            return this.gson.fromJson(jsonString, new TypeToken<WTPaginationResponse<WTEnvSchema>>() {
            }.getType());
        }
    }

    private void tryThrowWTRestException(String json) throws WTRestException {
        WTErrorEntity error = gson.fromJson(json, WTErrorEntity.class);
        if (error.getCode() != null && error.getMessage() != null) {
            throw new WTRestException(error);
        }
    }

    private WTErrorEntity executePost(String url, Object tClass, boolean requiredToken) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = this.gson.toJson(tClass);
        RequestBody body = RequestBody.create(json, JSON);
        Builder requestBuilder = new Request.Builder().url(url).post(body);
        if (requiredToken) {
            WTTokenEntity token = this.getToken();
            requestBuilder.addHeader("Authorization", "Bearer " + token.getAccessToken());
        }
        try (Response response = this.httpClient.newCall(requestBuilder.build()).execute()) {
            return this.gson.fromJson(response.body().string(), WTErrorEntity.class);
        }
    }

    private <T> T executeGet(String url, Class<T> pClass, boolean requiredToken) throws IOException {
        Builder requestBuilder = new Request.Builder().url(url);
        if (requiredToken) {
            WTTokenEntity token = this.getToken();
            requestBuilder.addHeader("Authorization", "Bearer " + token.getAccessToken());
        }
        try (Response response = this.httpClient.newCall(requestBuilder.build()).execute()) {
            String jsonString = Objects.requireNonNull(response.body()).string();
            logger.info(" response from api " + jsonString);
            return this.gson.fromJson(jsonString, pClass);
        }
    }

    @Override
    public WTErrorEntity ping() throws IOException {
        String path = String.format(
                this.getApiPath() + "/auth/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                this.clientId, this.clientSecret);

        return this.executeGet(path, WTErrorEntity.class, false);
    }

    @Override
    public WTErrorEntity createRelease() throws IOException {
        return null;
    }

    @Override
    public WTTokenEntity getToken() throws IOException {
        if (this.token != null) {
            return this.token;
        }
        this.token = this.getTokenFromDisk();
        if (this.token == null) {
            this.token = this.getTokenFromApi();
            if (this.token != null)
                this.saveToken(this.token);
        }
        return this.token;
    }

    @Override
    public void saveToken(WTTokenEntity token) throws IOException {
        XmlFile file = getTokenConfigFile();
        try {
            file.write(token);
        } catch (Exception e) {
            logger.info("write token error" + e.getMessage());
        }
    }

    private WTTokenEntity getTokenFromApi() throws IOException {
        String path = String.format(
                this.getApiPath() + "/auth/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                this.clientId, this.clientSecret);

        return this.executeGet(path, WTTokenEntity.class, false);
    }

    private WTTokenEntity getTokenFromDisk() throws IOException {
        XmlFile file = getTokenConfigFile();
        if (!file.exists()) {
            logger.warning("worktile token file not found");
            return null;
        }
        WTTokenEntity token = null;
        try {
            token = (WTTokenEntity) file.unmarshal(this.token);
        } catch (Exception error) {
            logger.warning("file.unmarshal to token from file error = " + error.getMessage());
        }
        if (token != null) {
            if (WorktileUtils.isExpired(token.getExpiresIn())) {
                logger.info("token in cache file is out of date, the token will be set null");
                this.token = null;
            }
        }
        return token;
    }

    private XmlFile getTokenConfigFile() {
        return WorktileUtils.getTokenXmlFile();
    }
}
