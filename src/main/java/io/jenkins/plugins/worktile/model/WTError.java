package io.jenkins.plugins.worktile.model;

public class WTError {
    private String code;
    private String message;

    public WTError(String code, String message) {
        setCode(code);
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
