package io.jenkins.plugins.worktile.model;

import com.google.gson.Gson;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.resolver.WorkItemResolver;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

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

    WorkItemResolver resolver = null;
    if (run instanceof AbstractBuild<?, ?>) {
      resolver = new WorkItemResolver((AbstractBuild<?, ?>) run, vars);
    } else if (run instanceof WorkflowRun) {
      resolver = new WorkItemResolver((WorkflowRun) run, vars);
    }
    if (resolver != null) {
      entity.workItemIdentifiers = resolver.resolve().toArray(new String[0]);
    }
    return entity;
  }

  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public enum Status {
    Deployed("deployed"),
    NotDeployed("not_deployed");

    private final String deploy;

    Status(String deploy) {
      this.deploy = deploy;
    }

    public String getDeploy() {
      return deploy;
    }
  }
}
