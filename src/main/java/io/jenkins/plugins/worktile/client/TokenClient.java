package io.jenkins.plugins.worktile.client;

import io.jenkins.plugins.worktile.model.WTRestException;
import io.jenkins.plugins.worktile.model.WTTokenEntity;

import java.io.IOException;

public interface TokenClient {
  WTTokenEntity getTokenFromApi() throws IOException, WTRestException;
}
