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
import com.webobjects.foundation.NSMutableArray;
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
 
    protected NSArray<String> _choicesNames;
    
    public void reset(){
        super.reset();
        _choicesNames = null;
    }

    public NSArray<String> choicesNames() {
        if(_choicesNames == null) {
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        }
        return _choicesNames;
    }

    public String stringForYes() {
        return choicesNames().objectAtIndex(0);
    }
    
    public String stringForNo() {
        return choicesNames().objectAtIndex(1);
    }
    
    public String stringForNull() {
        if(allowsNull()) {
            return choicesNames().objectAtIndex(2);
        }
        return null;
    }

    public String uiMode() {
        return useCheckbox() ? "checkbox" : "radio";
    }

    public boolean useCheckbox() {
    	return choicesNames().count() == 1;
    }

    public boolean allowsNull() {
        return choicesNames().count() > 2;
    }
}
