package io.jenkins.plugins.worktile.model;

public class WTEnvEntity {
    private String name;
    private String htmlUrl;

    public WTEnvEntity(String name, String htmlUrl) {
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
