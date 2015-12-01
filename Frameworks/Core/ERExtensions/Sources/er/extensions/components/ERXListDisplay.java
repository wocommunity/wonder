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
 * Useful for displaying a list of objects. Ex. a list of person eos could be displayed as "Fred, Mark and Max".
 * If you give the "item" binding, then the content is used to render. Otherwise the "attribute" binding will get used.
 * @binding list
 * @binding attribute
 * @binding emptyArrayDisplay the string to display when the array is null or empty
 * @binding item current item if in content mode
 * @binding separator separator to use for the first items (default ", ")
 * @binding finalSeparator separator for the last items (default localized " and ")
 * @binding escapeHTML
 * 
 * @author NetStruxr
 * @author kieran - I noticed nullArrayDisplay binding was not implemented. Implemented more useful emptyArrayDisplay with fallback to nullArrayDisplay binding for backwards compatibility.
 */
public class ERXListDisplay extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXListDisplay(WOContext aContext) {
        super(aContext);
    }
    
    public int index;
    protected NSArray list;
    protected String finalSeparator;
    protected String separator;

    @Override
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
    
    private Boolean isEmptyList;

	/** @return true if the array is null or empty */
	public boolean isEmptyList() {
		if (isEmptyList == null) {
			isEmptyList = Boolean.valueOf(list() == null || list().isEmpty());
		}
		return isEmptyList.booleanValue();
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

    @Override
    public void reset() {
        super.reset();
        list = null;
        separator = null;
        finalSeparator = null;
        isEmptyList = null;
        emptyArrayDisplay = null;
    }
    
    public String displayString() {
        String attribute = (String)valueForBinding("attribute");
        attribute = (attribute != null ? attribute : "toString");
        String empty = (String)valueForBinding("nullArrayDisplay");
        return ERXArrayUtilities.friendlyDisplayForKeyPath(list(), attribute, empty, separator(), finalSeparator());
    }
    
    private String emptyArrayDisplay;
	
	/**
	 * @return what to display when the list is null or empty. Supporting null or
	 *         empty makes sense since an empty relationship will return an
	 *         empty array, not a null array.
	 */
	public String emptyArrayDisplay() {
		if (emptyArrayDisplay == null) {
			emptyArrayDisplay = (String) valueForBinding("emptyArrayDisplay");
			// Backward compatibility for the previous version's
			// "nullArrayDisplay" binding which was never implemented anyway,
			// but let's implement it in case someone has bound to it in their
			// app expecting it to work in the future.
			if (emptyArrayDisplay == null) {
				emptyArrayDisplay = (String) valueForBinding("nullArrayDisplay");
			}

		}
		return emptyArrayDisplay;
	}
}
