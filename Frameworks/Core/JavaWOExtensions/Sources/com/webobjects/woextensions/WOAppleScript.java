/*
 * WOAppleScript.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/** @deprecated
    The WOAppleScript component is deprecated.
*/
@Deprecated
public class WOAppleScript extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected String _controller;

    protected String Undefined = "Undefined";
    
    public WOAppleScript(WOContext aContext)  {
        super(aContext);
        _controller = Undefined; // this marks an undefined id
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public String controller()  {
        if (_controller == Undefined) {
            _controller = (String) _WOJExtensionsUtil.valueForBindingOrNull("controller",this);
        }
        return _controller;
    }

    public String height()  {
        String aHeight;
        String aController = controller();
        if ((aController!=null) && aController.toLowerCase().equals("true")) {
            aHeight = "25";
        } else {
            aHeight = valueForBinding("height").toString();
        }

        return aHeight;
    }

    public String width()  {
        String aWidth;
        String aController = controller();
        if ((aController!=null) && aController.toLowerCase().equals("true")) {
            aWidth = "108";
        } else {
            aWidth = valueForBinding("width").toString();
        }
        return aWidth;
    }
}