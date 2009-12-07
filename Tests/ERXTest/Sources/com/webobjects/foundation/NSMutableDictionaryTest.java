package com.webobjects.foundation;

import java.util.HashMap;
import java.util.Map;

import er.erxtest.ERXTestCase;

public class NSMutableDictionaryTest extends ERXTestCase {

	public void testNSMutableDictionary() {
		NSMutableDictionary<?, ?> dict = new NSMutableDictionary<Object, Object>();
		assertTrue(dict.isEmpty());
	}

	public void testNSMutableDictionaryInt() {
		NSMutableDictionary<?, ?> dict = new NSMutableDictionary<Object, Object>(5);
		assertTrue(dict.isEmpty());
	}

	public void testNSMutableDictionaryMapOfKV() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(map);
		assertEquals("value", dict.get("key"));
	}

	public void testNSMutableDictionaryNSArrayOfVNSArrayOfK() {
		NSArray<String> keys = new NSArray<String>(new String[] { "key1", "key2" });
		NSArray<String> values = new NSArray<String>(new String[] { "value1", "value2" });
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(values, keys);
		assertEquals("value1", dict.get("key1"));
		assertEquals("value2", dict.get("key2"));
	}

	public void testNSMutableDictionaryNSMutableDictionaryOfKV() {
		NSMutableDictionary<String, String> mutableDict = new NSMutableDictionary<String, String>("value", "key");
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(mutableDict);
		assertFalse(dict.isEmpty());
		mutableDict.put("key", "newValue");
		assertEquals("value", dict.get("key"));
	}

	public void testNSMutableDictionaryVArrayKArray() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(values, keys);
		assertEquals("value1", dict.get("key1"));
		assertEquals("value2", dict.get("key2"));
	}

	public void testNSMutableDictionaryVK() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
		assertEquals("value", dict.get("key"));
	}

	public void testPutObjectObject() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>();
		dict.put("key", "value");
		assertEquals("value", dict.get("key"));
	}

	public void testSetObjectForKey() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>();
		dict.setObjectForKey("value", "key");
		assertEquals("value", dict.get("key"));
	}

	public void testPutAllMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		map.put("key2", "value2");
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>();
		dict.putAll(map);
		assertEquals("value", dict.get("key"));
		assertEquals("value2", dict.get("key2"));
	}

	public void testAddEntriesFromDictionary() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<String, String>(values, keys);
		
		NSMutableDictionary<String, String> mutableDict = new NSMutableDictionary<String, String>();
		mutableDict.addEntriesFromDictionary(dict);
		
		assertEquals("value1", dict.get("key1"));
		assertEquals("value2", dict.get("key2"));
	}
	
	public void testRemoveObject() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
		dict.remove("key");
		assertTrue(dict.isEmpty());
	}

	public void testClear() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
		dict.clear();
		assertTrue(dict.isEmpty());
	}
	
	public void testRemoveAllObjects() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
		dict.removeAllObjects();
		assertTrue(dict.isEmpty());
	}

	public void testRemoveObjectForKey() {
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
		dict.removeObjectForKey("key");
		assertTrue(dict.isEmpty());
	}

	public void testRemoveObjectsForKeys() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(values, keys);
		dict.removeObjectsForKeys(new NSArray<String>(new String[] { "key1", "key2" }));
		assertTrue(dict.isEmpty());
	}

	public void testSetDictionary() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(values, keys);
		dict.setDictionary(new NSDictionary<String, String>("value3", "key3"));
		assertEquals("value3", dict.get("key3"));
	}

	public void testImmutableClone() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<String, String>(values, keys);
		NSDictionary<String, String> clone = dict.immutableClone();
		
		assertEquals(NSDictionary.class, clone.getClass());
		assertEquals(clone, dict);
	}

  public void testClone() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>(values, keys);
		NSMutableDictionary<String, String> clone = (NSMutableDictionary<String, String>) dict.clone();
		
		assertEquals(NSMutableDictionary.class, clone.getClass());
		assertEquals(clone, dict);
	}

	public void testTakeValueForKeyPath() {
	  NSDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
	  dict.takeValueForKeyPath("newValue", "key");
	  assertEquals("newValue", dict.get("key"));

	  NSDictionary<String, String> subDict = new NSMutableDictionary<String, String>("value", "path");
	  NSDictionary<String, ?> dict2 = new NSDictionary<String, NSDictionary<?, ?>>(subDict, "key");
	  dict2.takeValueForKeyPath("newValue", "key.path");
	  assertEquals("newValue", dict2.valueForKeyPath("key.path"));
	}
	
  public void testTakeValueForKey() {
    NSDictionary<String, String> dict = new NSMutableDictionary<String, String>("value", "key");
    dict.takeValueForKey("newValue", "key");
    assertEquals("newValue", dict.get("key"));
  }

}
