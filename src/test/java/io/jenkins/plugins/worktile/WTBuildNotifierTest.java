package io.jenkins.plugins.worktile;

import hudson.EnvVars;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.worktile.model.WTBuildEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class WTBuildNotifierTest {
  @Rule public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void testBuildPerform() throws IOException, InterruptedException {
    FreeStyleProject freeStyleProject = mock(FreeStyleProject.class);
    AbstractBuild<?, ?> abstractBuild = mock(AbstractBuild.class);
    // doReturn(freeStyleProject).when(abstractBuild).getProject();
    EnvVars vars = mock(EnvVars.class);

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
      when(abstractBuild.getResult()).thenReturn(Result.SUCCESS);
      when(abstractBuild.getLog(999)).thenReturn(Collections.singletonList("hello, world"));
      when(abstractBuild.getEnvironment(TaskListener.NULL)).thenReturn(vars);

      WTBuildEntity entity = WTBuildEntity.from(abstractBuild, "^hello");
      assertEquals(entity.name, "build-1");
      assertEquals(entity.status, "success");
      assertEquals(entity.duration, 10L);
      assertEquals(entity.resultOverview, "hello, world");
      assertEquals(entity.duration, 10L);
    }
  }

  @Before
  public void prepare() {}

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
