package io.jenkins.plugins.worktile;

public class WTEnvironment {

    private String name;

    private String htmlUrl;

    public WTEnvironment(String name, String htmlUrl) {
        this.name = name;
        this.htmlUrl = htmlUrl;
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
