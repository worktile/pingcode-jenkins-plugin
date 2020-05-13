package io.jenkins.plugins.worktile.client;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTRestException;

public interface BuildClient {
    Object createBuild(WTBuildEntity entity) throws IOException, WTRestException;
}
