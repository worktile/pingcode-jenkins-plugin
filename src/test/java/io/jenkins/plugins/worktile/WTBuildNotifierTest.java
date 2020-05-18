package io.jenkins.plugins.worktile;

import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.openjdk.jmh.annotations.Setup;

import static org.junit.Assert.*;

public class WTBuildNotifierTest {
    @Rule Jenkins jenkinsRul = Jenkins.getInstanceOrNull();
}
