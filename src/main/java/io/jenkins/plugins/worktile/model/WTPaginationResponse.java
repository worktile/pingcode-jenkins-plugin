package io.jenkins.plugins.worktile.model;

public class WTPaginationResponse<T> {
    public Integer pageIndex;
    public Integer pageSize;
    public Integer total;
    public T[] values;
}
