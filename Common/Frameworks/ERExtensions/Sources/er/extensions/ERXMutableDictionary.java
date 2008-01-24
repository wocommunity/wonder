package er.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Adds {@link java.util.Map} functionality to NSMutableDictionary and has
 * helpers to en- and decode from database field.
 * <code>ERPrototype name = mutableDictionary</code>
 */
public class ERXMutableDictionary extends NSMutableDictionary implements Map {

	public static final long serialVersionUID = 8091318522043166356L;

	public static NSData toBlob(NSDictionary d) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bout);
			oos.writeObject(d);
			oos.close();
			NSData sp = new NSData(bout.toByteArray());
			return sp;
		}
		catch (IOException e) {
			// shouldn't ever happen, as we only write to memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static NSData toBlob(ERXMutableDictionary dict) {
		return toBlob((NSDictionary) dict);
	}

	public static NSDictionary fromBlob(NSData d) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
			ObjectInputStream ois = new ObjectInputStream(bis);
			NSDictionary dd = (NSDictionary) ois.readObject();
			ois.close();
			return dd;
		}
		catch (IOException e) {
			// shouldn't ever happen, as we only read from memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		catch (ClassNotFoundException e) {
			// might happen, but it doesn't help us much to know it
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static NSDictionary fromPropertyList(String plist) {
		NSDictionary dict = (NSDictionary) NSPropertyListSerialization.propertyListFromString(plist);
		return new ERXMutableDictionary(dict);
	}

	public static String toPropertyList(NSDictionary dict) {
		String plist = NSPropertyListSerialization.stringFromPropertyList(dict);
		return plist;
	}

	public String toPropertyList() {
		String plist = NSPropertyListSerialization.stringFromPropertyList(this);
		return plist;
	}

	public NSData toBlob() {
		return toBlob(this);
	}

	public ERXMutableDictionary(NSDictionary d) {
		super(d);
	}

	public ERXMutableDictionary() {
		super();
	}

	@Override
	public Object clone() {
		return new ERXMutableDictionary(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		removeAllObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object arg0) {
		return objectForKey(arg0) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object arg0) {
		return allValues().containsObject(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set entrySet() {
		throw new IllegalAccessError("not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public Object get(Object arg0) {
		return objectForKey(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return count() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set keySet() {
		throw new IllegalAccessError("not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object put(Object arg0, Object arg1) {
		Object prev = objectForKey(arg0);
		setObjectForKey(arg1, arg0);
		return prev;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map arg0) {
		throw new IllegalAccessError("not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public Object remove(Object arg0) {
		return removeObjectForKey(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		return count();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection values() {
		return (Collection) allValues();
	}

	@Override
	public NSArray allValues() {
		NSArray av = super.allValues();
		return new ERXMutableArray(av);
	}

	/**
	 * return the string value of an object for key
	 * 
	 * @param key
	 *            the key which is linked to the object
	 * @return if objectForKey return a non null value this method returns the
	 *         toString value from the object
	 */
	public String stringObjectForKey(String key) {
		Object o = objectForKey(key);
		return o == null ? null : o.toString();
	}

	/**
	 * @param key
	 */
	public Boolean booleanObjectForKey(String key) {
		Object o = objectForKey(key);
		return o == null ? null : ERXValueUtilities.booleanValue(o) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Simple thread safe wrapper. May or may not be correct, but it doesn't
	 * matter as you will never, *ever* call this directly, but call <code>
	 * ERXMutableDictionary.synchronizedDictionary();
	 * </code>
	 * instead and we will fix all the bugs in due time.
	 * 
	 * @author ak
	 * 
	 */

	public static class ThreadSafeDictionary extends ERXMutableDictionary {

		public ThreadSafeDictionary(NSMutableDictionary dictionary) {
			super(dictionary);
		}

		@Override
		public synchronized void addEntriesFromDictionary(NSDictionary otherDictionary) {
			super.addEntriesFromDictionary(otherDictionary);
		}

		@Override
		public synchronized NSDictionary immutableClone() {
			return super.immutableClone();
		}

		@Override
		public synchronized NSMutableDictionary mutableClone() {
			return super.mutableClone();
		}

		@Override
		public synchronized void removeAllObjects() {
			super.removeAllObjects();
		}

		@Override
		public synchronized Object removeObjectForKey(Object key) {
			return super.removeObjectForKey(key);
		}

		@Override
		public synchronized void removeObjectsForKeys(NSArray keys) {
			super.removeObjectsForKeys(keys);
		}

		@Override
		public synchronized void setDictionary(NSDictionary otherDictionary) {
			super.setDictionary(otherDictionary);
		}

		@Override
		public synchronized void setObjectForKey(Object object, Object key) {
			super.setObjectForKey(object, key);
		}

		@Override
		public synchronized void takeValueForKey(Object value, String key) {
			super.takeValueForKey(value, key);
		}

		@Override
		protected synchronized void _clearDeletionsAndCollisions() {
			super._clearDeletionsAndCollisions();
		}

		@Override
		protected synchronized void _ensureCapacity(int capacity) {
			super._ensureCapacity(capacity);
		}

		@Override
		protected synchronized void _initializeDictionary() {
			super._initializeDictionary();
		}

		@Override
		public synchronized int _shallowHashCode() {
			return super._shallowHashCode();
		}

		@Override
		public synchronized NSArray allKeys() {
			return super.allKeys();
		}

		@Override
		public synchronized NSArray allKeysForObject(Object object) {
			return super.allKeysForObject(object);
		}

		@Override
		public synchronized Class classForCoder() {
			return super.classForCoder();
		}

		@Override
		public synchronized int count() {
			return super.count();
		}

		@Override
		public synchronized void encodeWithCoder(NSCoder coder) {
			super.encodeWithCoder(coder);
		}

		@Override
		public synchronized boolean equals(Object object) {
			return super.equals(object);
		}

		@Override
		public synchronized int hashCode() {
			return super.hashCode();
		}

		@Override
		public synchronized HashMap hashMap() {
			Object keys[] = keysNoCopy();
			int c = keys.length;
			HashMap map = new HashMap(c <= 0 ? 1 : c);
			for (int i = 0; i < c; i++) {
				map.put(keys[i], objectForKey(keys[i]));
			}

			return map;
		}

		@Override
		public synchronized Hashtable hashtable() {
			return super.hashtable();
		}

		@Override
		public synchronized boolean isEqualToDictionary(NSDictionary otherDictionary) {
			return super.isEqualToDictionary(otherDictionary);
		}

		@Override
		public synchronized Enumeration keyEnumerator() {
			return super.keyEnumerator();
		}

		@Override
		protected synchronized Object[] keysNoCopy() {
			return super.keysNoCopy();
		}

		@Override
		public synchronized Enumeration objectEnumerator() {
			return super.objectEnumerator();
		}

		@Override
		public synchronized Object objectForKey(Object key) {
			return super.objectForKey(key);
		}

		@Override
		public synchronized NSArray objectsForKeys(NSArray keys, Object notFoundMarker) {
			return super.objectsForKeys(keys, notFoundMarker);
		}

		@Override
		protected synchronized Object[] objectsNoCopy() {
			return super.objectsNoCopy();
		}

		@Override
		public synchronized void takeValueForKeyPath(Object value, String keyPath) {
			super.takeValueForKeyPath(value, keyPath);
		}

		@Override
		public synchronized String toString() {
			return super.toString();
		}

		@Override
		public synchronized Object valueForKey(String key) {
			return super.valueForKey(key);
		}

		@Override
		public synchronized Object valueForKeyPath(String keyPath) {
			return super.valueForKeyPath(keyPath);
		}
	}

	/**
	 * Returns a thread-safe mutable wrapper for the given mutable dictionary.
	 * 
	 * @param dict
	 *            the dictionary to make thread-safe
	 * @return a thread-safe wrapper around the given dictionary
	 */
	public static NSMutableDictionary synchronizedDictionary(NSMutableDictionary dict) {
		return new ThreadSafeDictionary(dict);
	}

	/**
	 * Returns a new thread-safe mutable dictionary.
	 * 
	 * @return a new thread-safe mutable dictionary
	 */
	public static NSMutableDictionary synchronizedDictionary() {
		return ERXMutableDictionary.synchronizedDictionary(new ERXMutableDictionary());
	}

	/**
	 * Returns either a thread-safe wrapper for a mutable dictionary, or just
	 * returns dict if the dictionary is not mutable.
	 * 
	 * @param dict
	 *            the dictionary to make thread-safe
	 * @return a thread-safe dictionary
	 */
	public static NSDictionary synchronizedDictionary(NSDictionary dict) {
		if (!(dict instanceof NSMutableDictionary)) {
			return dict;
		}
		return ERXMutableDictionary.synchronizedDictionary((NSMutableDictionary) dict);
	}
}
