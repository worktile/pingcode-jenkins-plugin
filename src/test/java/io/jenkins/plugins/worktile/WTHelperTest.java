package io.jenkins.plugins.worktile;

import hudson.model.Run;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WTHelperTest {

    @Test
    public void testIsURL1() {
        assertTrue(WTHelper.isURL("https://ww.worktile.com"));
        assertFalse(WTHelper.isURL("hello, world"));
    }

    @Test
    public void testGetWorkItems() {
        /* pattern */
        List<String> items1 = WTHelper.formatWorkItems(Arrays.asList("#PLM-000", "#PLM-0001"));
        assertEquals(2, items1.size());
        items1.forEach(item -> {
            assertFalse(item.startsWith("#"));
        });
    }

    @Test
    public void testBuildName() {
        String fullName = "fyt-jenkins-demo #10";
        int index = fullName.lastIndexOf("#");
        String name = fullName.substring(0, index).trim();
        assertEquals(name, "fyt-jenkins-demo");
    }

    @Test
    public void testGetReviewResult() throws IOException {
        Run<?, ?> run = mock(Run.class);
        when(run.getLog(anyInt())).thenReturn(Arrays.asList("using credential 0eb6598a-7c2a-4ef2-a2eb-64934de6b415",
                "hello, world", "Run test cases  38369", " 788 passing (13s)", "Statements   : 92.55% ( 3129/3381 )"));

        String matched = WTHelper.resolveOverview(run, "^*passing", "");
        assertEquals(matched, " 788 passing (13s)");

        String Statements = WTHelper.resolveOverview(run, "Statements", "");
        assertEquals(Statements, "Statements   : 92.55% ( 3129/3381 )");

        String defaultValue = WTHelper.resolveOverview(run, "Nothing for matching", "defaultSummary");
        assertEquals(defaultValue, "defaultSummary");
    }
}
