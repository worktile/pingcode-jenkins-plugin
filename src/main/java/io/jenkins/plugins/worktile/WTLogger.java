package io.jenkins.plugins.worktile;

import java.io.Serializable;

import hudson.model.TaskListener;

public class WTLogger implements Serializable {
    private static final long serialVersionUID = 1L;

    private TaskListener listener;

    public WTLogger(TaskListener listener) {
        this.listener = listener;
    }

    public void info(String message) {
        this.listener.getLogger().println("Worktile - [INFO] " + message);
    }

    public void error(String message) {
        this.listener.getLogger().println("Worktile - [ERROR] " + message);
    }
}
