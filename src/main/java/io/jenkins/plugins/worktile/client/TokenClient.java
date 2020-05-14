package io.jenkins.plugins.worktile.client;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;

public interface TokenClient {
    WTTokenEntity getTokenFromApi() throws IOException, WTRestException;
}
