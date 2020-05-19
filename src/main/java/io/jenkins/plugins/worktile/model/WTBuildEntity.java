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
    String status = WTHelper.statusOfRun(run);
    WTBuildEntity entity = new WTBuildEntity();
    EnvVars vars;
    try {
      vars = run.getEnvironment(TaskListener.NULL);
    } catch (Exception e) {
      vars = new EnvVars();
    }
    entity.name = run.getFullDisplayName();
    entity.identifier = run.getId();
    entity.resultOverview = WTHelper.resolveOverview(run, pattern);
    entity.status =
        status.equals("success") ? Status.Success.getValue() : Status.Failure.getValue();
    entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
    entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
    entity.duration = run.getDuration();

    WorkItemResolver resolver = null;
    if (run instanceof AbstractBuild<?, ?>) {
      resolver = new WorkItemResolver((AbstractBuild<?, ?>) run, vars);
      entity.jobUrl = ((AbstractBuild<?, ?>) run).getProject().getAbsoluteUrl();
      entity.resultUrl = ((AbstractBuild<?, ?>) run).getProject().getAbsoluteUrl() + run.getNumber() + "/console";
    } else if (run instanceof WorkflowRun) {
      resolver = new WorkItemResolver((WorkflowRun) run, vars);
      entity.jobUrl = run.getAbsoluteUrl();
      entity.resultUrl = run.getAbsoluteUrl() + "console";
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
    Success("success"),
    Failure("failure");
    private final String value;

    Status(String status) {
      this.value = status;
    }

    public String getValue() {
      return this.value;
    }
  }
}
