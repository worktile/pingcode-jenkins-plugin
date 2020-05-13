package io.jenkins.plugins.worktile.client;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTRestException;

public interface DeployClient {
    Object createDeploy(WTDeployEntity entity) throws IOException, WTRestException;
}
