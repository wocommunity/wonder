package com.webobjects.foundation;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.webobjects.foundation.NSComparator.ComparisonException;

import er.erxtest.ERXTestCase;

public class NSMutableArrayTest extends ERXTestCase {

	public void testNSMutableArray() {
		NSMutableArray<?> array = new NSMutableArray<Object>();
		assertTrue(array.isEmpty());
	}

	public void testNSMutableArrayE() {
		NSMutableArray<String> array = new NSMutableArray<String>("abc");
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}
	
	public void testNSMutableArrayEArray() {
		String[] str = new String[] {"abc", "def"};
		NSMutableArray<String> array = new NSMutableArray<String>(str);
		assertEquals(2, array.size());
		assertEquals("abc", array.get(0));
		assertEquals("def", array.get(1));
	}

	public void testNSMutableArrayEArrayNSRange() {
		String[] str = new String[] {"abc", "def"}; 
		NSRange range = new NSRange(1, 1);
		NSMutableArray<String> array = new NSMutableArray<String>(str, range);
		assertEquals(1, array.size());
		assertEquals("def", array.get(0));
	}
	
	public void testNSMutableArrayNSArrayOfQextendsE() {
		NSMutableArray<String> array = new NSMutableArray<String>("abc");
		NSMutableArray<String> array2 = new NSMutableArray<String>(array);
		assertEquals(1, array2.size());
		assertEquals("abc", array2.get(0));
	}
	
	public void testNSMutableArrayCollectionOfQextendsE() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("abc");
		list.add("def");
		
		NSArray<String> array = new NSArray<String>(list);
		assertEquals(2, array.size());
		
