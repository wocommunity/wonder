/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;

/////////////////////////////////////////////////////////////////////////////////
// Important D2W Keys:
//	displayKey - key that specifies the key path off of the object that will be used
//		for displaying in the hyperlink.  Not required.
//	showBrackets - Boolean, specifies if the <> should be displayed around the mailto link.
/////////////////////////////////////////////////////////////////////////////////
/**
 * A display mailto component with a number of bindings.<br />
 * 
 * @binding email
 * @binding object
 * @binding key
 * @binding showBrackets
 */

public class ERDDisplayMailTo extends ERDCustomEditComponent {

     public ERDDisplayMailTo(WOContext context) { super(context); }
    
    protected String _displayString, _email;
    protected Boolean _showBrackets;
    
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public void reset() {
         super.reset();
        _displayString = null;
        _email = null;
        _showBrackets = null;
     }

    public boolean showBrackets() {
        if (_showBrackets == null) {
            _showBrackets = booleanValueForBinding("showBrackets") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _showBrackets.booleanValue();
    }
    
    public String mailToHref() {
        String mailToHref = "mailto:" + email();
        return mailToHref;
    }
    
    public String email() {
        if (_email == null) {
            _email = (String)(hasBinding("email") ? valueForBinding("email") : objectKeyPathValue());
        }
        return _email;
    }

    public String displayString() {
        if (_displayString == null) {
            _displayString = (String)(hasBinding("displayKey") ? object().valueForKeyPath((String)valueForBinding("displayKey")) : email());
        }
        return _displayString;
    }
}
