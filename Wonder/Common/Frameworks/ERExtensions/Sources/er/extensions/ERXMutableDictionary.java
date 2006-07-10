package er.extensions;


import java.io.*;
import java.util.*;

import com.webobjects.foundation.*;

/**
 * Adds {@link java.util.Map} functionality to NSMutableDictionary and
 * has helpers to en- and decode from database field. <code>ERPrototype name = mutableDictionary</code>
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
		} catch (IOException e) {
			// shouldn't ever happen, as we only write to memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
     }
    
    public static NSData toBlob(ERXMutableDictionary dict) {
    	return toBlob((NSDictionary)dict);
    }
    
    public static NSDictionary fromBlob(NSData d) {
		try {
	        ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
	        ObjectInputStream ois = new ObjectInputStream(bis);
	        NSDictionary dd = (NSDictionary) ois.readObject();
	        ois.close();
	        return dd;
		} catch (IOException e) {
			// shouldn't ever happen, as we only read from memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (ClassNotFoundException e) {
			// might happen, but it doesn't help us much to know it
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
    }

    public static NSDictionary fromPropertyList(String plist) {
    		NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(plist);
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

    public Object clone() {
        return new ERXMutableDictionary(this);
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        removeAllObjects();
    }
    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object arg0) {
        return objectForKey(arg0) != null;
    }
    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object arg0) {
        return allValues().containsObject(arg0);
    }
    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        throw new IllegalAccessError("not implemented");
    }
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object arg0) {
        return objectForKey(arg0);
    }
    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return count() == 0;
    }
    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        throw new IllegalAccessError("not implemented");
    }
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object arg0, Object arg1) {
        Object prev = objectForKey(arg0);
        setObjectForKey(arg1, arg0);
        return prev;
    }
    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map arg0) {
        throw new IllegalAccessError("not implemented");
    }
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object arg0) {
        return removeObjectForKey(arg0);
    }
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return count();
    }
    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values() {
        return (Collection)allValues();
    }
    
    public NSArray allValues() {
        NSArray av = super.allValues();
        return new ERXMutableArray(av);
    }
    
    /** return the string value of an  object for key
     * @param key, the key which is linked to the object
     * @return if objectForKey return a non null value 
     * this method returns the toString value from the object
     */
    public String stringObjectForKey(String key) {
        Object o = objectForKey(key);
        return o == null ? null : o.toString();
    }
    
    /**
     * @param key
     * @return
     */
    public Boolean booleanObjectForKey(String key) {
        Object o = objectForKey(key);
        return o == null ? null : ERXValueUtilities.booleanValue(o) ? Boolean.TRUE : Boolean.FALSE;
    }
   
	/**
	 * Simple thread safe wrapper. May or may not be correct, but it doesn't
	 * matter as you will never, *ever* call this directly, but call <code>
	 * ERXMutableDictionary.synchronizedDictionary();
	 * </code> instead and we will fix all the bugs in due time.
	 * @author ak
	 * 
	 */

    public static class ThreadSafeDictionary extends ERXMutableDictionary {

		public ThreadSafeDictionary(NSMutableDictionary dictionary) {
			super(dictionary);
		}

		public synchronized void addEntriesFromDictionary(NSDictionary otherDictionary) {
			super.addEntriesFromDictionary(otherDictionary);
		}

		public synchronized NSDictionary immutableClone() {
			return super.immutableClone();
		}

		public synchronized NSMutableDictionary mutableClone() {
			return super.mutableClone();
		}

		public synchronized void removeAllObjects() {
			super.removeAllObjects();
		}

		public synchronized Object removeObjectForKey(Object key) {
			return super.removeObjectForKey(key);
		}

		public synchronized void removeObjectsForKeys(NSArray keys) {
			super.removeObjectsForKeys(keys);
		}

		public synchronized void setDictionary(NSDictionary otherDictionary) {
			super.setDictionary(otherDictionary);
		}

		public synchronized void setObjectForKey(Object object, Object key) {
			super.setObjectForKey(object, key);
		}

		public synchronized void takeValueForKey(Object value, String key) {
			super.takeValueForKey(value, key);
		}

		protected synchronized  void _clearDeletionsAndCollisions() {
			super._clearDeletionsAndCollisions();
		}

		protected synchronized  void _ensureCapacity(int capacity) {
			super._ensureCapacity(capacity);
		}

		protected synchronized  void _initializeDictionary() {
			super._initializeDictionary();
		}

		public synchronized int _shallowHashCode() {
			return super._shallowHashCode();
		}

		public synchronized NSArray allKeys() {
			return super.allKeys();
		}

		public synchronized NSArray allKeysForObject(Object object) {
			return super.allKeysForObject(object);
		}

		public synchronized Class classForCoder() {
			return super.classForCoder();
		}

		public synchronized int count() {
			return super.count();
		}

		public synchronized void encodeWithCoder(NSCoder coder) {
			super.encodeWithCoder(coder);
		}

		public synchronized boolean equals(Object object) {
			return super.equals(object);
		}

		public synchronized int hashCode() {
			return super.hashCode();
		}

		public synchronized HashMap hashMap() {
			Object keys[] = keysNoCopy();
			int c = keys.length;
			HashMap map = new HashMap(c <= 0 ? 1 : c);
			for(int i = 0; i < c; i++) {
				map.put(keys[i], objectForKey(keys[i]));
			}

			return map;
		}

		public synchronized Hashtable hashtable() {
			return super.hashtable();
		}

		public synchronized boolean isEqualToDictionary(NSDictionary otherDictionary) {
			return super.isEqualToDictionary(otherDictionary);
		}

		public synchronized Enumeration keyEnumerator() {
			return super.keyEnumerator();
		}

		protected synchronized  Object[] keysNoCopy() {
			return super.keysNoCopy();
		}

		public synchronized Enumeration objectEnumerator() {
			return super.objectEnumerator();
		}

		public synchronized Object objectForKey(Object key) {
			return super.objectForKey(key);
		}

		public synchronized NSArray objectsForKeys(NSArray keys, Object notFoundMarker) {
			return super.objectsForKeys(keys, notFoundMarker);
		}

		protected synchronized  Object[] objectsNoCopy() {
			return super.objectsNoCopy();
		}

		public synchronized void takeValueForKeyPath(Object value, String keyPath) {
			super.takeValueForKeyPath(value, keyPath);
		}

		public synchronized String toString() {
			return super.toString();
		}

		public synchronized Object valueForKey(String key) {
			return super.valueForKey(key);
		}

		public synchronized Object valueForKeyPath(String keyPath) {
			return super.valueForKeyPath(keyPath);
		}
    }
    
    public static NSDictionary synchronizedDictionary() {
    	return synchronizedDictionary(new ERXMutableDictionary());
    }
    
    public static NSDictionary synchronizedDictionary(NSDictionary dict) {
    	if(!(dict instanceof NSMutableDictionary)) {
    		return dict;
    	}
    	return new ThreadSafeDictionary((NSMutableDictionary)dict);
    }       
}
