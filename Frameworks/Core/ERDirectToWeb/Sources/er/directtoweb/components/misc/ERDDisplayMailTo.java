/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.foundation.ERXArrayUtilities;

/////////////////////////////////////////////////////////////////////////////////
// Important D2W Keys:
//	displayString - String that will be used to display the hyperlink
//	displayKey - key that specifies the key path off of the object that will be used
//		for displaying in the hyperlink.  Not required.
//	showBrackets - Boolean, specifies if the <> should be displayed around the mailto link.
/////////////////////////////////////////////////////////////////////////////////
/**
 * A display mailto component with a number of bindings.
 * 
 * @binding email
 * @binding object
 * @binding key
 * @binding showBrackets
 */
public class ERDDisplayMailTo extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

     public ERDDisplayMailTo(WOContext context) { super(context); }
    
    protected String _displayString, _email;
    protected Boolean _showBrackets;

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    @Override
    public boolean isStateless() { return true; }

    @Override
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
            Object emailObj = (hasBinding("email") ? valueForBinding("email") : objectKeyPathValue());
            if(emailObj != null && emailObj instanceof NSArray) {
                _email = ERXArrayUtilities.removeNullValues(((NSArray)emailObj)).componentsJoinedByString(",");
            }
            else if(emailObj != null && emailObj instanceof String) {
                _email = (String) emailObj;
            }
        }
        return _email;
    }

    public String displayString() {
        if (_displayString == null) {
            //first look for displayString binding
            _displayString = (String)(hasBinding("displayString")? valueForBinding("displayString") : null);

            //if displayString binding is not available, then settle for just email
            if (_displayString == null) {
                _displayString = email();
            }
        }
        return _displayString;
    }
}
