package io.jenkins.plugins.worktile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class WTHelperTest {

    @Test
    public void testIsURL1() {
        assertEquals(true, WTHelper.isURL("https://ww.worktile.com"));
    }

    @Test
    public void testIsURL2() {
        assertEquals(false, WTHelper.isURL("hello, world"));
    }

    @Test
    public void testGetWorkItems() {
        /* pattern */
        List<String> items1 = WTHelper.getWorkItems(Arrays.asList(new String[] { "#PLM-000", "#PLM-0001" }));
        assertEquals(2, items1.size());

        List<String> array1 = WTHelper
                .getWorkItems(Arrays.asList(new String[] { "#PLM-000", "#PLM-0001", "hello, world" }));
        assertEquals(2, array1.size());

        List<String> array2 = WTHelper.getWorkItems(Arrays.asList(new String[] { "#PLM-000", "#PLM-000" }));
        assertEquals(1, array2.size());

        List<String> array3 = WTHelper.getWorkItems(Arrays.asList(new String[] { "#hello", "#world", "welcome" }));
        assertEquals(0, array3.size());
    }

    @Test
    public void testIsExpired() {
        assertEquals(WTHelper.isExpired(WTHelper.toSafeTs(System.currentTimeMillis() - 200000)), true);
        assertEquals(WTHelper.isExpired(WTHelper.toSafeTs(System.currentTimeMillis() + 200000)), false);
    }
}
