/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.lang.*;
import java.util.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

// This is mainly used for caching sets of keys in hash tables.
public class ERXMultiKey {

    private Object[] _keys;
    private short _keyCount;

    public ERXMultiKey(short keyCount) {
        _keyCount=keyCount;
        _keys=new Object[keyCount];
    }
    public ERXMultiKey(Object[] keys) {
        this((short)keys.length);
        System.arraycopy(keys,0,_keys,0,(int)_keyCount);
        //for (int i=0; i<_keyCount; i++) _keys[i]=keys[i];
    }

    public ERXMultiKey(NSArray keys) {
        this ((short)keys.count());
        for (int i=0; i<keys.count(); i++) _keys[i]=keys.objectAtIndex(i);
   }

    public ERXMultiKey(Vector keys) {
        this ((short)keys.size());
        for (int i=0; i<keys.size(); i++) _keys[i]=keys.elementAt(i);
   }
    
    public final Object[] keys() { return _keys; }
    public final int hashCode() {
        int result=0;
        for (int i=0; i<_keyCount; i++)
            if (_keys[i]!=null)
                result+=_keys[i].hashCode()<<i;
        return result;
    }

    public final boolean equals(Object o) {
        ERXMultiKey o2=(ERXMultiKey)o;
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

    public String toString() {
        StringBuffer result=new StringBuffer("(");
        for (short i=0; i<_keys.length; i++) {
            Object o=_keys[i];
            result.append(o instanceof EOEntity ? ((EOEntity)o).name() : o.toString());
        }
        result.append(")");
        return result.toString();
    }    
}
