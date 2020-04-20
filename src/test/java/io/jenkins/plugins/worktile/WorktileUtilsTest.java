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

    @Test
    public void testGetWorkItems() {
        /* pattern */
        String[] array = WorktileUtils.getWorkItems(new String[] { "#PLM-000", "#PLM-0001" });
        assertEquals(2, array.length);
        String[] array1 = WorktileUtils.getWorkItems(new String[] { "#PLM-000", "#PLM-0001", "hello, world" });
        assertEquals(2, array1.length);
        String[] array2 = WorktileUtils.getWorkItems(new String[] { "#PLM-000", "#PLM-000" });
        assertEquals(1, array2.length);
        String[] array3 = WorktileUtils.getWorkItems(new String[] { "#hello", "#world", "welcome" });
        assertEquals(0, array3.length);
    }
}
