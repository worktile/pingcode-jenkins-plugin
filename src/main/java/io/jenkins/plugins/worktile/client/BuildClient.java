package io.jenkins.plugins.worktile.client;

import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTRestException;

import java.io.IOException;

public interface BuildClient {
  Object createBuild(WTBuildEntity entity) throws IOException, WTRestException;
}
