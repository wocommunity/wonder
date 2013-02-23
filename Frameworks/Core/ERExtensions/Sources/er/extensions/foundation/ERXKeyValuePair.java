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
 * @param <K> the key type
 * @param <V> the value type 
 */
// ENHANCEME: Should implement NSKeyValueCoding
public class ERXKeyValuePair<K, V> {
    /** holds the key */
    protected K _key;
    /** holds the value */
    protected V _value;

    /**
     * Public constructor
     * @param newKey key
     * @param newValue value
     */
    public ERXKeyValuePair(K newKey, V newValue){
        _key = newKey;
        _value = newValue;
    }

    /**
     * Sets the key.
     * @param newKey new key value
     */
    public void setKey(K newKey){ _key = newKey; }
    /**
     * returns the key
     * @return da key 
     */
    public K key(){ return _key; }
    /**
     * Sets the value
     * @param value new value
     */
    public void setValue(V value) { _value = value; }
    /**
     * returns the value
     * @return da value
     */
    public V value() { return _value; }
    
    @Override
    public boolean equals( Object object ) {
        if( object instanceof ERXKeyValuePair ) {
            ERXKeyValuePair kvp = (ERXKeyValuePair) object;
            return value() != null && key() != null && 
                value().equals( kvp.value() ) && key().equals( kvp.key() );        
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (key() != null ? key().hashCode() : 1) * (value() != null ? value().hashCode() : 1);
    }
    
    @Override
    public String toString() {
        return "[ "+ key() +": "+ value() +" ]";
    }
}
