package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTEnvironment;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import io.jenkins.plugins.worktile.model.WTEnvSchema;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.model.WTPaginationResponse;
import io.jenkins.plugins.worktile.model.WTRestException;

public interface WorktileRestClient {
    WTErrorEntity ping() throws IOException;

    WTErrorEntity createBuild(WTBuildEntity WTBuildEntity) throws IOException;

    WTErrorEntity createRelease() throws IOException;

    WTErrorEntity createEnvironment(WTEnvironment environment) throws IOException;

    WTPaginationResponse<WTEnvSchema> listEnv() throws IOException, WTRestException;
}
