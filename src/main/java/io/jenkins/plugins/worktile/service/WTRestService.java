package io.jenkins.plugins.worktile.service;

import java.io.IOException;
import java.util.logging.Logger;

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
import okhttp3.OkHttpClient;

public class WTRestService implements BuildClient, DeployClient, EnvironmentClient {

    public static final Logger logger = Logger.getLogger(WTRestService.class.getName());
    private static final String verPrefix = "/v1";

    private String baseURL;
    private ApiConnection apiConnection;

    public WTRestService(String endpoint, String token, String clientSecret) {
        this.baseURL = endpoint + verPrefix;
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
        String path = this.baseURL + "/build/builds";
        return this.apiConnection.executePost(path, entity);
    }

    @Override
    public WTPaginationResponse<WTEnvironmentSchema> listEnvironments() throws IOException, WTRestException {
        String path = this.baseURL + "";
        return this.apiConnection.executeGet(path);
    }

    @Override
    public WTEnvironmentSchema createEnvironment(WTEnvironmentEntity entity) throws IOException, WTRestException {
        String path = this.baseURL + "";
        return this.apiConnection.executePost(path, entity);
    }

    @Override
    public WTEnvironmentSchema deleteEnvironment(String id) throws IOException, WTRestException {
        String path = this.baseURL + "";
        return this.apiConnection.executeDelete(path);
    }
}
