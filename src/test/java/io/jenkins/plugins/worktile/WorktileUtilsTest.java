package io.jenkins.plugins.worktile;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> items1 = WorktileUtils.getWorkItems(Arrays.asList(new String[] { "#PLM-000", "#PLM-0001" }));
        assertEquals(2, items1.size());

        List<String> array1 = WorktileUtils
                .getWorkItems(Arrays.asList(new String[] { "#PLM-000", "#PLM-0001", "hello, world" }));
        assertEquals(2, array1.size());

        List<String> array2 = WorktileUtils.getWorkItems(Arrays.asList(new String[] { "#PLM-000", "#PLM-000" }));
        assertEquals(1, array2.size());

        List<String> array3 = WorktileUtils.getWorkItems(Arrays.asList(new String[] { "#hello", "#world", "welcome" }));
        assertEquals(0, array3.size());
    }

    @Test
    public void testIsExpired() {
        assertEquals(WorktileUtils.isExpired(WorktileUtils.toSafeTs(System.currentTimeMillis() - 200000)), true);
        assertEquals(WorktileUtils.isExpired(WorktileUtils.toSafeTs(System.currentTimeMillis() + 200000)), false);
    }
}
