/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

public class ERXKeyValuePair {

    public ERXKeyValuePair(Object newKey, Object newValue){
        _key = newKey;
        _value = newValue;
    }

    protected Object _key;
    public void setKey(Object newKey){ _key = newKey; }
    public Object key(){ return _key; }

    protected Object _value;
    public void setValue(Object value){ _value = value; }
    public Object value(){ return _value; }
}
