package io.jenkins.plugins.worktile.model;

public class Token {
    private String value;
    private long expire;
    private String type;

    Token(String value, long expire, String type) {
        setValue(value);
        setExpire(expire);
        setType(type);
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setValue(String value) {
        this.value = value;
    }
}