package io.jenkins.plugins.worktile;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class WTHelperTest {

  @Test
  public void testIsURL1() {
    assertTrue(WTHelper.isURL("https://ww.worktile.com"));
  }

  @Test
  public void testIsURL2() {
    assertFalse(WTHelper.isURL("hello, world"));
  }

  @Test
  public void testGetWorkItems() {
    /* pattern */
    List<String> items1 = WTHelper.getWorkItems(Arrays.asList("#PLM-000", "#PLM-0001"));
    assertEquals(2, items1.size());

    List<String> array1 =
        WTHelper.getWorkItems(Arrays.asList("#PLM-000", "#PLM-0001", "hello, world"));
    assertEquals(2, array1.size());

    List<String> array2 = WTHelper.getWorkItems(Arrays.asList("#PLM-000", "#PLM-000"));
    assertEquals(1, array2.size());

    List<String> array3 = WTHelper.getWorkItems(Arrays.asList("#hello", "#world", "welcome"));
    assertEquals(0, array3.size());
  }
}
