package io.jenkins.plugins.worktile.resolver;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.plugins.worktile.WTHelper;
import jenkins.scm.RunWithSCM;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class WorkItemResolver {
    public static final Pattern pattern = Pattern.compile("#[^(\\s|/)]*[A-Za-z0-9_]+-[0-9]+");

    private final Set<String> collection = new HashSet<>();
    private final RunWithSCM scm;
    private final EnvVars envVars;

    public WorkItemResolver(RunWithSCM scm, EnvVars vars) {
        this.scm = scm;
        this.envVars = vars;
    }

    @SuppressWarnings("rawtypes")
    public static WorkItemResolver create(Run<?, ?> run, EnvVars vars) {
        RunWithSCM runWithScm = null;
        if(run instanceof AbstractBuild<?, ?>) {
            runWithScm = (AbstractBuild<?, ?>)run;
        }
        else if(run instanceof WorkflowRun) {
            runWithScm = (WorkflowRun)run;
        }
        return new WorkItemResolver(runWithScm, vars);
    }

    public List<String> resolve() {
        collection.clear();
        fromSCM();
        fromEnvironment();
        List<String> matches = WTHelper.matches(pattern, new ArrayList<>(collection), false, false);
        return WTHelper.formatWorkItems(matches);
    }

    public void fromSCM() {
        if(scm == null) { return; }
        List changeLogSets = scm.getChangeSets();
        for(Object changeLogSet : changeLogSets) {
            for(Object set : (ChangeLogSet<? extends Entry>)changeLogSet) {
                String msg = ((Entry)set).getMsg();
                if(msg != null) {
                    collection.add(msg);
                }
            }
        }
    }

    public void fromEnvironment() {
        if(envVars == null) { return; }
        if(envVars.get("GIT_BRANCH") != null) { collection.add(envVars.get("GIT_BRANCH")); }
        if(envVars.get("ghprbSourceBranch") != null) { collection.add(envVars.get("ghprbSourceBranch")); }
        if(envVars.get("ghprbPullTitle") != null) { collection.add(envVars.get("ghprbPullTitle")); }
        if(envVars.get("ghprbCommentBody") != null) { collection.add(envVars.get("ghprbCommentBody")); }
    }
}
