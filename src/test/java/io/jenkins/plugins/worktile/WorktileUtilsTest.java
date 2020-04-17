package io.jenkins.plugins.worktile;

import org.junit.Test;
import static org.junit.Assert.*;

public class WorktileUtilsTest {

    @Test
    public void testIsURL1() {
        assertEquals(true, WorktileUtils.isURL("https://ww.worktile.com"));
    }

    @Test
    public void testIsURL2() {
        assertEquals(false, WorktileUtils.isURL("hello, world"));
    }
}
