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

/**
 * Given an action opens the action in a new window.<br />
 * 
 */

public class ERXJSOpenWindowHyperlink extends WOComponent {

    public ERXJSOpenWindowHyperlink(WOContext aContext) {
        super(aContext);
    }

    ///** logging support *//
    public static final Logger log = Logger.getLogger(ERXJSOpenWindowHyperlink.class);

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public boolean isDirectAction() {
        return valueForBinding("directActionName") != null;
    }

    // see EROpenJSWindowSubmitButton for the purpose of this method
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
        result.append("'); win.focus(); return false;");
        return result.toString();
    }

    public WOActionResults action() {
        return (WOActionResults)valueForBinding("action");
    }
}
