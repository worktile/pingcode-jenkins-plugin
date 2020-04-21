package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.BuildResult;
import io.jenkins.plugins.worktile.model.WTError;

public interface WorktileRestClient {
    WTError ping() throws IOException;

    WTError createBuild(BuildResult buildResult) throws IOException;

    WTError createRelease() throws IOException;
}
