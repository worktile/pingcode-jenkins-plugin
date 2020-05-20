package io.jenkins.plugins.worktile.model;

import com.google.gson.Gson;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.resolver.WorkItemResolver;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

public class WTBuildEntity {
  public final String provider = "jenkins";
  public String name;
  public String identifier;
  public String jobUrl;
  public String resultOverview;
  public String resultUrl;
  public String status;
  public String[] workItemIdentifiers;
  public long startAt;
  public long endAt;
  public long duration;

  public static WTBuildEntity from(Run<?, ?> run, String pattern) {
    return WTBuildEntity.from(run, null, pattern);
  }

  public static WTBuildEntity from(Run<?, ?> run, String status, String pattern) {
    WTBuildEntity entity = new WTBuildEntity();

    if (status == null) {
      String autoStatus = WTHelper.statusOfRun(run);
      status = autoStatus.equals("success") ? Status.Success.getValue() : Status.Failure.getValue();
    }

    entity.status = status;
    EnvVars vars = WTHelper.safeEnvVars(run);
    String fullName = run.getFullDisplayName();
    int index = fullName.lastIndexOf("#");
    entity.name = fullName.substring(0, index).trim();
    entity.identifier = run.getId();
    entity.resultOverview = WTHelper.resolveOverview(run, pattern);
    entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
    entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
    entity.duration = run.getDuration();

    if (run instanceof AbstractBuild<?, ?>) {
      entity.jobUrl = ((AbstractBuild<?, ?>) run).getProject().getAbsoluteUrl();
      entity.resultUrl = ((AbstractBuild<?, ?>) run).getProject().getAbsoluteUrl() + run.getNumber() + "/console";
    } else if (run instanceof WorkflowRun) {
      entity.jobUrl = run.getAbsoluteUrl();
      entity.resultUrl = run.getAbsoluteUrl() + "console";
    }
    entity.workItemIdentifiers = WorkItemResolver.create(run, vars).resolve().toArray(new String[0]);
    return entity;
  }

  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public enum Status {
    Success("success"), Failure("failure");

    private final String value;

    Status(String status) {
      this.value = status;
    }

    public String getValue() {
      return this.value;
    }
  }
}
