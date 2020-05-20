package io.jenkins.plugins.worktile;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.worktile.model.WTDeployEntity;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WTDeployNotifierTest {
  @Test
  public void perform() throws IOException, InterruptedException {
    AbstractBuild<?, ?> abstractBuild = mock(AbstractBuild.class);
    EnvVars vars = new EnvVars();
    vars.put("BUILD_ID", "110");

    final MockEntry entry = new MockEntry("feat(build): #PLM-80 this is the first build");

    {
      ChangeLogSet changeLogSet = mock(ChangeLogSet.class);
      when(abstractBuild.getChangeSet()).thenReturn(changeLogSet);
      List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = new ArrayList<>();
      changeSets.add(changeLogSet);
      final Set<? extends ChangeLogSet.Entry> entries = new HashSet(Arrays.asList(entry));
      when(changeLogSet.iterator()).thenAnswer(invocation -> entries.iterator());
      when(abstractBuild.getChangeSets()).thenReturn(changeSets);

      when(abstractBuild.getFullDisplayName()).thenReturn("build-1");
      when(abstractBuild.getUrl()).thenReturn("https://nothing.io:8080");
      when(abstractBuild.getId()).thenReturn("1");
      when(abstractBuild.getDuration()).thenReturn(10L);
      when(abstractBuild.getResult()).thenReturn(Result.FAILURE);
      when(abstractBuild.getEnvironment(TaskListener.NULL)).thenReturn(vars);

      WTDeployEntity entity =
          WTDeployEntity.from(
              abstractBuild,
              "build-${BUILD_ID}",
              "http://www.worktile.com/release-1",
              "1234567");

      assertEquals(entity.releaseName, "build-110");
      assertEquals(entity.releaseUrl, "http://www.worktile.com/release-1");
      assertEquals(entity.status, WTDeployEntity.Status.NotDeployed.getDeploy());

      vars.clear();
    }
  }

  private static class MockEntry extends ChangeLogSet.Entry {
    private final String msg;

    public MockEntry(String msg) {
      this.msg = msg;
    }

    @Override
    public String getMsg() {
      return this.msg;
    }

    @Override
    public User getAuthor() {
      return null;
    }

    @Override
    public Collection<String> getAffectedPaths() {
      return null;
    }
  }
}
