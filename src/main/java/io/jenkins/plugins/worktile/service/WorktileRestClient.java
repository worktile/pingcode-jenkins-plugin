package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.WTEnvironment;
import io.jenkins.plugins.worktile.model.*;

public interface WorktileRestClient {
    WTErrorEntity ping() throws IOException;

    WTErrorEntity createBuild(WTBuildEntity WTBuildEntity) throws IOException;

    WTErrorEntity createEnvironment(WTEnvironment environment) throws IOException;

    WTPaginationResponse<WTEnvSchema> listEnv() throws IOException, WTRestException;

    boolean createDeploy(WTDeployEntity entity) throws  IOException, WTRestException;
}
