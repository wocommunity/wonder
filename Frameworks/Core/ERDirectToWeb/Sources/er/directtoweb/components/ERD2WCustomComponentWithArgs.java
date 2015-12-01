/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WCustomComponent;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.directtoweb.ERDirectToWeb;
import er.extensions.validation.ERXExceptionHolder;

// Useful component and important bug fix
// Fixes validation failures being propogated
// Adds valueForBinding that resolves in the d2wContext.
/**
 * Allows custom components to resolve valueForBinding requests in the rules.
 * @d2wKey extraBindings
 */
public class ERD2WCustomComponentWithArgs extends D2WCustomComponent implements ERXExceptionHolder {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

   public ERD2WCustomComponentWithArgs(WOContext context) {
        super(context);
    }
    
    /** logging support */
    public final static Logger log = Logger.getLogger(ERD2WCustomComponentWithArgs.class);

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
    
    @Override
    public Object valueForBinding(String binding) {
        return hasBinding(binding) ? originalValueForBinding(binding) : nonCachingContext().valueForKey(binding);
    }

    @Override
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }

    public void clearValidationFailed(){
        if (parent() instanceof ERXExceptionHolder)
            ((ERXExceptionHolder)parent()).clearValidationFailed();
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
    
    // Needed to fix a caching bug.
    public D2WContext nonCachingContext() { return (D2WContext)super.valueForBinding("localContext"); }
}
