package io.jenkins.plugins.worktile.model;

public class WTErrorEntity {
  private String code;
  private String message;

  public WTErrorEntity(String code, String message) {
    setCode(code);
    setMessage(message);
  }

  @Override
  public String toString() {
    return "WTError code: " + getCode() + " " + "message: " + getMessage();
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
