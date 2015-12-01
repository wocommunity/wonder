/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import org.apache.commons.lang3.ObjectUtils;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * Sets a key value when the hyperlink is clicked.
 * 
 * @binding value The value to set when the hyperlink is set
 * @binding binding The method to call to set the value
 * @binding string optional link text
 * @binding action optional action
 * @binding class optional css class name
 */
public class ERXHyperlinkKeyValueSetter extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXHyperlinkKeyValueSetter(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean isStateless() { return true; }

    public WOActionResults action() {
        setValueForBinding(valueForBinding("value"), "binding");
        return (WOActionResults) (canGetValueForBinding("action") ? valueForBinding("action") : null);
    }

    public boolean disabled() {
    	Object val = valueForBinding("binding");
    	return ObjectUtils.equals(val, valueForBinding("value"));
    }
}
