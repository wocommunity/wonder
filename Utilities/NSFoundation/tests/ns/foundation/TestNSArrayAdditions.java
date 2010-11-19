package ns.foundation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ns.foundation.NSArray;
import ns.foundation.NSMutableArray;
import ns.foundation._NSFoundationCollection.NullHandling;

public class TestNSArrayAdditions extends BaseTestCase {
  public void testAsNSArrayArray() {
    String[] array = new String[] { "abc", "def", "ghi" };
    NSArray<String> array2 = NSArray.asNSArray(array);
    assertTrue(array2 instanceof NSArray<?>);
    assertEquals(3, array2.count());
    assertEquals("def", array2.objectAtIndex(1));
    array[1] = "fed";
    assertEquals("fed", array2.objectAtIndex(1));
  }

  public void testAsNSArrayArrayBoolean() {
    String[] str = new String[] { "abc", "def", "ghi" };
    NSArray<String> array = NSArray.asNSArray(str, NullHandling.NoCheck);
    assertEquals(array.getClass(), NSArray.class);
    assertEquals(3, array.count());
    assertEquals("def", array.objectAtIndex(1));
    str[1] = "fed";
    assertEquals("fed", array.objectAtIndex(1));
    str[0] = null;
    try {
      array = NSArray.asNSArray(str, NullHandling.CheckAndFail);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    array = NSArray.asNSArray(str, NullHandling.CheckAndSkip);
    assertEquals(2, array.size());
    array = NSArray.asNSArray(str, NullHandling.NoCheck);
    assertEquals(3, array.size());
  }

  public void testAsNSArrayListBoolean() {
    String[] array = new String[] { "abc", "def", "ghi" };
    List<String> list = new ArrayList<String>(Arrays.asList(array));
    NSArray<String> array2 = NSArray.asNSArray(list, NullHandling.NoCheck);
    assertEquals(array2.getClass(), NSArray.class);
    assertEquals(3, array2.count());
    assertEquals("def", array2.objectAtIndex(1));
    list.set(1, "fed");
    assertEquals("fed", array2.objectAtIndex(1));
    assertEquals("def", array[1]);

    list.set(0, null);
    try {
      array2 = NSArray.asNSArray(list, NullHandling.CheckAndFail);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    array2 = NSArray.asNSArray(list, NullHandling.NoCheck);
  }

  public void testAsNSMutableArrayArray() {
    String[] array = new String[] { "abc", "def", "ghi" };
    NSMutableArray<String> array2 = NSMutableArray.asNSMutableArray(array);
    assertEquals(array2.getClass(), NSMutableArray.class);
    assertEquals(3, array2.count());
    assertEquals("def", array2.objectAtIndex(1));
    array[1] = "fed";
    assertEquals("fed", array2.objectAtIndex(1));
    array2.set(1, "def");
    assertEquals("def", array[1]);
    try {
      array2.add("jkl");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException e) {
    }
  }

  public void testAsNSMutableArrayArrayBoolean() {
    String[] array = new String[] { "abc", "def", "ghi" };
    NSMutableArray<String> array2 = NSMutableArray.asNSMutableArray(array, NullHandling.NoCheck);
    assertEquals(array2.getClass(), NSMutableArray.class);
    assertEquals(3, array2.count());
    assertEquals("def", array2.objectAtIndex(1));
    array[1] = "fed";
    assertEquals("fed", array2.objectAtIndex(1));
    array2.set(1, "def");
    assertEquals("def", array[1]);
    array[0] = null;
    try {
      array2 = NSMutableArray.asNSMutableArray(array, NullHandling.CheckAndFail);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    array2 = NSMutableArray.asNSMutableArray(array, NullHandling.NoCheck);
  }

  public void testAsNSMutableArrayListBoolean() {
    String[] array = new String[] { "abc", "def", "ghi" };
    List<String> list = new ArrayList<String>(Arrays.asList(array));
    NSMutableArray<String> array2 = NSMutableArray.asNSMutableArray(list, NullHandling.NoCheck);
    assertEquals(array2.getClass(), NSMutableArray.class);
    assertEquals(3, array2.count());
    assertEquals("def", array2.objectAtIndex(1));
    list.set(1, "fed");
    assertEquals("fed", array2.objectAtIndex(1));
    assertEquals("def", array[1]);
    array2.set(1, "def");
    assertEquals("def", list.get(1));
    list.set(0, null);
    try {
      array2 = NSMutableArray.asNSMutableArray(list, NullHandling.CheckAndFail);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    array2 = NSMutableArray.asNSMutableArray(list, NullHandling.NoCheck);
  }

}
