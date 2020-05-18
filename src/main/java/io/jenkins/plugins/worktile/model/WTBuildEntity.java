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
    entity.jobUrl = run.getAbsoluteUrl();
    entity.resultOverview = WTHelper.resolveOverview(run, pattern);
    entity.resultUrl = run.getAbsoluteUrl() + "console";
    entity.status =
        status.equals("success") ? Status.Success.getValue() : Status.Failure.getValue();
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
