package io.jenkins.plugins.worktile;

import hudson.model.TaskListener;

import java.io.Serializable;

public class WTLogger implements Serializable {
  private static final long serialVersionUID = 1L;

  private final TaskListener listener;

  public WTLogger(TaskListener listener) {
    this.listener = listener;
  }

  public void info(String message) {
    this.listener.getLogger().println("WT - [INFO] ================================");
    this.listener.getLogger().println("WT - [INFO] " + message);
    this.listener.getLogger().println("WT - [INFO] ================================");
  }

  public void error(String message) {
    this.listener.getLogger().println("WT - [ERROR] ============ Some error happen with worktile plugin =================");
    this.listener.getLogger().println("WT - [ERROR] This is probably a problem with worktile plugin, verbose information as blow");
    this.listener.getLogger().println("WT - [ERROR] " + message);
    this.listener.getLogger().println("WT - [ERROR] please issue this problem at " + "https://github.com/worktile/wt-rd-jenkins-plugin/issues");
  }
}
