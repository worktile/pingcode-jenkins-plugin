package io.jenkins.plugins.worktile;

import java.io.PrintStream;

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
        PrintStream log = listener.getLogger();
        log.println("result = " + run.getResult());
        log.println("build nubmer ? = " + run.number);
        log.println("build run.getId ? = " + run.getId());
        log.println("build run duration" + run.getDuration());
        log.println("build run getUrl" + run.getUrl());
    }
}