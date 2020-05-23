package io.jenkins.plugins.worktile.resolver;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        List<String> items = WorkItemResolver.create(build, null).resolve();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), "PLM-80");
    }

    @Test
    public void testResolveFromEnv() {
        EnvVars vars = new EnvVars();
        vars.put("GIT_BRANCH", "fanyongtao/#PLM-90");
        List<String> items = WorkItemResolver.create(null, vars).resolve();
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
