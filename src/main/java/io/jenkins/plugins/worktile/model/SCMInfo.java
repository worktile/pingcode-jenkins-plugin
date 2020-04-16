package io.jenkins.plugins.worktile.model;

import java.util.List;

public class SCMInfo {
    private String url;

    private String branch;

    private String commit;

    private List<String> blames;

    public SCMInfo() {
    }

    public String getUrl() {
        return url;
    }

    public List<String> getBlames() {
        return blames;
    }

    public void setBlames(List<String> blames) {
        this.blames = blames;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}