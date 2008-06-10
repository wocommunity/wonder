/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WEditBoolean;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Allows editing boolean values based on radio buttons and localizable strings.<br />
 * Set the values via the <code>choicesNames</code> d2wcontext value, eg: ("Yes", "No") or ("Set", "Unset", "Don't care")
 */
// FIXME AK: together with ERD2WQueryBoolean, should use a common ERXEditBoolean that takes a choicesNames binding
public class ERD2WCustomEditBoolean extends D2WEditBoolean {

    public ERD2WCustomEditBoolean(WOContext context) {
        super(context);
    }

    public static class BooleanProxy {
        private String _name;
        private Boolean _value;
       
        BooleanProxy(String name, Boolean value) {
            _name = name;
            _value = value;
        }
        
    	public String name() {
            return _name;
    	}
    	
    	public Boolean value() {
            return _value;
    	}
    	
    	public boolean equals(Object other) {
            return other == _value || (other != null && _value != null && ERXValueUtilities.booleanValue(other) == _value);
    	}
    }
    
    public BooleanProxy trueValue;
    public BooleanProxy falseValue;
    public BooleanProxy nullValue;
    
    protected NSArray _choicesNames;
    protected String _radioBoxGroupName;
    
    public void reset(){
        super.reset();
        _choicesNames = null;
        _radioBoxGroupName = null;
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
        if (_radioBoxGroupName == null) { 
            _radioBoxGroupName = "YesNoGroup_"+ context().elementID().replace('.','_'); 
        } 
        return _radioBoxGroupName; 
    }

    public NSArray choicesNames(){
        if(_choicesNames == null) {
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
            trueValue = new BooleanProxy((String) choicesNames().objectAtIndex(0), Boolean.TRUE);
            falseValue = new BooleanProxy((String) choicesNames().objectAtIndex(1), Boolean.FALSE);
            if(choicesNames().count() > 2) {
                nullValue = new BooleanProxy((String) choicesNames().objectAtIndex(2), null);
            }
        }
        return _choicesNames;
    }

    public boolean useCheckbox() {
    	return choicesNames().count() == 1;
    }
    
    public void validationFailedWithException(Throwable theException,Object object, String theKeyPath) {
    	if(object instanceof BooleanProxy) {
    		BooleanProxy proxy = (BooleanProxy)object;
    		object = proxy.value();
    	}
    	parent().validationFailedWithException(theException, object, theKeyPath);
    }

    public Object validateTakeValueForKeyPath(Object object, String string) {
    	if(useCheckbox() && object == null) {
    		object = Boolean.FALSE;
    	}
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
