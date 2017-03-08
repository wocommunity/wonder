package com.webobjects.foundation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import er.erxtest.ERXTestCase;


public class NSMutableSetTest extends ERXTestCase {

	public void testNSMutableSet() {
		NSMutableSet<?> set = new NSMutableSet<>();
		assertTrue(set.isEmpty());
	}

	public void testNSMutableSetCollectionOfQextendsE() {
		ArrayList<String> list = new ArrayList<>();
		list.add("abc");
		list.add("abc");
		
		NSMutableSet<String> set = new NSMutableSet<>(list);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testNSMutableSetE() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		assertTrue(set.contains("abc"));
	}

	public void testNSMutableSetEArray() {
		String[] strings = new String[] {"abc", "abc"};
		NSMutableSet<String> set = new NSMutableSet<>(strings);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testNSMutableSetInt() {
		NSMutableSet<?> set = new NSMutableSet<>(1);
		assertTrue(set.isEmpty());
	}

	public void testNSMutableSetNSArrayOfQextendsE() {
		NSMutableArray<String> list = new NSMutableArray<>();
		list.add("abc");
		list.add("abc");
		
		NSMutableSet<String> set = new NSMutableSet<>(list);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testNSMutableSetNSSetOfQextendsE() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		NSMutableSet<String> copy = new NSMutableSet<>(set);
		assertEquals(1, copy.size());
		assertTrue(copy.contains("abc"));
	}

	public void testNSMutableSetSetOfQextendsEBoolean() {
		Set<String> source = new HashSet<>();
		source.add("abc");
		source.add(null);
		
		NSMutableSet<String> set = new NSMutableSet<>(source, true);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
		
		try {
			set = new NSMutableSet<>(source, false);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAddObject() {
		NSMutableSet<String> set = new NSMutableSet<>();
		set.addObject("abc");
		assertTrue(set.contains("abc"));
	}
	
	public void testAddObjects() {
		NSMutableSet<String> set = new NSMutableSet<>();
		set.addObjects("abc", "123");
		assertEquals(2, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testAddObjectsFromArray() {
		NSMutableArray<String> list = new NSMutableArray<>();
		list.add("abc");
		list.add("abc");
		
		NSMutableSet<String> set = new NSMutableSet<>();
		set.addObjectsFromArray(list);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testRemoveAllObjects() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		set.removeAllObjects();
		assertTrue(set.isEmpty());
	}

	public void testRemoveObject() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		Object removed = set.removeObject("abc");
		assertTrue(set.isEmpty());
		assertEquals("abc", removed);
	}

	public void testSubtractSet() {
		NSMutableSet<String> otherSet = new NSMutableSet<>();
		otherSet.add("abc");
		otherSet.add("123");
		
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		set.subtractSet(otherSet);
		assertTrue(set.isEmpty());
	}

	public void testUnionSet() {
		NSMutableSet<String> otherSet = new NSMutableSet<>();
		otherSet.add("abc");
		otherSet.add("123");
		
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		set.unionSet(otherSet);
		assertEquals(2, set.size());
		assertTrue(set.contains("abc"));
		assertTrue(set.contains("123"));
	}

	public void testSetSet() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		NSMutableSet<String> otherSet = new NSMutableSet<>("123");
		otherSet.add("def");
		
		set.setSet(otherSet);
		assertEquals(otherSet, set);
	}

  public void testClone() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		NSMutableSet<String> clone = (NSMutableSet<String>) set.clone();
		assertEquals(set, clone);
		
		assertNotSame(set, clone);
	}

	public void testImmutableClone() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		NSSet<String> clone = set.immutableClone();
		assertEquals(1, clone.size());
		assertTrue(clone.contains("abc"));
		
		assertNotSame(set, clone);
		
		assertEquals(NSSet.class, clone.getClass());
	}

	public void testClear() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		set.clear();
		assertTrue(set.isEmpty());
	}

	public void testAdd() {
		NSMutableSet<String> set = new NSMutableSet<>();
		set.add("abc");
		assertTrue(set.contains("abc"));
	}

	public void testAddAllCollection() {
		NSMutableArray<String> list = new NSMutableArray<>();
		list.add("abc");
		list.add("abc");
		
		NSMutableSet<String> set = new NSMutableSet<>();
		set.addAll(list);
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}

	public void testRemove() {
		NSMutableSet<String> set = new NSMutableSet<>("abc");
		boolean removed = set.remove("abc");
		assertTrue(set.isEmpty());
		assertTrue(removed);
	}

	public void testRemoveAllCollection() {
		NSMutableArray<String> list = new NSMutableArray<>();
		list.add("abc");
		list.add("123");
		
		NSMutableSet<String> set = new NSMutableSet<>();
		set.addAll(list);
		set.removeAll(list);
		assertTrue(set.isEmpty());
	}

	public void testRetainAllCollection() {
		NSMutableArray<String> list = new NSMutableArray<>();
		list.add("abc");
		list.add("123");
		
		NSMutableSet<String> set = new NSMutableSet<>();
		set.addAll(list);
		
		list.remove("123");	
		set.retainAll(list);
		
		assertEquals(1, set.size());
		assertTrue(set.contains("abc"));
	}
	
	public void testNSMutableSetIterator() {
		NSMutableSet<String> set = new NSMutableSet<>("abc", "def");
		int size = set.size();
		NSMutableSet<String> check = set.mutableClone();
		
		Iterator<String> iterator = set.iterator();
		assertTrue(iterator.hasNext());
		assertTrue(check.remove(iterator.next()));
		assertTrue(iterator.hasNext());
		assertTrue(check.remove(iterator.next()));
		assertFalse(iterator.hasNext());
		assertTrue(check.isEmpty());
		try {
			iterator.next(); // no items left, should throw NoSuchElementException
			fail();
		} catch (NoSuchElementException e) {
			// test passed
		}
		
		iterator = set.iterator();
		check = set.mutableClone();
		try {
			iterator.remove(); // not called next() before, should throw IllegalStateException
			fail();
		} catch (IllegalStateException e) {
			// test passed
		}
		
		for (int i = 0; i < size; i++) {
			String currentItem = iterator.next();
			iterator.remove();
			assertFalse(set.contains(currentItem));
			check.remove(currentItem);
		}
		assertTrue(set.isEmpty());
		assertTrue(check.isEmpty());
		
		try {
			iterator.remove(); // already called remove before, should throw IllegalStateException
			fail();
		} catch (IllegalStateException e) {
			// test passed
		}
	}
}
