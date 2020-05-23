package io.jenkins.plugins.worktile;

import io.jenkins.plugins.worktile.resolver.WorkItemResolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

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
    public void testMatches() {
        Pattern pattern = WorkItemResolver.pattern;
        List<String> contexts = Arrays.asList("yourName/#PLM-000",
                                              "feat(refactor): #PLM-001 some context do refactor",
                                              "feat(refactor): #PLM-000 do refactor",
                                              "feat(refactor): #PLM-123 #PLM234"
        );

        {
            List<String> matches = WTHelper.matches(pattern, contexts, false, false);
            assertEquals(matches.size(), 3);
        }
        {
            List<String> matches = WTHelper.matches(pattern, contexts, true, false);
            assertEquals(matches.size(), 1);
        }
        {
            List<String> matches = WTHelper.matches(pattern, contexts, true, true);
            assertEquals(matches.size(), 1);
        }
        {
            List<String> matches = WTHelper.matches(pattern, contexts, false, true);
            assertEquals(matches.size(), 4);
        }
    }
}
