package io.jenkins.plugins.worktile.model;

import hudson.model.AbstractBuild;

public class BuildResult {
    public String name;
    public String identifier;
    public String jobUrl;
    public String resultOverview;
    public String resultUrl;
    public String status;
    public String[] workItems;
    public long startAt;
    public long endAt;
    public long duration;
    public final String provider = "jenkins";
}
