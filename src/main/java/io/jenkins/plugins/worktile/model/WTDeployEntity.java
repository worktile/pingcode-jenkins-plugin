package io.jenkins.plugins.worktile.model;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.worktile.WTHelper;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;

public class WTDeployEntity {
  public String releaseName;
  public String status;
  public String envId;
  public String releaseUrl;
  public long startAt;
  public long endAt;
  public long duration;
  public String[] workItemIdentifiers;

  public static WTDeployEntity from(
      Run<?, ?> run, String releaseName, String releaseUrl, String envId) {
    WTDeployEntity entity = new WTDeployEntity();
    String status = WTHelper.statusOfRun(run);

    EnvVars vars;
    try {
      vars = run.getEnvironment(TaskListener.NULL);
    } catch (Exception e) {
      vars = new EnvVars();
    }

    entity.releaseName = vars.expand(releaseName);
    entity.releaseUrl = vars.expand(releaseUrl);
    entity.envId = envId;
    entity.status =
        status.equals("success") ? Status.Deployed.getDeploy() : Status.NotDeployed.getDeploy();
    entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
    entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
    entity.duration = run.getDuration();

    List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = new ArrayList<>();

    if (run instanceof AbstractBuild<?, ?>) {
      changeLogSets = ((AbstractBuild<?, ?>) run).getChangeSets();
    } else if (run instanceof WorkflowRun) {
      changeLogSets = ((WorkflowRun) run).getChangeSets();
    }

    entity.workItemIdentifiers =
        WTHelper.extractWorkItems(changeLogSets, vars).toArray(new String[0]);
    return entity;
  }

  public enum Status {
    Deployed("deployed"),
    NotDeployed("not_deployed");

    private final String deploy;

    private Status(String deploy) {
      this.deploy = deploy;
    }

    public String getDeploy() {
      return deploy;
    }
  }
}
