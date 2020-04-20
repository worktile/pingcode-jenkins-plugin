package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.BuildResult;

public interface WorktileRestClient {
    boolean ping() throws IOException;

    String createBuild(BuildResult buildResult) throws IOException;

    void createRelease() throws IOException;
}
