package io.jenkins.plugins.worktile.model;

public class WTDeployEntity {
    public String releaseName;
    public String status;
    public String envId;
    public String releaseUrl;
    public long startAt;
    public long endAt;
    public long duration;
    public String[] workItemIdentifiers;
}
