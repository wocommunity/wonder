/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.Vector;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;

/**
 * Simple class to use multiple objects as
 * a single key for a dictionary or HashMap.
 * The goal of this class is to be very fast.
 */
public class ERXMultiKey {

    /** holds the object array of keys */
    private Object[] _keys;
    /** caches the number of keys */
    private short _keyCount;

    /**
     * Constructs a multi-key for a given
     * number.
     * @param keyCount number of keys
     */
    public ERXMultiKey(short keyCount) {
        _keyCount=keyCount;
        _keys=new Object[keyCount];
    }

    /**
     * Constructs a multi-key.
     */
    public ERXMultiKey() {
        _keyCount=0;
        _keys=new Object[0];
    }

    /**
     * Constructs a multi-key for a given
     * object array.
     * @param keys object array
     */
    public ERXMultiKey(Object[] keys) {
        this((short)keys.length);
        System.arraycopy(keys,0,_keys,0,(int)_keyCount);
    }

    /**
     * Constructs a multi-key for a given
     * array.
     * @param keys array of keys
     */    
    public ERXMultiKey(NSArray keys) {
        this ((short)keys.count());
        for (int i=0; i<keys.count(); i++) _keys[i]=keys.objectAtIndex(i);
   }

    /**
     * Constructs a multi-key for a given
     * vector.
     * @param keys vector of keys
     */
    public ERXMultiKey(Vector keys) {
        this ((short)keys.size());
        for (int i=0; i<keys.size(); i++) _keys[i]=keys.elementAt(i);
    }

    /**
     * Method used to return the object array
     * of keys for the current multi-key.
     * @return object array of keys
     */    
    public final Object[] keys() { return _keys; }

    /**
     * Calculates a unique hash code for
     * the given array of keys.
     * @return unique hash code for the array
     *		of keys.
     */
    public final int hashCode() {
        int result = 0;

        for (int i=0; i<_keyCount; i++) {
		    final Object theKey = _keys[i];

            if ( theKey != null ) {
                result ^= theKey.hashCode();
                result = ( result << 1 ) | ( result >> 31 );
            }
        }

        return result;
    }

    /**
     * Method used to compare two ERXMultiKeys.
     * A multi key is equal to another multi key
     * if the number of keys are equal and all
     * of the keys are either both null or <code>
     * equals</code>.
     * @param o object to be compared
     * @return result of comparison
     */
    public final boolean equals(Object o) {
    	if (o instanceof ERXMultiKey) {
    		ERXMultiKey o2 = (ERXMultiKey) o;
    		if (_keyCount!=o2._keyCount)
    			return false;
    		for (int i=0; i<_keyCount; i++) {
    			Object k=o2._keys[i];
    			Object m=_keys[i];
    			if (m!=k && (m==null || k==null || !m.equals(k)))
    				return false;
    		}
    		return true;
    	}
    	return false;
    }

    /**
     * String representation of the multi-key.
     * @return string representation of key.
     */
    public String toString() {
        StringBuffer result=new StringBuffer("(");
        for (short i=0; i<_keys.length; i++) {
            Object o=_keys[i];
            result.append(o instanceof EOEntity ? ((EOEntity)o).name() : o != null ? o.toString() : "<NULL>");
            if(i != _keys.length-1)
                result.append(", ");
        }
        result.append(")");
        return result.toString();
    }
}
