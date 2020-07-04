package com.webobjects.foundation;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import er.erxtest.ERXTestCase;

public class NSDictionaryTest extends ERXTestCase {

	public void testNSDictionary() {
		NSDictionary<Object, Object> dict = new NSDictionary<>();
		assertTrue(dict.isEmpty());
	}

	 public void testNSDictionaryIsImmutable() {
	    NSDictionary<Object, Object> dict = new NSDictionary<>();
	    try {
	      dict.put("abc", "def");
	      fail("NSDictionary is not immutable");
	    } catch (UnsupportedOperationException e) {
	    }
	 }
	
	public void testNSDictionaryMapOfKV() {
		Map<String, String> map = new HashMap<>();
		map.put("key", "value");
		NSDictionary<String, String> dict = new NSDictionary<>(map);
		assertEquals("value", dict.get("key"));
	}

	public void testNSDictionaryMapOfKVNull() {
		try {
			new NSDictionary<>((Map<String, String>)null);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
		}
	}

	public void testNSDictionaryMapOfKVInvalid() {
		Map<String, String> map = new HashMap<>();
		map.put("key", null);
		try {
			new NSDictionary<>(map);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testNSDictionaryMapOfKVBoolean() {
		Map<String, String> map = new HashMap<>();
		map.put("key", "value");
		NSDictionary<String, String> dict = new NSDictionary<>(map, true);
		assertEquals("value", dict.get("key"));
		dict = new NSDictionary<>(map, false);
		assertEquals("value", dict.get("key"));
	}

	public void testNSDictionaryMapOfKVBooleanNull() {
		try {
			new NSDictionary<>((Map<String, String>)null, true);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
		}
		try {
			new NSDictionary<>((Map<String, String>)null, false);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
		}
	}

	public void testNSDictionaryMapOfKVBooleanInvalid() {
		Map<String, String> map = new HashMap<>();
		map.put("key", null);
		try {
			new NSDictionary<>(map, false);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
		new NSDictionary<>(map, true);
	}
	
	public void testNSDictionaryNSArrayOfVNSArrayOfK() {
		NSArray<String> keys = new NSArray<>(new String[] { "key1", "key2" });
		NSArray<String> values = new NSArray<>(new String[] { "value1", "value2" });
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		assertEquals("value1", dict.get("key1"));
		assertEquals("value2", dict.get("key2"));
	}
	
	public void testNSDictionaryNSArrayOfVNSArrayOfKNull() {
	    NSArray<String> array = new NSArray<>(new String[] { "value1", "value2" });
	    try {
	      new NSDictionary<>((NSArray<String>)null, array);
	      fail("IllegalArgumentException expected");
	    } catch (IllegalArgumentException e) {
	    }
	    try {
	      new NSDictionary<>(array, (NSArray<String>)null);
	      fail("IllegalArgumentException expected");
	    } catch (IllegalArgumentException e) {
	    }
	    new NSDictionary<>((NSArray<String>)null, (NSArray<String>)null);
	}

	public void testNSDictionaryNSDictionaryOfKV() {
		NSMutableDictionary<String, String> mutableDict = new NSMutableDictionary<>("value", "key");
		NSDictionary<String, String> dict = new NSDictionary<>(mutableDict);
		NSDictionary<String, String> dict2 = new NSDictionary<>(dict);
		assertEquals(mutableDict, dict);
		assertEquals(mutableDict, dict2);
		mutableDict.put("key", "newValue");
		assertEquals("value", dict.get("key"));
		assertEquals("value", dict2.get("key"));
	}
	
	public void testNSDictionaryNSDictionaryOfKVNull() {
	  try {
	    new NSDictionary<>((NSDictionary<String, String>) null);
	    fail("NullPointerException expected");
	  } catch (NullPointerException e) {
	  }
  }

	public void testNSDictionaryVArrayKArray() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		assertEquals(2, dict.size());
		assertEquals("value1", dict.get("key1"));
		assertEquals("value2", dict.get("key2"));
	}

	public void testNSDictionaryVArrayKArrayNull() {
		String[] values = new String[] {"value1", "value2"};
		try {
			new NSDictionary<>((String[])null, values);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}

		try {
			new NSDictionary<>(values, (String[])null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}

		NSDictionary<String, String> dict = new NSDictionary<>((String[])null, (String[])null);
		assertTrue(dict.isEmpty());
	}

	
	public void testNSDictionaryVK() {
		NSDictionary<String, String> dict = new NSDictionary<>("value", "key");
		assertEquals("value", dict.get("key"));
	}

	public void testNSDictionaryVKNull() {
		try {
			new NSDictionary<>((String) null, "key");
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}

		try {
			new NSDictionary<>("value", (String) null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}

		try {
			new NSDictionary<>((String) null, (String) null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	
	public void testAllKeys() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSArray<String> keyArray = dict.allKeys();
		assertEquals(2, keyArray.size());
		assertTrue(keyArray.contains("key1"));
		assertTrue(keyArray.contains("key2"));
	}

	public void testAllKeysForObject() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value", "value"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSArray<String> keyArray = dict.allKeysForObject("value");
		assertEquals(2, keyArray.size());
		assertTrue(keyArray.contains("key1"));
		assertTrue(keyArray.contains("key2"));
	}
	
	public void testObjectsForKeys() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSArray<String> objectArray = dict.objectsForKeys(new NSArray<>(new String[] { "key1", "key2", "key3" }), "null");
		assertEquals(3, objectArray.size());
		assertTrue(objectArray.contains("value1"));
		assertTrue(objectArray.contains("value2"));
		assertTrue(objectArray.contains("null"));
	}

	public void testAllValues() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSArray<String> keyArray = dict.allValues();
		assertEquals(2, keyArray.size());
		assertTrue(keyArray.contains("value1"));
		assertTrue(keyArray.contains("value2"));
	}

	public void testCount() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		assertEquals(2, dict.count());
	}

	public void testEmptyDictionary() {
		assertTrue(NSDictionary.EmptyDictionary.isEmpty());
		assertTrue(NSDictionary.emptyDictionary().isEmpty());
	}

	public void testHashMap() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		Map<String, String> hashMap = dict.hashMap();
		assertEquals(2, hashMap.size());
		assertEquals("value1", hashMap.get("key1"));
		assertEquals("value2", dict.get("key2"));
	}

	public void testIsEqualToDictionary() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSDictionary<String, String> dict2 = new NSDictionary<>(values, keys);
		assertTrue(dict.isEqualToDictionary(dict2));
	}

