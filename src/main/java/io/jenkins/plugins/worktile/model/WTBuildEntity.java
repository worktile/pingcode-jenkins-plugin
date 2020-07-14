package io.jenkins.plugins.worktile.model;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.worktile.WTHelper;
import io.jenkins.plugins.worktile.resolver.WorkItemResolver;

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

    public static WTBuildEntity from(Run<?, ?> run, FilePath workspace, TaskListener listener, String pattern,
            String defaultSummary, String resultURL) {
        return WTBuildEntity.from(run, workspace, listener, null, pattern, defaultSummary, resultURL);
    }

    public static WTBuildEntity from(Run<?, ?> run, FilePath workspace, TaskListener listener, String status,
            String pattern, String defaultSummary, String resultURL) {
        WTBuildEntity entity = new WTBuildEntity();
        if (status == null) {
            String autoStatus = WTHelper.statusOfRun(run);
            status = autoStatus.equals("success") ? Status.Success.getValue() : Status.Failure.getValue();
        }

        EnvVars vars = WTHelper.safeEnvVars(run);

        entity.status = status;
        String fullName = run.getFullDisplayName();
        int index = fullName.lastIndexOf("#");
        entity.name = fullName.substring(0, index).trim();
        entity.identifier = run.getId();
        entity.resultOverview = WTHelper.resolveOverview(run, pattern, defaultSummary);
        entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
        entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
        entity.duration = Math.subtractExact(entity.endAt, entity.startAt);

        if (run instanceof AbstractBuild<?, ?>) {
            String defaultResultUrl = ((AbstractBuild<?, ?>) run).getProject() //
                    .getAbsoluteUrl() //
                    + run.getNumber() + "/console";
            entity.jobUrl = ((AbstractBuild<?, ?>) run).getProject().getAbsoluteUrl();
            entity.resultUrl = (resultURL == null || resultURL.trim().isEmpty()) ? defaultResultUrl
                    : vars.expand(resultURL);
        } //
        else if (run instanceof WorkflowRun) {
            String defaultResultUrl = run.getAbsoluteUrl() + "console";
            entity.jobUrl = run.getAbsoluteUrl();
            entity.resultUrl = (resultURL == null || resultURL.trim().isEmpty()) ? defaultResultUrl
                    : vars.expand(resultURL);
        }
        entity.workItemIdentifiers = new WorkItemResolver(run, workspace, listener) //
                .resolve() //
                .toArray(new String[0]);

        return entity;
    }

    public String toString() {
        return WTHelper.prettyJSON(this);
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
