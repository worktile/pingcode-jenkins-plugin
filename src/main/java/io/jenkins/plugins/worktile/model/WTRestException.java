package io.jenkins.plugins.worktile.model;

public class WTRestException extends Exception {

    public static final long serialVersionUID = 123456789L;

    private String code;

    private String message;

    public WTRestException(String code, String message) {
        super(message);
        setCode(code);
        setMessage(message);
    }

    public WTRestException(WTErrorEntity entity) {
        this(entity.getCode(), entity.getMessage());
    }

    @Override
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
