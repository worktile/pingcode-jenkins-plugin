package io.jenkins.plugins.worktile.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hudson.XmlFile;

import io.jenkins.plugins.worktile.WTEnvironment;
import io.jenkins.plugins.worktile.WorktileUtils;
import io.jenkins.plugins.worktile.client.ApiConnection;
import io.jenkins.plugins.worktile.client.BuildClient;
import io.jenkins.plugins.worktile.client.DeployClient;
import io.jenkins.plugins.worktile.client.EnvironmentClient;
import io.jenkins.plugins.worktile.model.*;
import okhttp3.*;

import java.io.IOException;
import java.util.logging.Logger;

public class WorktileRestService implements BuildClient, DeployClient, EnvironmentClient {

    public static final Logger logger = Logger.getLogger(WorktileRestService.class.getName());
    private static final String verPrefix = "/v1";

    private String baseURL;
    private ApiConnection apiConnection;

    public WorktileRestService(String endpoint, String token, String clientSecret) {
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
    public WTPaginationResponse<WTEnvSchema> listEnvironments() throws IOException, WTRestException {
        String path = this.baseURL + "";
        return this.apiConnection.executeGet(path);
    }

    @Override
    public WTEnvSchema createEnvironment(WTEnvEntity entity) throws IOException, WTRestException {
        String path = this.baseURL + "";
        return this.apiConnection.executePost(path, entity);
    }

    @Override
    public WTEnvSchema deleteEnvironment(String id) throws IOException, WTRestException {
        String path = this.baseURL + "";
        return this.apiConnection.executeDelete(path);
    }
}
