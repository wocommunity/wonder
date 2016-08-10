/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

import er.extensions.woextensions.WOCollapsibleComponentContent;

/**
 * Better collapsible component content.
 * 
 * @binding closedImageFileName" defaults="Resources
 * @binding closedLabel
 * @binding condition" defaults="Boolean
 * @binding openedImageFileName" defaults="Resources
 * @binding openedLabel
 * @binding visibility
 * @binding framework" defaults="Frameworks
 * @binding submitActionName
 * @binding wrapLabelWithHTMLTag" defaults="Boolean
 */

public class ERXCollapsibleComponentContent extends WOCollapsibleComponentContent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXCollapsibleComponentContent(WOContext context) {
        super(context);
    }
    
    protected String _closedImageFileName, _openedImageFileName, _framework;
    protected boolean _isFrameworkSet = false;
    
    @Override
    public String openedImageFileName()  {
        if (_openedImageFileName==null) {
            if (hasBinding("openedImageFileName")) {
                _openedImageFileName = (String)valueForBinding("openedImageFileName");
            } else {
                _openedImageFileName = "ERDownTriangle.gif";
            }
        }
        return _openedImageFileName;
    }

    @Override
    public String closedImageFileName()  {
        if (_closedImageFileName==null) {
            if (hasBinding("closedImageFileName")) {
                _closedImageFileName = (String)valueForBinding("closedImageFileName");
            } else {
                _closedImageFileName = "ERRightTriangle.gif";
            }
        }
        return _closedImageFileName;
    }

    @Override
    public String framework() {
        if (!_isFrameworkSet) {
            _isFrameworkSet = true;
            _framework = hasBinding("framework") ? (String)valueForBinding("framework") : "ERExtensions";
            if (_framework != null && _framework.equalsIgnoreCase("app"))
                _framework=null;
        }
        return _framework;
    }
    
    public boolean omitWrappingTag() { return !hasBinding("wrapLabelWithHTMLTag"); }
}