	public void testImmutableClone() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSDictionary<String, String> clone = dict.immutableClone();
		
		assertEquals(NSDictionary.class, clone.getClass());
		assertEquals(clone, dict);
	}

	public void testMutableClone() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSDictionary<String, String> clone = dict.mutableClone();
		
		assertEquals(NSMutableDictionary.class, clone.getClass());
		assertEquals(clone, dict);
	}

	public void testKeyEnumerator() {
		NSDictionary<String, String> dict = new NSDictionary<>("value", "key");	
		Enumeration<String> e = dict.keyEnumerator();
		
		assertTrue(e.hasMoreElements());
		assertEquals("key", e.nextElement());
		assertFalse(e.hasMoreElements());
		try {
			e.nextElement();
			fail("Expected NoSuchElementException");
		} catch (NoSuchElementException ex) {} 
	}
	
	public void testObjectEnumerator() {
		NSDictionary<String, String> dict = new NSDictionary<>("value", "key");	
		Enumeration<String> e = dict.objectEnumerator();
		
		assertTrue(e.hasMoreElements());
		assertEquals("value", e.nextElement());
		assertFalse(e.hasMoreElements());
		try {
			e.nextElement();
			fail("Expected NoSuchElementException");
		} catch (NoSuchElementException ex) {} 
	}
	
	public void testObjectForKey() {
		NSDictionary<String, String> dict = new NSDictionary<>("value", "key");
		assertEquals("value", dict.objectForKey("key"));
	}

	public void testValueForKey() {
		NSDictionary<String, String> dict = new NSDictionary<>("value", "key");
		assertEquals("value", dict.valueForKey("key"));
	}

	public void testValueForKeyPath() {
		NSDictionary<String, String> dict = new NSDictionary<>("value", "key");
		assertEquals("value", dict.valueForKeyPath("key"));
		
		dict = new NSDictionary<>("value", "key.path");
		assertEquals("value", dict.valueForKeyPath("key.path"));
		
		NSDictionary<String, String> subDict = new NSDictionary<>("value", "path");
		NSDictionary<String, NSDictionary<String,String>> dict2 = new NSDictionary<String, NSDictionary<String,String>>(subDict, "key");
		assertEquals("value", dict2.valueForKeyPath("key.path"));
	}

  public void testClone() {
		String[] keys = new String[] {"key1", "key2"};
		String[] values = new String[] {"value1", "value2"};
		NSDictionary<String, String> dict = new NSDictionary<>(values, keys);
		NSDictionary<String, String> clone = (NSDictionary<String, String>) dict.clone();
		
		assertEquals(NSDictionary.class, clone.getClass());
		assertEquals(clone, dict);
	}

	public void testPutObjectObject() {
		try {
			NSDictionary.emptyDictionary().put("key", "value");
			fail("Put should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testPutAllMap() {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("key", "value");
			NSDictionary.emptyDictionary().putAll(map);
			fail("PutAll should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}	
	}

	public void testRemoveObject() {
		try {
			NSDictionary.emptyDictionary().remove("abc");
			fail("RemoveObject should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testClear() {
		try {
			NSDictionary.EmptyDictionary.clear();
			fail("Clear should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testUnknownKeyException() {
		NSDictionary<String, String> dict = new NSDictionary<>("John", "name");
		try {
			throw new NSKeyValueCoding.UnknownKeyException("error", dict, "name");
		} catch (NSKeyValueCoding.UnknownKeyException e) {
		  return;
		} catch (Exception e ) {
		  fail("Unable to throw unknown key exception");
		}
	}
	
	public void testCreateNSDictionaryOfZeroEntries() {
		NSDictionary<?, ?> dict = NSDictionary.of();
		
		assertTrue(dict.isEmpty());
		assertTrue(NSDictionary.EmptyDictionary == dict);
	}
	
	public void testCreateNSDictionaryOfOneEntry() {
		NSDictionary<String, String> dict = NSDictionary.of("k1", "v1");
		
		assertEquals(1, dict.size());
		assertEquals("v1", dict.get("k1"));
	}
	
	public void testCreateNSDictionaryOfTwoEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1",
				"k2", "v2");
		
		assertEquals(2, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
	}
	
	public void testCreateNSDictionaryOfThreeEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3");
		
		assertEquals(3, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
	}
	
	public void testCreateNSDictionaryOfFourEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4");
		
		assertEquals(4, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
	}
	
	public void testCreateNSDictionaryOfFiveEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4", 
				"k5", "v5");
		
		assertEquals(5, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
		assertEquals("v5", dict.get("k5"));
	}
	
	public void testCreateNSDictionaryOfSixEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4", 
				"k5", "v5", 
				"k6", "v6");
		
		assertEquals(6, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
		assertEquals("v5", dict.get("k5"));
		assertEquals("v6", dict.get("k6"));
	}
	
	public void testCreateNSDictionaryOfSevenEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4", 
				"k5", "v5", 
				"k6", "v6", 
				"k7", "v7");
		
		assertEquals(7, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
		assertEquals("v5", dict.get("k5"));
		assertEquals("v6", dict.get("k6"));
		assertEquals("v7", dict.get("k7"));
	}
	
	public void testCreateNSDictionaryOfEightEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4", 
				"k5", "v5", 
				"k6", "v6", 
				"k7", "v7", 
				"k8", "v8");
		
		assertEquals(8, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
		assertEquals("v5", dict.get("k5"));
		assertEquals("v6", dict.get("k6"));
		assertEquals("v7", dict.get("k7"));
		assertEquals("v8", dict.get("k8"));
	}
	
	public void testCreateNSDictionaryOfNineEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4", 
				"k5", "v5", 
				"k6", "v6", 
				"k7", "v7", 
				"k8", "v8", 
				"k9", "v9");
		
		assertEquals(9, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
		assertEquals("v5", dict.get("k5"));
		assertEquals("v6", dict.get("k6"));
		assertEquals("v7", dict.get("k7"));
		assertEquals("v8", dict.get("k8"));
		assertEquals("v9", dict.get("k9"));
	}
	
	public void testCreateNSDictionaryOfTenEntries() {
		NSDictionary<String, String> dict = NSDictionary.of(
				"k1", "v1", 
				"k2", "v2", 
				"k3", "v3", 
				"k4", "v4", 
				"k5", "v5", 
				"k6", "v6", 
				"k7", "v7", 
				"k8", "v8", 
				"k9", "v9",
				"k10", "v10");
		
		assertEquals(10, dict.size());
		assertEquals("v1", dict.get("k1"));
		assertEquals("v2", dict.get("k2"));
		assertEquals("v3", dict.get("k3"));
		assertEquals("v4", dict.get("k4"));
		assertEquals("v5", dict.get("k5"));
		assertEquals("v6", dict.get("k6"));
		assertEquals("v7", dict.get("k7"));
		assertEquals("v8", dict.get("k8"));
		assertEquals("v9", dict.get("k9"));
		assertEquals("v10", dict.get("k10"));
	}
}
