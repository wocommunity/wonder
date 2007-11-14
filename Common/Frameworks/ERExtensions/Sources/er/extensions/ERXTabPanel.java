/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.woextensions.WOTabPanel;

/**
 * Better tab panel. Allows denial of tab switching. Useful when validation failures occur.<br />
 * 
 * @binding tabs a list of objects representing the tabs
 * @binding tabNameKey a string containing a key to apply to tabs to get the title of the tab
 * @binding selectedTab contains the selected tab
 * @binding bgcolor color to use for the selected tab and the body of the panel
 * @binding nonSelectedBgColor color to use for the non-selected tabs
 * @binding tabClass CSS class to use for the selected tab
 * @binding nonSelectedTabClass CSS class to use for the unselected tabs
 * @binding submitActionName if this binding is non null, tabs will contain a submit button instead of a regular hyperlink and the action
 * @binding textColor
 * @binding borderColor
 * @binding useFormSubmit true, if the form shoud be submitted before switching, allows denial of switches
 * @binding tabImageFramework the name of the framework that contains the tab images
 * @binding leftTabImage the name of the image on the left side of the tab
 * @binding rightTabImage the name of the image on the right side of the tab
 * @binding tabClass the style used for a selected tab
 * @binding nonSelectedTabClass the style used for a unselected tab
 * @binding tabImageContainerClass the style used for the td that surrounds the left and right side images of a selected tab
 * @binding nonSelectedTabImageContainerClass the style used for the td that surrounds the left and right side images of an unselected tab
 */
public class ERXTabPanel extends WOTabPanel  {

    public ERXTabPanel(WOContext c) {
        super(c);
    }    
    
    String _tabClass;
    String _nonSelectedTabClass;
    String _tabImageContainerClass;
    String _nonSelectedTabImageContainerClass;
    String _tabImageFramework;
    String _leftTabImage;
    String _rightTabImage;
    Boolean _useLinkForTabSwitch;

    public Object selectedTabIdentifier() {
    	int id = selectedTabName().hashCode();
    	return new Integer(id < 0 ? -id : id);
    }
    
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

    public Object tabImageFramework() {
      if (_tabImageFramework == null) {
        if (hasBinding("tabImageFramework")) {
          _tabImageFramework = (String)valueForBinding("tabImageFramework");
        }
        else {
          _tabImageFramework = "JavaWOExtensions";
        }
      }
      return _tabImageFramework;
    }

    public Object leftTabImage() {
      if (_leftTabImage == null) {
        if (hasBinding("leftTabImage")) {
          _leftTabImage = (String)valueForBinding("leftTabImage");
        }
        else {
          _leftTabImage = "leftTab.gif";
        }
      }
      return _leftTabImage;
    }

    public Object rightTabImage() {
      if (_rightTabImage == null) {
        if (hasBinding("rightTabImage")) {
          _rightTabImage = (String)valueForBinding("rightTabImage");
        }
        else {
          _rightTabImage = "rightTab.gif";
        }
      }
      return _rightTabImage;
    }

    public Object tabImageContainerClass() {
      if (_tabImageContainerClass == null) {
        if (hasBinding("tabImageContainerClass")) {
          _tabImageContainerClass = (String)valueForBinding("tabImageContainerClass");
        }
        else {
          _tabImageContainerClass = "tabImageContainer";
        }
      }
      return _tabImageContainerClass;
    }

    public Object nonSelectedTabImageContainerClass() {
      if (_nonSelectedTabImageContainerClass == null) {
        if (hasBinding("nonSelectedTabImageContainerClass")) {
          _nonSelectedTabImageContainerClass = (String)valueForBinding("nonSelectedTabImageContainerClass");
        }
        else {
          _nonSelectedTabImageContainerClass = "nonSelectedTabImageContainer";
        }
      }
      return _nonSelectedTabImageContainerClass;
    }

    public Object tabClass() {
        if (_tabClass==null) {
            if (hasBinding("tabClass")) {
                _tabClass = (String)valueForBinding("tabClass");
            } else {
                _tabClass = "tab";
            }

        }

        return _tabClass;
    }
    
    public Object nonSelectedTabClass() {
        if (_nonSelectedTabClass==null) {
            if (hasBinding("nonSelectedTabClass")) {
                _nonSelectedTabClass = (String)valueForBinding("nonSelectedTabClass");
            } else {
                _nonSelectedTabClass = "nonSelectedTab";
            }

        }

        return _nonSelectedTabClass;
    }


    public Object cellTabImageContainerClass() {
      Object cellTabImageContainerClass;
      if (isCellShaded()) {
        cellTabImageContainerClass = nonSelectedTabImageContainerClass();
      }
      else {
        cellTabImageContainerClass = tabImageContainerClass();
      }
      return cellTabImageContainerClass;
    }
    
    public Object cellTabClass() {
      Object cellTabClass;
      if (isCellShaded()) {
        cellTabClass = nonSelectedTabClass();
      }
      else {
        cellTabClass = tabClass();
      }
      return cellTabClass;
    }


    public Object submitString() {
        String formName = ERXWOForm.formName(context(), "EditForm");
        return "document."+formName+"."+currentTabNameWithoutSpaces()+".click(); return false;";
    }

    public Object currentTabNameWithoutSpaces() {
    	String currentTabName = (String)valueForKey("currentTabName");
    	if (currentTabName != null) {
    		currentTabName = "tab_" + currentTabName;
    		currentTabName = currentTabName.replaceAll("[\\s&+%.]", "_");
    	}
        return currentTabName;
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