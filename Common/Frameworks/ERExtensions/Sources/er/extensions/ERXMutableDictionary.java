package er.extensions;


import java.io.*;
import java.util.*;

import com.webobjects.foundation.*;

/**
usefull class in to automatically en- and decode an NSMutableDictionary
 as blob into a database. ERPrototype name = mutableDictionary
*/
public class ERXMutableDictionary extends NSMutableDictionary implements Map {
    public static final long serialVersionUID = 8091318522043166356L;
    
    public static NSData toBlob(ERXMutableDictionary d) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bout);
        oos.writeObject(d);
        oos.close();
        NSData sp = new NSData(bout.toByteArray());
        return sp;
    }
    public static ERXMutableDictionary fromBlob(NSData d) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ERXMutableDictionary dd = (ERXMutableDictionary) ois.readObject();
        ois.close();
        return dd;
    }

    public static ERXMutableDictionary fromPropertyList(String plist) {
    		NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(plist);
    		return new ERXMutableDictionary(dict);
    }

    public static String toPropertyList(ERXMutableDictionary dict) {
		String plist = NSPropertyListSerialization.stringFromPropertyList(dict);
		return plist;
    }

    public String toPropertyList() {
		String plist = NSPropertyListSerialization.stringFromPropertyList(this);
		return plist;
    }

    public NSData toBlob() throws Exception {
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
}
