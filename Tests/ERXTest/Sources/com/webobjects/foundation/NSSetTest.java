package com.webobjects.foundation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import er.erxtest.ERXTestCase;


public class NSSetTest extends ERXTestCase {

	public void testNSSet() {
		NSSet<String> set = new NSSet<String>();
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
	}

	public void testNSSetCollectionOfQextendsE() {
		Collection<String> list = new ArrayList<String>();
		list.add("abc");
		list.add("abc");
		
		NSSet<String> set = new NSSet<String>(list);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testNSSetE() {
		NSSet<String> set = new NSSet<String>("abc");
		assertTrue(set.contains("abc"));
	}

	public void testNSSetEArray() {
		String[] strings = new String[] {"abc", "abc"};
		NSSet<String> set = new NSSet<String>(strings);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testNSSetNSArrayOfQextendsE() {
		NSMutableArray<String> list = new NSMutableArray<String>();
		list.add("abc");
		list.add("abc");
		
		NSSet<String> set = new NSSet<String>(list);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testNSSetNSSetOfQextendsE() {
		NSSet<String> set = new NSSet<String>("abc");
		NSSet<String> copy = new NSSet<String>(set);
		assertEquals(1, copy.size());
		assertTrue(copy.contains("abc"));
	}

	public void testNSSetSetOfQextendsEBoolean() {
		Set<String> source = new HashSet<String>();
		source.add("abc");
		source.add(null);
		
		NSSet<String> set = new NSSet<String>(source, true);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
		
		try {
			set = new NSSet<String>(source, false);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
		
		source = new NSSet<String>(new String[] { "abc", "def" });
		set = new NSSet<String>(source, false);
		assertEquals(source, set);
	}

	public void testAllObjects() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSArray<String> allObjects = set.allObjects();
		assertEquals(2, allObjects.size());
		assertTrue(allObjects.contains("abc"));
		assertTrue(allObjects.contains("123"));
	}

	public void testAnyObject() {
		NSSet<String> set = new NSSet<String>("abc");
		Object object = set.anyObject();
		assertEquals("abc", object);
	}

	public void testContainsObject() {
		NSSet<String> set = new NSSet<String>("abc");
		assertTrue(set.containsObject("abc"));
		assertFalse(set.containsObject("123"));
	}

	public void testCount() {
		NSSet<String> set = new NSSet<String>("abc");
		assertEquals(1, set.count());
		
		set = new NSSet<String>(new String[] { "abc", "123", "abc" });
		assertEquals(2, set.count());
	}

	public void testEmptySet() {
		NSSet<String> set = NSSet.emptySet();
		assertTrue(set.isEmpty());
	}

	public void testHashSet() {
		NSSet<String> set = new NSSet<String>("abc");
		HashSet<String> hashSet = set.hashSet();
		assertEquals(1, hashSet.size());
		assertTrue(hashSet.contains("abc"));
	}

  public void testClone() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSSet<String> clone = (NSSet<String>) set.clone();
		assertEquals(set, clone);
	}

	public void testImmutableClone() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSSet<String> clone = set.immutableClone();
		assertEquals(set, clone);
		assertEquals(NSSet.class, clone.getClass());
	}

	public void testMutableClone() {
		NSSet<String> set = new NSSet<String>("abc");
		NSMutableSet<String> clone = set.mutableClone();
		assertEquals(1, clone.size());
		assertTrue(clone.contains("abc"));
	}

	public void testIntersectsSet() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSSet<String> set2 = new NSSet<String>("abc");
		assertTrue(set.intersectsSet(set2));
		
		NSSet<String> set3 = new NSSet<String>("def");
		assertFalse(set.intersectsSet(set3));
	}

	public void testIsEqualToSet() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSSet<String> set2 = new NSSet<String>(new String[] { "abc", "123" });
		assertTrue(set.isEqualToSet(set2));
		
