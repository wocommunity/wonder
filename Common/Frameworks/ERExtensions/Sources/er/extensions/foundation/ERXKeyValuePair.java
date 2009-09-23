/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

/**
 * Very simple class used for hold key-value
 * pairs.
 */
// ENHANCEME: Should implement NSKeyValueCoding
// FIXME: Should implement equals
public class ERXKeyValuePair {

    /**
     * Public constructor
     * @param newKey key
     * @param newValue value
     */
    public ERXKeyValuePair(Object newKey, Object newValue){
        _key = newKey;
        _value = newValue;
    }

    /** holds the key */
    protected Object _key;
    /** holds the value */
    protected Object _value;

    /**
     * Sets the key.
     * @param newKey new key value
     */
    public void setKey(Object newKey){ _key = newKey; }
    /**
     * returns the key
     * @return da key 
     */
    public Object key(){ return _key; }
    /**
     * Sets the value
     * @param value new value
     */
    public void setValue(Object value) { _value = value; }
    /**
     * returns the value
     * @return da value
     */
    public Object value() { return _value; }
}
