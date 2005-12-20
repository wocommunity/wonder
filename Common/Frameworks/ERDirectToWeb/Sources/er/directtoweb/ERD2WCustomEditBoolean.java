/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Allows editing boolean values based on radio buttons and localizable strings.<br />
 * Set the values via the <code>choicesNames</code> d2wcontext value, eg: ("Yes", "No") or ("Set", "Unset", "Don't care")
 */
// FIXME AK: together with ERD2WQueryBoolean, should use a common ERXEditBoolean that takes a choicesNames binding
public class ERD2WCustomEditBoolean extends D2WEditBoolean {

    public ERD2WCustomEditBoolean(WOContext context) {
        super(context);
    }

    public interface BooleanProxy {
    	public String name();
    	public Boolean value();
    }
    
    public BooleanProxy trueValue = new BooleanProxy() {
    	public boolean equals(Object other) {
    		return other == trueValue || (other != null && ERXValueUtilities.booleanValue(other));
    	}
    	
    	public String name() {
    		return (String) choicesNames().objectAtIndex(0);
    	}
    	
    	public Boolean value() {
    		return Boolean.TRUE;
    	}
    };
    
    public BooleanProxy falseValue = new BooleanProxy() {
    	public boolean equals(Object other) {
    		return other == falseValue || (other != null && !ERXValueUtilities.booleanValue(other));
    	}
    	
    	public String name() {
    		return (String) choicesNames().objectAtIndex(1);
    	}
    	
    	public Boolean value() {
    		return Boolean.FALSE;
    	}
    };
    
    public BooleanProxy nullValue = new BooleanProxy() {
    	public boolean equals(Object other) {
    		return other == nullValue || other == null;
    	}
    	
    	public String name() {
    		return (String) choicesNames().objectAtIndex(2);
    	}
    	
    	public Boolean value() {
    		return null;
    	}
   };
    
    protected NSArray _choicesNames;

    public void reset(){
        super.reset();
        _choicesNames = null;
    }

    public Object yesNoBoolean() {
        Object value = object().valueForKeyPath(propertyKey());
        if(trueValue.equals(value)) return trueValue;
        if(falseValue.equals(value)) return falseValue;
        return nullValue;
    }
    
    public void setYesNoBoolean(Object newYesNoBoolean) {
    	BooleanProxy proxy = (BooleanProxy)newYesNoBoolean;
    	object().validateTakeValueForKeyPath(proxy.value(), propertyKey());
    }
    public String radioBoxGroupName(){
        return ("YesNoGroup_"+d2wContext().propertyKey());
    }

    public NSArray choicesNames(){
        if(_choicesNames == null)
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    public void validationFailedWithException(Throwable theException,Object object, String theKeyPath) {
    	if(object instanceof BooleanProxy) {
    		BooleanProxy proxy = (BooleanProxy)object;
    		object = proxy.value();
    	}
    	parent().validationFailedWithException(theException, object, theKeyPath);
    }

    public Object validateTakeValueForKeyPath(Object object, String string) {
    	if(object instanceof BooleanProxy) {
    		BooleanProxy proxy = (BooleanProxy)object;
    		object = proxy.value();
    	}
    	return super.validateTakeValueForKeyPath(object, string);
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r,c);
        try {
            object().validateTakeValueForKeyPath(objectPropertyValue(), propertyKey());
        } catch (NSValidation.ValidationException e) {
            validationFailedWithException(e, objectPropertyValue(), propertyKey());
        }
    }

    public boolean allowsNull() {
        return choicesNames().count() > 2;
    }
}
