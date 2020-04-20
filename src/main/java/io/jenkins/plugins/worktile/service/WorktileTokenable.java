package io.jenkins.plugins.worktile.service;

import java.io.IOException;

import io.jenkins.plugins.worktile.model.WorktileToken;

public interface WorktileTokenable {
    WorktileToken getToken() throws IOException;

    void saveToken(WorktileToken token) throws IOException;
}