		NSSet<String> set3 = new NSSet<String>("abc");
		assertFalse(set.isEqualToSet(set3));
	}

	public void testIsSubsetOfSet() {
		NSSet<String> set = new NSSet<String>("abc");
		NSSet<String> set2 = new NSSet<String>(new String[] { "abc", "123" });
		assertTrue(set.isSubsetOfSet(set2));
		assertFalse(set2.isSubsetOfSet(set));
		
		NSSet<String> set3 = new NSSet<String>("def");
		assertFalse(set.isSubsetOfSet(set3));
	}

	public void testMember() {
		NSSet<String> set = new NSSet<String>("abc");
		assertTrue(set.member("abc").equals("abc"));
		assertFalse(set.member("123") != null);
	}

	public void testSetByIntersectingSet() {
		NSSet<String> set = new NSSet<String>("abc");
		NSSet<String> set2 = new NSSet<String>(new String[] { "abc", "123" });
		
		NSSet<String> intersection = set.setByIntersectingSet(set2);
		assertEquals(1, intersection.size());
		assertTrue(intersection.contains("abc"));
		
		set2 = new NSSet<String>("123");
		intersection = set.setByIntersectingSet(set2);
		assertTrue(intersection.isEmpty());
	}

	public void testSetBySubtractingSet() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSSet<String> set2 = new NSSet<String>("123");
		
		NSSet<String> difference = set.setBySubtractingSet(set2);
		assertEquals(1, difference.size());
		assertTrue(difference.contains("abc"));
		
		set2 = new NSSet<String>("def");
		difference = set.setBySubtractingSet(set2);
		assertEquals(set, difference);
	}

	public void testSetByUnioningSet() {
		NSSet<String> set = new NSSet<String>(new String[] { "abc", "123" });
		NSSet<String> set2 = new NSSet<String>("def");
		
		NSSet<String> union = set.setByUnioningSet(set2);
		assertEquals(3, union.size());
		
		set2 = new NSSet<String>("abc");
		union = set.setByUnioningSet(set2);
		assertEquals(set.size(), union.size());
	}

	public void testClear() {
		try {
			new NSSet<String>("abc").clear();
			fail("Clear should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testAdd() {
		try {
			NSSet.emptySet().add("abc");
			fail("Add should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testAddAllCollection() {
		try {
			HashSet<String> set = new HashSet<String>();
			set.add("abc");
			set.add("123");
			NSSet.emptySet().addAll(set);
			fail("AddAll should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testRemove() {
		try {
		  NSSet<String> set = new NSSet<String>(new String[] { "abc" });
			set.remove("abc");
			fail("Clear should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testRemoveAllCollection() {
		try {
			HashSet<String> hashset = new HashSet<String>();
			hashset.add("abc");
			hashset.add("123");
			NSSet<String> set = new NSSet<String>(new String[] { "abc" });
			set.removeAll(hashset);
			fail("RemoveAll should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testRetainAllCollection() {
		try {
			HashSet<String> hashset = new HashSet<String>();
			hashset.add("abc");
			hashset.add("123");
			NSSet<String> set = new NSSet<String>(new String[] { "abc", "def" });
			set.retainAll(hashset);
			fail("RetainAll should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}
	
	public void testNSSetIterator() {
		NSSet<String> set = new NSSet<String>("abc", "def");
		NSMutableSet<String> check = set.mutableClone();
		
		Iterator<String> iterator = set.iterator();
		assertTrue(iterator.hasNext());
		assertTrue(check.remove(iterator.next()));
		assertTrue(iterator.hasNext());
		assertTrue(check.remove(iterator.next()));
		assertFalse(iterator.hasNext());
		assertTrue(check.isEmpty());
		try {
			iterator.next(); // no items left, should throw ArrayIndexOutOfBoundsException
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
			// test passed
		}
		
		iterator = set.iterator();
		try {
			iterator.remove(); // immutable, should throw UnsupportedOperationException
			fail();
		} catch (UnsupportedOperationException e) {
			// test passed
		}
	}
}
