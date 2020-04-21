package io.jenkins.plugins.worktile.model;

public class WTBuildEntity {
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
    public final String provider = "jenkins";
}
