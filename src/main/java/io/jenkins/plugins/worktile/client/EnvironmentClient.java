package io.jenkins.plugins.worktile.client;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.WTEnvironmentEntity;
import io.jenkins.plugins.worktile.model.WTEnvironmentSchema;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;

public interface EnvironmentClient {
    WTPaginationResponse<WTEnvironmentSchema> listEnvironments() throws IOException, WTRestException;

    WTEnvironmentSchema getEnvironmentByName(String name) throws IOException, WTRestException;

    WTEnvironmentSchema deleteEnvironment(String id) throws IOException, WTRestException;

    WTEnvironmentSchema createEnvironment(WTEnvironmentEntity entity) throws IOException, WTRestException;
}
