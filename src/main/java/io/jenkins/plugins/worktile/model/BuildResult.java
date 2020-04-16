package io.jenkins.plugins.worktile.model;

public class BuildResult {
    public final String provider = "jenkins";

    private String identifier;

    private String ResultOverview;

    private String jobString;

    private String resultString;

    private Integer startAt;

    private Integer endAt;

    private Integer duration;

    public BuildResult() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getEndAt() {
        return endAt;
    }

    public void setEndAt(Integer endAt) {
        this.endAt = endAt;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public void setStartAt(Integer startAt) {
        this.startAt = startAt;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public String getJobString() {
        return jobString;
    }

    public void setJobString(String jobString) {
        this.jobString = jobString;
    }

    public String getResultOverview() {
        return ResultOverview;
    }

    public void setResultOverview(String resultOverview) {
        this.ResultOverview = resultOverview;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}