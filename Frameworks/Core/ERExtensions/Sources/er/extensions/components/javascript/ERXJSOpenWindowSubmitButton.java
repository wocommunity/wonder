/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOContext;

// Will open a new window. 
/**
 * Submit button to open a new window with an action.
 */

public class ERXJSOpenWindowSubmitButton extends ERXJSOpenWindowHyperlink {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXJSOpenWindowSubmitButton(WOContext context) {
        super(context);
    }
    
    protected String _contextComponentActionURL;

    // Grosse Haque alert!
    // in order to get the proper URL to put in the javascript in the case of an action
    // binding, we use the invisible hyperlink, which calls this method
    // we store context().componentActionURL() and use that in the JS
    public String pushURL() {
        _contextComponentActionURL =context().componentActionURL();        
        return null;
    }

    // this 
    @Override
    public String contextComponentActionURL() {
        return _contextComponentActionURL;
    }
}
