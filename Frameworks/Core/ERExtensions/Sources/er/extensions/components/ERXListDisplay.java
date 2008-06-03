/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Useful for displaying a list of objects. Ex. a list of person eos could be displayed as "Fred, Mark and Max".<br />
 * If you give the "item" binding, then the content is used to render. Otherwise the "attribute" binding will get used.
 * @binding list
 * @binding attribute
 * @binding nullArrayDisplay
 * @binding item current item if in content mode
 * @binding separator separator to use for the first items (default ", ")
 * @binding finalSeparator separator for the last items (default localized " and ")
 * @binding escapeHTML
 */

public class ERXListDisplay extends WOComponent {

    public ERXListDisplay(WOContext aContext) {
        super(aContext);
    }
    
    public int index;
    protected NSArray list;
    protected String finalSeparator;
    protected String separator;
    
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public boolean escapeHTML() {
        return ERXValueUtilities.booleanValueWithDefault(valueForBinding("escapeHTML"), true);
    }
    
    public boolean useContent() {
        return hasBinding("item");
    }
    
    public NSArray list() {
        if(list == null) {
            list =(NSArray)valueForBinding("list");
        }
        return list;
    }
    
    public Object item() {
      return valueForBinding("item");
    }
    
    public void setItem(Object item) {
        setValueForBinding(item, "item");
    }
    
    public String currentSeparator() {
        int count = list().count();
        if(index < count - 2) {
            return separator();
        }
        if(index == count - 2) {
            return finalSeparator();
        }
        return "";
    }
   
    public String finalSeparator() {
        if(finalSeparator == null) {
            finalSeparator = (String)valueForBinding("finalSeparator");
            finalSeparator = (finalSeparator != null ? finalSeparator : ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(" and "));
        }
        return finalSeparator;
    }
    
    public String separator() {
        if(separator == null) {
            separator = (String)valueForBinding("separator");
            separator = (separator != null ? separator : ", ");
        }
        return separator;
    }
    
    public void reset() {
        super.reset();
        list = null;
        separator = null;
        finalSeparator = null;
    }
    
    public String displayString() {
        String attribute = (String)valueForBinding("attribute");
        attribute = (attribute != null ? attribute : "toString");
        String empty = (String)valueForBinding("nullArrayDisplay");
        return ERXArrayUtilities.friendlyDisplayForKeyPath(list(), attribute, empty, separator(), finalSeparator());
    }
}
