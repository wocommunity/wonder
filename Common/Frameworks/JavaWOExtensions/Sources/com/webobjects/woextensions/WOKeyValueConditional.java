/*
 * WOKeyValueConditional.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WOKeyValueConditional extends WOComponent {
    protected String _key;
    protected int _negate = -1;  // -1 is invalid, 0 is false, and 1 is true

    public WOKeyValueConditional(WOContext aContext) {
        super(aContext);
    }

    public boolean condition() {
        Object parentValue = parent().valueForKeyPath(key());
        Object thisValue = valueForBinding("value");
        return (parentValue == null) ? (thisValue == null) : (parentValue.equals(thisValue));
    }

    public String key() {
        if (_key == null) {
            _key = (String)_WOJExtensionsUtil.valueForBindingOrNull("key",this);
        }
        return _key;
    }

    public boolean negate() {
        if (_negate == -1) {
            Object thisNegate = valueForBinding("negate");
            if (thisNegate == null) {
                _negate = 0;
            } else if (thisNegate instanceof Boolean) {
                _negate = (((Boolean) thisNegate).booleanValue()) ? 1 : 0;
            } else if (thisNegate instanceof Integer) {
                _negate = (((Integer) thisNegate).intValue() == 0) ? 0 : 1;
            } else if (thisNegate instanceof String) {
                _negate = (new Boolean((String) thisNegate)).booleanValue() ? 1 : 0;
            } else {
                _negate = 0;
            }
        }
        return _negate != 0;
    }

    public boolean isStateless() {
        return true;
    }

    protected void _invalidateCaches() {
        // In order for this to behave like an element, all instance
        // variables need to be flushed when this component sleeps
        // so that it will pull via association.
        _key = null;
        _negate = -1;
    }

    public void reset()  {
        _invalidateCaches();
    }
}
