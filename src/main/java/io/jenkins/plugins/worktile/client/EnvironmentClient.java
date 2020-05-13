package io.jenkins.plugins.worktile.client;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.WTEnvEntity;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;

public interface EnvironmentClient {
    WTPaginationResponse<WTEnvSchema> listEnvironments() throws IOException, WTRestException;

    WTEnvSchema deleteEnvironment(String id) throws IOException, WTRestException;

    WTEnvSchema createEnvironment(WTEnvEntity entity) throws IOException, WTRestException;
}
