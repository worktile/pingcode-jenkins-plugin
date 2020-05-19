package io.jenkins.plugins.worktile.resolver;

import hudson.EnvVars;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.worktile.WTHelper;
import jenkins.scm.RunWithSCM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class WorkItemResolver {
  public static final Pattern branchPattern = Pattern.compile("#[^/]*[A-Za-z0-9_]+-[0-9]+");
  public static final Pattern messagePattern = Pattern.compile("#[^\\s]*[A-Za-z0-9_]+-[0-9]+");

  private final RunWithSCM scm;
  private final EnvVars envVars;

  public WorkItemResolver(RunWithSCM scm, EnvVars vars) {
    this.scm = scm;
    this.envVars = vars;
  }

  public List<String> resolve() {
    List<String> array = new ArrayList<>();
    if (getScm() != null) {
      array.addAll(resolveFromSCM());
    }
    if (getEnvVars() != null) {
      array.addAll(resolveFromEnv());
    }
    return array;
  }

  public RunWithSCM getScm() {
    return scm;
  }

  public List<String> resolveFromSCM() {
    if (getScm() == null) return new ArrayList<>();
    List changeSets = getScm().getChangeSets();
    List<String> array = new ArrayList<>();
    changeSets.forEach(
        changeSet -> {
          array.addAll(resolveFromChangeSet((ChangeLogSet<ChangeLogSet.Entry>) changeSet));
        });
    return array;
  }

  public EnvVars getEnvVars() {
    return envVars;
  }

  public List<String> resolveFromEnv() {
    if (getEnvVars() == null) {
      return new ArrayList<>();
    }
    String branch = getEnvVars().get("GIT_BRANCH");
    String ghprbSourceBranch = getEnvVars().get("ghprbSourceBranch");
    String ghprbPullTitle = getEnvVars().get("ghprbPullTitle");

    List<String> array = new ArrayList<>();
    if (branch != null) {
      array.addAll(
          WTHelper.formatWorkItems(
              WTHelper.getMatchSet(
                  branchPattern, Collections.singletonList(branch), false, false)));
    }
    if (ghprbSourceBranch != null) {
      array.addAll(
          WTHelper.formatWorkItems(
              WTHelper.getMatchSet(
                  branchPattern, Collections.singletonList(ghprbSourceBranch), false, false)));
    }
    if (ghprbPullTitle != null) {
      array.addAll(
          WTHelper.formatWorkItems(
              WTHelper.getMatchSet(
                  messagePattern, Collections.singletonList(ghprbPullTitle), false, false)));
    }
    return new ArrayList<>(new HashSet<>(array));
  }

  public List<String> resolveFromChangeSet(ChangeLogSet<ChangeLogSet.Entry> logSet) {
    List<String> array = new ArrayList<>();
    for (Object change : logSet) {
      ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
      String scmMessage = entry.getMsg();
      array.add(scmMessage);
    }
    return WTHelper.formatWorkItems(WTHelper.getMatchSet(messagePattern, array, false, false));
  }
}
