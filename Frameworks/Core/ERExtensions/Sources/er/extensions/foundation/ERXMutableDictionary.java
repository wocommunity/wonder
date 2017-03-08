package er.extensions.foundation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

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
 * @param <V>
 */
public class ERXMutableDictionary<K,V> extends NSMutableDictionary<K,V> {

	public static final long serialVersionUID = 8091318522043166356L;

	public static NSData toBlob(NSDictionary<?,?> d) {
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bout)) {
			oos.writeObject(d);
			NSData sp = new NSData(bout.toByteArray());
			return sp;
		}
		catch (IOException e) {
			// shouldn't ever happen, as we only write to memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static NSData toBlob(ERXMutableDictionary<?,?> dict) {
		return toBlob((NSDictionary<?,?>) dict);
	}

	@SuppressWarnings("unchecked")
	public static NSDictionary fromBlob(NSData d) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes()); ObjectInputStream ois = new ERXMappingObjectStream(bis)) {
			NSDictionary<?,?> dd = (NSDictionary<?,?>) ois.readObject();
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

	@SuppressWarnings("unchecked")
	public static NSDictionary fromPropertyList(String plist) {
		NSDictionary<Object,Object> dict = (NSDictionary) NSPropertyListSerialization.propertyListFromString(plist);
		return new ERXMutableDictionary<Object,Object>(dict);
	}

	public static String toPropertyList(NSDictionary<?,?> dict) {
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

	public ERXMutableDictionary(NSDictionary<? extends K, ? extends V> d) {
		super(d);
	}

	public ERXMutableDictionary() {
		super();
	}

	@Override
	public Object clone() {
		return new ERXMutableDictionary<K,V>(this);
	}

	/**
	 * return the string value of an object for key
	 * 
	 * @param key
	 *            the key which is linked to the object
	 * @return if objectForKey return a non null value this method returns the
	 *         toString value from the object
	 */
	public String stringObjectForKey(K key) {
		Object o = objectForKey(key);
		return o == null ? null : o.toString();
	}

	/**
	 * @param key
	 */
	public Boolean booleanObjectForKey(K key) {
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

	public static class ThreadSafeDictionary<K,V> extends ERXMutableDictionary<K,V> {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public ThreadSafeDictionary(NSMutableDictionary<? extends K, ? extends V> dictionary) {
			super(dictionary);
		}

		@Override
		public synchronized void addEntriesFromDictionary(NSDictionary<? extends K, ? extends V> otherDictionary) {
			super.addEntriesFromDictionary(otherDictionary);
		}

		@Override
		public synchronized NSDictionary<K,V> immutableClone() {
			return super.immutableClone();
		}

		@Override
		public synchronized NSMutableDictionary<K,V> mutableClone() {
			return super.mutableClone();
		}

		@Override
		public synchronized void removeAllObjects() {
			super.removeAllObjects();
		}

		@Override
		public synchronized V removeObjectForKey(Object key) {
			return super.removeObjectForKey(key);
		}

		@Override
		public synchronized void removeObjectsForKeys(NSArray<? extends K> keys) {
			super.removeObjectsForKeys(keys);
		}

		@Override
		public synchronized void setDictionary(NSDictionary<? extends K, ? extends V> otherDictionary) {
			super.setDictionary(otherDictionary);
		}

		@Override
		public synchronized void setObjectForKey(V object, K key) {
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
		public synchronized NSArray<K> allKeys() {
			return super.allKeys();
		}

		@Override
		public synchronized NSArray<K> allKeysForObject(Object object) {
			return super.allKeysForObject(object);
		}

		@Override
		@SuppressWarnings("unchecked")
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
		public synchronized HashMap<K,V> hashMap() {
			Object keys[] = keysNoCopy();
			int c = keys.length;
			HashMap<K,V> map = new HashMap<K,V>(c <= 0 ? 1 : c);
			for (int i = 0; i < c; i++) {
				map.put((K)keys[i], objectForKey(keys[i]));
			}

			return map;
		}

		@Override
		public synchronized Hashtable<K,V> hashtable() {
			return super.hashtable();
		}

		@Override
		public synchronized boolean isEqualToDictionary(NSDictionary<?, ?> otherDictionary) {
			return super.isEqualToDictionary(otherDictionary);
		}

		@Override
		public synchronized Enumeration<K> keyEnumerator() {
			return super.keyEnumerator();
		}

		@Override
		public synchronized Object[] keysNoCopy() {
			return super.keysNoCopy();
		}

		@Override
		public synchronized Enumeration<V> objectEnumerator() {
			return super.objectEnumerator();
		}

		@Override
		public synchronized V objectForKey(Object key) {
			return super.objectForKey(key);
		}
		
		@Override
		public synchronized NSArray<V> objectsForKeys(NSArray<? extends K> keys, V notFoundMarker) {
			return super.objectsForKeys(keys, notFoundMarker);
		}

		@Override
		public synchronized Object[] objectsNoCopy() {
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
	public static <T,U> NSMutableDictionary<T,U> synchronizedDictionary(NSMutableDictionary<? extends T, ? extends U> dict) {
		return new ThreadSafeDictionary<T,U>(dict);
	}

	/**
	 * Returns a new thread-safe mutable dictionary.
	 * @param <U>
	 * 
	 * @return a new thread-safe mutable dictionary
	 */
	public static <T,U> NSMutableDictionary<T,U> synchronizedDictionary() {
		return synchronizedDictionary(new ERXMutableDictionary<T,U>());
	}
 
	/**
	 * Returns either a new thread-safe dictionary, or just
	 * returns dict if the dictionary is not mutable.
	 * 
	 * @param dict
	 *            the dictionary to make thread-safe
	 * @return a thread-safe dictionary
	 */
	public static <T,U> NSDictionary<T,U> synchronizedDictionary(NSDictionary<? extends T, ? extends U> dict) {
		if (!(dict instanceof NSMutableDictionary)) {
			return (NSDictionary<T, U>) dict;
		}
		return synchronizedDictionary((NSMutableDictionary<T, U>) dict);
	}
}
