/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.woextensions.*;

/*

 An HTML based Tab Panel

 Bindings:

 - tabs: a list of objects representing the tabs
 - tabNameKey: a string containing a key to apply to tabs to get the title of the tab
 - selectedTab: contains the selected tab
 - submitActionName: if this binding is non null, tabs will contain a submit button instead of a regular hyperlink and the action
   pointed to by the binding will be called
 - bgcolor: color to use for the selected tab and the body of the panel
 - nonSelectedBgColor: color to use for the non-selected tabs

 
 */
/**
 * Better tab panel. Allows denial of tab switching. Useful when validation failures occur.<br />
 * 
 * @binding tabs
 * @binding tabNameKey
 * @binding selectedTab
 * @binding nonSelectedBgColor
 * @binding bgcolor
 * @binding submitActionName
 * @binding textColor
 * @binding borderColor
 * @binding useFormSubmit" defaults="Boolean
 */

public class ERXTabPanel extends WOTabPanel  {

    public ERXTabPanel(WOContext c) {
        super(c);
    }    
    
    String _tabClass;
    String _nonSelectedTabClass;
    Boolean _useLinkForTabSwitch;

    public void switchSubmitTab() {
        Object result = null;

        if (submitActionName() != null && !submitActionName().equals("")) {
	    //FIXME: This should be more robust.
	    result = parent() != null ? parent().valueForKey(submitActionName()) : null;
        }

        if (result==null || ERXValueUtilities.booleanValue(result)) {
            switchTab();
        }

    }


    public Object tabClass() {
        if (_tabClass==null) {
            if (hasBinding("tabClass")) {
                _tabClass = (String)valueForBinding("tabClass");
            } else {
                _tabClass = "";
            }

        }

        return _tabClass;
    }


    public Object nonSelectedTabClass() {
        if (_nonSelectedTabClass==null) {
            if (hasBinding("nonSelectedTabClass")) {
                _nonSelectedTabClass = (String)valueForBinding("nonSelectedTabClass");
            } else {
                _nonSelectedTabClass = "";
            }

        }

        return _nonSelectedTabClass;
    }


    public Object cellTabClass() {
        if (isCellShaded()) {
            return nonSelectedTabClass();
        } else {
            return tabClass();
        }

    }


    public Object submitString() {
        String formName = ERXWOForm.formName(context(), "EditForm");
        return "document."+formName+".submit(); return false;";
    }

    public Object currentTabNameWithoutSpaces() {
        return NSArray.componentsSeparatedByString((String)valueForKey("currentTabName"), " ").componentsJoinedByString("");
    }


    public boolean useLinkForTabSwitch() {
        if (_useLinkForTabSwitch == null) {
            _useLinkForTabSwitch = ERXValueUtilities.booleanValue(session().valueForKeyPath("browser.isIE")) &&
            ERXValueUtilities.booleanValue(session().valueForKey("javaScriptEnabled")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _useLinkForTabSwitch.booleanValue();
    }
    

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        _useLinkForTabSwitch=null;
        super.appendToResponse(aResponse, aContext);
    }

}