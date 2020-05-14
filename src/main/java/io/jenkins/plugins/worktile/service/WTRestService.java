package io.jenkins.plugins.worktile.service;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.jenkins.plugins.worktile.client.ApiConnection;
import io.jenkins.plugins.worktile.client.BuildClient;
import io.jenkins.plugins.worktile.client.DeployClient;
import io.jenkins.plugins.worktile.client.EnvironmentClient;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;

import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;

public class WTRestService implements BuildClient, DeployClient, EnvironmentClient {

    public static final Logger logger = Logger.getLogger(WTRestService.class.getName());

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String baseURL;
    private ApiConnection apiConnection;

    public WTRestService(String endpoint, String token) {
        this.baseURL = endpoint;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();
        this.apiConnection = new ApiConnection(token, client);
    }

    @Override
    public Object createBuild(WTBuildEntity entity) throws IOException, WTRestException {
        String path = this.baseURL + "/build/builds";
        return this.apiConnection.executePost(path, entity);
    }

    @Override
    public Object createDeploy(WTDeployEntity entity) throws IOException, WTRestException {
        String path = this.baseURL + "/release/deploys";
        return this.apiConnection.executePost(path, entity);
    }

    @Override
    public WTPaginationResponse<WTEnvironmentSchema> listEnvironments() throws IOException, WTRestException {
        String path = this.baseURL + "/release/environments?page_index=0&page_size=100";
        String json = this.apiConnection.executeGet(path);
        return gson.fromJson(json, new TypeToken<WTPaginationResponse<WTEnvironmentSchema>>() {
        }.getType());
    }

    @Override
    public WTEnvironmentSchema createEnvironment(WTEnvironmentEntity entity) throws IOException, WTRestException {
        String path = this.baseURL + "/release/environments";
        String json = this.apiConnection.executePost(path, entity);
        return gson.fromJson(json, WTEnvironmentSchema.class);
    }

    @Override
    public WTEnvironmentSchema deleteEnvironment(String id) throws IOException, WTRestException {
        String path = this.baseURL + "/release/environments/" + id;
        String json = this.apiConnection.executeDelete(path);
        return gson.fromJson(json, WTEnvironmentSchema.class);
    }
}
