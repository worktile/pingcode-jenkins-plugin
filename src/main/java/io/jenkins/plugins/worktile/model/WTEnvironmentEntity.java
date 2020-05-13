package io.jenkins.plugins.worktile.model;

public class WTEnvironmentEntity {
    private String name;
    private String htmlUrl;

    public WTEnvironmentEntity(String name, String htmlUrl) {
        this.setName(name);
        this.setHtmlUrl(htmlUrl);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
