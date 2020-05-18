package io.jenkins.plugins.worktile.model;

public class WTBuildEntity {
  public final String provider = "jenkins";
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

  public static enum Status {
    Success("success"),
    Failure("failure");

    private final String value;

    private Status(String status) {
      this.value = status;
    }

    public String getValue() {
      return this.value;
    }
  }
}
