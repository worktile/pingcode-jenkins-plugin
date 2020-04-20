package io.jenkins.plugins.worktile;

import hudson.Extension;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

@Extension
@SuppressWarnings("rawtypes")
public class ProjectListener extends RunListener<Run> {

    public ProjectListener() {
        super(Run.class);
    }

    @SuppressWarnings("CastToConcreteClass")
    @Override
    public void onCompleted(Run run, TaskListener listener) {
    }
}