		list.add(null);
		try {
			array = new NSArray<String>(list);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testNSMutableArrayInt() {
		NSMutableArray<Integer> array = new NSMutableArray<Integer>(2);
		assertTrue(array.isEmpty());
	}

	public void testNSMutableArrayListOfQextendsENSRangeBoolean() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("abc");
		list.add(null);
		list.add("def");
		
		NSRange range = new NSRange(1, 2);
		
		NSArray<String> array = new NSArray<String>(list, range, true);
		assertEquals(1, array.size());
		
		try {
			array = new NSArray<String>(list, range, false);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAddObjectArg() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		array.add("abc");
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testAddIntObject() {
		NSMutableArray<String> array = new NSMutableArray<String>("abc");
		array.add(0, "def");
		assertEquals(2, array.size());
		assertEquals("def", array.get(0));
	}

	public void testAddAllCollection() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		List<String> arrayList = new ArrayList<String>();
		arrayList.add("abc");
		arrayList.add("def");
		array.addAll(arrayList);
		assertEquals(2, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testAddAllSubList() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		NSMutableArray<String> otherArray = new NSMutableArray<String>();
		otherArray.add("a");
		otherArray.add("b");
		otherArray.add("c");
		otherArray.add("d");
		otherArray.add("e");
		List<String> subList = otherArray.subList(1, 4);
		array.addAll(subList);
		assertEquals(3, array.size());
		assertEquals("b", array.get(0));
		assertEquals("c", array.get(1));
		assertEquals("d", array.get(2));
	}

	public void testAddAllIntCollection() {
		NSMutableArray<String> array = new NSMutableArray<String>("ghi");
		List<String> arrayList = new ArrayList<String>();
		arrayList.add("abc");
		arrayList.add("def");
		array.addAll(0, arrayList);
		assertEquals(3, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testAddAllIntSubList() {
		NSMutableArray<String> array = new NSMutableArray<String>("x");
		NSMutableArray<String> otherArray = new NSMutableArray<String>();
		otherArray.add("a");
		otherArray.add("b");
		otherArray.add("c");
		otherArray.add("d");
		otherArray.add("e");
		List<String> subList = otherArray.subList(1, 4);
		array.addAll(0, subList);
		assertEquals(4, array.size());
		assertEquals("b", array.get(0));
		assertEquals("x", array.get(3));
	}

	public void testAddObject() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		array.addObject("abc");
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testAddObjects() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		String[] strings = new String[] {"abc", "def"};
		array.addObjects(strings);
		assertEquals(2, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testAddObjectsFromArray() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		NSMutableArray<String> otherArray = new NSMutableArray<String>();
		otherArray.add("abc");
		otherArray.add("def");
		array.addObjectsFromArray(otherArray);
		assertEquals(2, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testClear() {
		NSMutableArray<String> array = new NSMutableArray<String>("abc");
		assertFalse(array.isEmpty());
		array.clear();
		assertTrue(array.isEmpty());
	}

  public void testClone() {
		NSMutableArray<String> array = new NSMutableArray<String>("abc");
		NSMutableArray<String> clone = (NSMutableArray<String>) array.clone();
		assertEquals(array, clone);
	}

	public void testImmutableClone() {
		NSMutableArray<String> array = new NSMutableArray<String>("abc");
		NSArray<String> clone = array.immutableClone();
		assertEquals(NSArray.class, clone.getClass());
		assertEquals(array, clone);
	}

	public void testRemoveInt() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		array.add("abc");
		array.add("def");
		
		array.remove(0);
		assertEquals(1, array.size());
		assertEquals("def", array.get(0));
	}

	public void testRemoveObject() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		array.add("abc");
		array.add("def");
		
		array.removeObject("abc");
		assertEquals(1, array.size());
		assertEquals("def", array.get(0));
	}

	public void testRemoveAllCollection() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		List<String> arrayList = new ArrayList<String>();
		arrayList.add("abc");
		arrayList.add("def");
		array.removeAll(arrayList);
		assertEquals(1, array.size());
		assertEquals("ghi", array.get(0));
	}

	public void testRetainAllCollection() {
		NSMutableArray<String> array = new NSMutableArray<String>();
		array.add("abc");
		array.add("def");
		
		List<String> arrayList = new ArrayList<String>();
		arrayList.add("abc");

		array.retainAll(arrayList);
		
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testSetIntObject() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		array.set(0, "123");
		assertEquals("123", array.get(0));
	}

	public void testInsertObjectAtIndex() {
		NSMutableArray<String> array = new NSMutableArray<String>("def");
		array.insertObjectAtIndex("abc", 0);
		assertEquals(2, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveAllObjects() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		assertFalse(array.isEmpty());
		array.removeAllObjects();
		assertTrue(array.isEmpty());
	}

	public void testRemoveIdenticalObjectObject() {
		String def = "def";
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", def });
		array.removeIdenticalObject(def);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveIdenticalObjectObjectNSRange() {
		String def = "def";
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", def });
		NSRange range = new NSRange(1, 1);
		array.removeIdenticalObject(def, range);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveLastObject() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def" });
		array.removeLastObject();
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveObjectObject() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def" });
		array.removeObject("def");
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveObjectObjectNSRange() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def" });
		NSRange range = new NSRange(1, 1);
		array.removeObject("def", range);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveObjectAtIndex() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def" });
		array.removeObjectAtIndex(1);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveObjects() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		String[] strings = new String[] {"def", "ghi"};
		array.removeObjects((Object[])strings);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveObjectsInArray() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		NSArray<String> strings = new NSArray<String>(new String[] { "def", "ghi" });
		array.removeObjectsInArray(strings);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	public void testRemoveObjectsInRange() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		NSRange range = new NSRange(1, 2);
		array.removeObjectsInRange(range);
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

	@SuppressWarnings("deprecation")
  public void testReplaceObjectAtIndexEInt() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		array.replaceObjectAtIndex(0, "123");
		assertEquals("123", array.get(0));
	}

	public void testReplaceObjectAtIndexIntE() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		array.replaceObjectAtIndex("123", 0);
		assertEquals("123", array.get(0));
	}

	public void testReplaceObjectsInRange() {
	  NSArray<String> array = new NSArray<String>(new String[] { "abc", "def", "ghi" });
	  NSArray<String> array2 = new NSArray<String>(new String[] { "123", "456" });
		NSRange sourceRange = new NSRange(1, 2);
		NSRange otherRange = new NSRange(0, 2);
		NSMutableArray<String>array3  = array.mutableClone();
		array3.replaceObjectsInRange(sourceRange, array2, otherRange);
		assertEquals(3, array3.size());
		assertEquals("abc", array3.get(0));
		assertEquals("123", array3.get(1));
		assertEquals("456", array3.get(2));
		
		array3 = array.mutableClone();
		sourceRange = new NSRange(0, 3);
		otherRange = new NSRange(0, 2);
    array3.replaceObjectsInRange(sourceRange, array2, otherRange);
    assertEquals(2, array3.size());
    assertEquals(array2, array3);

    array3 = array.mutableClone();
    sourceRange = new NSRange(2, 1);
    otherRange = new NSRange(0, 2);
    array3.replaceObjectsInRange(sourceRange, array2, otherRange);
    assertEquals(4, array3.size());
    assertEquals("abc", array3.get(0));
    assertEquals("def", array3.get(1));
    assertEquals("123", array3.get(2));		
    assertEquals("456", array3.get(3));   
	}

	public void testSetArray() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi" });
		array.setArray(new NSArray<String>("abc"));
		assertEquals(1, array.size());
		assertEquals("abc", array.get(0));
	}

  public void testSubList() {
    NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi", "jkl" });
    List<String> sublist = array.subList(2, 4);
    assertEquals(2, sublist.size());
    assertEquals("ghi", sublist.get(0));
  }
  
  public void testSubListAdd() {
    NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi", "jkl" });
    List<String> sublist = array.subList(1, 3);
    assertEquals(2, sublist.size());

    sublist.add("mno");
    assertEquals(3, sublist.size());
    assertEquals("mno", sublist.get(2));
    assertEquals(5, array.size());
    assertEquals("mno", array.get(3));
    assertEquals("jkl", array.get(4));    
  }
  
  public void testSubListAddNull() {
    NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi", "jkl" });
    List<String> sublist = array.subList(1, 3);
    try {
      sublist.add(null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testSubListRemove() {
    NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi", "jkl", "ghi" });
    List<String> sublist = array.subList(2, 4);

    sublist.remove("ghi");
    assertEquals(1, sublist.size());
    assertEquals(4, array.size());
    assertEquals("jkl", sublist.get(0));
    assertEquals("ghi", array.objectAtIndex(3));
    
    try {
      sublist.remove(null);
      fail("NullPointerException expected");
    } catch (NullPointerException e) {
    }
  }

	public void testSubListAddToJavaCollection() {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def", "ghi", "jkl" });
		List<String> sublist = array.subList(2, 4);
		Vector<String> javaCollection = new Vector<String>();
		javaCollection.addAll(sublist);
		assertEquals(2, javaCollection.size());
		assertEquals("ghi", javaCollection.get(0));
		assertEquals("jkl", javaCollection.get(1));
	}

	public void testSortUsingComparator() throws ComparisonException {
		NSMutableArray<String> array = new NSMutableArray<String>(new String[] { "abc", "def" });

		array.sortUsingComparator(NSComparator.AscendingStringComparator);
		assertEquals("abc", array.get(0));
		assertEquals("def", array.get(1));
		
		array.sortUsingComparator(NSComparator.DescendingStringComparator);
		assertEquals("def", array.get(0));
		assertEquals("abc", array.get(1));
		
		
		array = new NSMutableArray<String>(new String[] { "abc", "DEF" });
		array.sortUsingComparator(NSComparator.AscendingCaseInsensitiveStringComparator);
		assertEquals("abc", array.get(0));
		assertEquals("DEF", array.get(1));
		
		array.sortUsingComparator(NSComparator.DescendingCaseInsensitiveStringComparator);
		assertEquals("DEF", array.get(0));
		assertEquals("abc", array.get(1));
		
		
		NSMutableArray<Integer> intarray = new NSMutableArray<Integer>(new Integer[] { new Integer(1), new Integer(2) });
		intarray.sortUsingComparator(NSComparator.AscendingNumberComparator);
		assertEquals(Integer.valueOf(1), intarray.get(0));
		assertEquals(Integer.valueOf(2), intarray.get(1));
		
		intarray.sortUsingComparator(NSComparator.DescendingNumberComparator);
		assertEquals(Integer.valueOf(2), intarray.get(0));
		assertEquals(Integer.valueOf(1), intarray.get(1));
		
		
		NSTimestamp earlierTime = new NSTimestamp();
		NSTimestamp laterTime = earlierTime.timestampByAddingGregorianUnits(0,1,0,0,0,0);
		NSMutableArray<NSTimestamp> timearray = new NSMutableArray<NSTimestamp>(new NSTimestamp[] { earlierTime, laterTime });
		timearray.sortUsingComparator(NSComparator.AscendingTimestampComparator);
		assertEquals(earlierTime, timearray.get(0));
		assertEquals(laterTime, timearray.get(1));
		
		timearray.sortUsingComparator(NSComparator.DescendingTimestampComparator);
		assertEquals(laterTime, timearray.get(0));
		assertEquals(earlierTime, timearray.get(1));
	}

  public void testTakeValueForKey() {
    NSDictionary<?, ?>[] dicts = new NSDictionary[] {
        new NSMutableDictionary<String, String>("val1", "key"),
        new NSMutableDictionary<String, String>("val2", "key")
        };
    NSArray<NSDictionary<?, ?>> array = new NSArray<NSDictionary<?, ?>>(dicts);
    array.takeValueForKey("val3", "key3");
    
    NSDictionary<?, ?> dict1 = array.get(0);
    NSDictionary<?, ?> dict2 = array.get(1);
    
    assertEquals("val3", dict1.objectForKey("key3"));
    assertEquals("val3", dict2.objectForKey("key3"));
  }
}
