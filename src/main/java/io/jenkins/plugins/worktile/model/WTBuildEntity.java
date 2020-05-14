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

    public String toString() {
        return String.format(
                "name:%s\nid:%s\njobUrl:%s\n,overview:%s\nresultUrl:%s,status:%s,start:%d,end:%d,duration:%d,provider:%s",
                name, identifier, jobUrl, resultOverview, resultUrl, status, startAt, endAt, duration, provider);
    }
}
