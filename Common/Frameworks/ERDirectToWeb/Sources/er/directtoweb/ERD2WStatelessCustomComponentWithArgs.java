/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.ERXExceptionHolder;

// Stateless version of D2WCustomComponentWithArgs
/**
 * Stateless version of D2WCustomComponentWithArgs.<br />
 * 
 */

public class ERD2WStatelessCustomComponentWithArgs extends ERD2WStatelessComponent implements ERXExceptionHolder {

    public ERD2WStatelessCustomComponentWithArgs(WOContext context) { super(context); }

    private Object _extraBindings;
    public Object extraBindings() {
        _extraBindings=d2wContext().valueForKey("extraBindings");
        if (_extraBindings!=null) {
            if (_extraBindings instanceof String && ((String)_extraBindings).length()>0) {
                // assume it's a dict and parse it
                _extraBindings=NSPropertyListSerialization.propertyListFromString((String)_extraBindings);
            }
        }
        return _extraBindings;
    }

    // Done this way so that subClasses can always get the original valueForBinding.
    public Object originalValueForBinding(String binding) { return super.valueForBinding(binding); }

    public Object valueForBinding(String binding) {
        return hasBinding(binding) ? originalValueForBinding(binding) : d2wContext().valueForKey(binding);
    }

    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }

    public void clearValidationFailed(){
        if (parent() instanceof ERXExceptionHolder)
            ((ERXExceptionHolder)parent()).clearValidationFailed();
    }

    public void reset() {
        _extraBindings=null;
        super.reset();
    }

    public boolean d2wDebuggingEnabled() {
        return ERDirectToWeb.d2wDebuggingEnabled(session());
    }
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }
    public boolean d2wPropertyKeyDebuggingEnabled() {
        return ERDirectToWeb.d2wPropertyKeyDebuggingEnabled(session());
    }
}
