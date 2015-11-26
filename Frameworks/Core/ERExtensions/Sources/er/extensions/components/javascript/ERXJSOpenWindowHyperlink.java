/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXExtensions;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXDictionaryUtilities;

/**
 * Given an action opens the action in a new window.
 */
public class ERXJSOpenWindowHyperlink extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXJSOpenWindowHyperlink(WOContext aContext) {
        super(aContext);
    }

    ///** logging support *//
    public static final Logger log = Logger.getLogger(ERXJSOpenWindowHyperlink.class);

    @Override
    public boolean isStateless() { return true; }

    public boolean isDirectAction() {
        return valueForBinding("directActionName") != null;
    }

    // see ERXJSOpenWindowSubmitButton for the purpose of this method
    public String contextComponentActionURL() {
        return context().componentActionURL();
    }
    
    public String openWindow() {
        StringBuffer result = new StringBuffer("javascript:win=window.open('");
        if (valueForBinding("href")!=null) {
            result.append(valueForBinding("href"));
        } else if (valueForBinding("directActionName") == null) {
            result.append(contextComponentActionURL());
        } else {
            String anActionName;
            if (valueForBinding("actionClass") == null) {
                anActionName = (String)valueForBinding("directActionName");
            } else {
                anActionName = (String)valueForBinding("actionClass") + "/" + (String)valueForBinding("directActionName");
            }
            result.append(context().directActionURLForActionNamed(anActionName, (NSDictionary)valueForBinding("queryDictionary")));
            ERXExtensions.addRandomizeDirectActionURL(result);
        }
        
        NSDictionary urlParameters = (NSDictionary)valueForBinding("urlParameters");
        if (urlParameters != null) {
        	result.append(result.toString().indexOf('?') > - 1 ? '&' : '?');
        	result.append(ERXDictionaryUtilities.queryStringForDictionary(urlParameters, null));
        }
        
        String fragment=(String)valueForBinding("fragment");
        if (fragment!=null)
            result.append("#"+fragment);
        result.append("','"+valueForBinding("target"));
        result.append("','width="+valueForBinding("width"));
        result.append(",height="+valueForBinding("height"));
        result.append(",location=no");
        result.append(",scrollbars="+valueForBinding("scrollbars"));
        result.append(",menubar="+valueForBinding("menubar"));
        result.append(",toolbar="+valueForBinding("toolbar"));
        result.append(",titlebar="+valueForBinding("titlebar"));
        result.append(",resizable="+valueForBinding("resizable"));
        result.append(",dependant=yes");
        result.append("'); win.focus(); ");
        
        // Opens pop-up at place clicked, use moveTo instead of top, left params to open
        // command to avoid FireFox bugs
        if (ERXComponentUtilities.booleanValueForBinding(this, "positionAtCursor", false)) {
            result.append("win.moveTo(window.event.screenX, window.event.screenY); ");
        }
        
        result.append("return false;");
        
        return result.toString();
    }

    public WOActionResults action() {
        return (WOActionResults)valueForBinding("action");
    }
}
