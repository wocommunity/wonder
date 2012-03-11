/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.ERXExtensions;

/**
 * Sets a key value when the hyperlink is clicked.<br />
 * 
 * @binding value The value to set when the hyperlink is set
 * @binding binding The method to call to set the value
 * @binding string optional link text
 * @binding action optional action
 * @binding class optional css class name
 */

public class ERXHyperlinkKeyValueSetter extends WOComponent {

    public ERXHyperlinkKeyValueSetter(WOContext aContext) {
        super(aContext);
    }

    public boolean isStateless() { return true; }
    
    public WOActionResults action() {
        setValueForBinding(valueForBinding("value"), "binding");
        return (WOActionResults) (canGetValueForBinding("action") ? valueForBinding("action") : null);
    }
    
    public boolean disabled() {
    	Object val = valueForBinding("binding");
    	return ERXExtensions.safeEquals(val, valueForBinding("value"));
    }
}
