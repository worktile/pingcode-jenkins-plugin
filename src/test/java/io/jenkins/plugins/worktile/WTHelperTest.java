package io.jenkins.plugins.worktile;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
}
