package io.jenkins.plugins.worktile.resolver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet;

public class WorkItemResolverTest {

  @Test
  public void testResolveFromSCM() {
    final MockEntry entry = new MockEntry("feat(build): #PLM-80 this is the first build");
    final Set<? extends ChangeLogSet.Entry> entries = new HashSet(Arrays.asList(entry));
    AbstractBuild build = mock(AbstractBuild.class);
    ChangeLogSet changeLogSet = mock(ChangeLogSet.class);
    List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = new ArrayList<>();
    changeSets.add(changeLogSet);
    when(changeLogSet.iterator()).thenAnswer(invocation -> entries.iterator());
    when(build.getChangeSets()).thenReturn(changeSets);
    WorkItemResolver resolver = new WorkItemResolver(build, null);
    List<String> items = resolver.resolveFromSCM();
    assertEquals(items.size(), 1);
    assertEquals(items.get(0), "PLM-80");
  }

  @Test
  public void testResolveFromEnv() {
    EnvVars vars = new EnvVars();
    vars.put("GIT_BRANCH", "fanyongtao/#PLM-90");
    WorkItemResolver resolver = new WorkItemResolver(null, vars);
    List<String> items = resolver.resolveFromEnv();
    assertEquals(items.size(), 1);
    assertEquals(items.get(0), "PLM-90");
    vars.clear();
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
