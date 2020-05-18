package io.jenkins.plugins.worktile.client;

import io.jenkins.plugins.worktile.model.WTDeployEntity;
import io.jenkins.plugins.worktile.model.WTRestException;

import java.io.IOException;

public interface DeployClient {
  Object createDeploy(WTDeployEntity entity) throws IOException, WTRestException;
}
