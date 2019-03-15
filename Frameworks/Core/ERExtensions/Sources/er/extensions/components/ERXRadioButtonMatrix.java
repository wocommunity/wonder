/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Radio button list with lots of more options.
 * 
 * @binding list
 * @binding uniqueID
 * @binding item
 * @binding selection
 * @binding maxColumns
 * @binding cellpadding
 * @binding cellspacing
 * @binding width
 * @binding cellAlign
 * @binding cellVAlign
 * @binding cellWidth
 * @binding tableOtherTagString
 */
public class ERXRadioButtonMatrix extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Integer DEFAULT_PADDING = ERXConstant.ZeroInteger;
    private static final Integer DEFAULT_SPACING = ERXConstant.ZeroInteger;
    
    private static final String CSS_CLASS_FOR_TABLE_ALONE = "ERXMatrixTable";
    private static final String CSS_CLASS_FOR_TABLE_CHECKED_DEFAULT = "Checked";


    public ERXRadioButtonMatrix(WOContext aContext) {
        super(aContext);
    }

    protected Object currentItem;
    protected Object _selection;
    protected Number index;
    protected Object uniqueID;

    @Override
    public void reset() {
        invalidateCaches();
    }

    
    public Object currentItem() {
        return currentItem;
    }

    public void setCurrentItem(Object aValue) {
        currentItem = aValue;
        setValueForBinding(aValue, "item");
    }

    public Number index() {
        return index;
    }
    
    public boolean disabled() {
    	return booleanValueForBinding("disabled", false);
    }

    public void setIndex(Number newIndex) {
        index = newIndex;
    }

    public Object selection() {
        if (_selection == null) {
            // ** only pull this one time
            _selection = valueForBinding("selection");
        }

        return _selection;
    }

    public void setSelection(String anIndex) {
        if (anIndex != null) {
            // ** push the selection to the parent
            NSArray anItemList = (NSArray)valueForBinding("list");
            Object aSelectedObject = anItemList.objectAtIndex(Integer.parseInt(anIndex));
            setValueForBinding(aSelectedObject, "selection");
            // ** and force it to be pulled if there's a next time.
        }

        _selection = null;
    }

    public String isCurrentItemSelected() {
        if (selection()!=null && selection().equals(currentItem)) {
            return "checked";
        }

        return "";
    }

	public String otherTagStringForRadioButton() {
    	boolean isDisabled = disabled();
    	boolean isChecked = !ERXStringUtilities.stringIsNullOrEmpty(isCurrentItemSelected());
        	return (isDisabled ? "disabled" : "") + (isDisabled && isChecked? " " : "") + (isChecked ? "checked" : "");
	}

    @Override
    public void awake() {
        super.awake();
        uniqueID = valueForBinding("uniqueID");
        if(uniqueID == null) {
            uniqueID = context().elementID();
        }
    }

    public void invalidateCaches() {
        _selection=null;
        currentItem=null;
        index=null;
        uniqueID=null;
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        super.appendToResponse(aResponse, aContext);
    }

    @Override
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        setSelection(aRequest.stringFormValueForKey(uniqueID()));
        super.takeValuesFromRequest(aRequest, aContext);
    }
    
    public String uniqueID() {
        return uniqueID.toString();
    }
    
    public Object cellpadding() {
        Object v = valueForBinding("cellpadding");
        
        if(v != null) {
            return v;
        }
        return DEFAULT_PADDING;
    }

    public Object cellspacing() {
        Object v = valueForBinding("cellspacing");

        if(v != null) {
            return v;
        }
        return DEFAULT_SPACING;
    }
    
    
    /**
     * If the iterated radio button is checked, set the css class of the table that wraps
     * the radio button to include the css class CSS_CLASS_FOR_TABLE_CHECKED_DEFAULT.
     * This allows css to target the checked radio button, and therefore to be able to render it
     * differently.
     * If the iterated radio button is not checked, the css class will be set to CSS_CLASS_FOR_TABLE_ALONE
     * 
     * @return cssClass - whose value is dependent on whether the radio button is checked
     */
    public String cssClassForTableForRadioButton() {
    	String cssClass = CSS_CLASS_FOR_TABLE_ALONE; 
    	boolean isChecked = !ERXStringUtilities.stringIsNullOrEmpty(isCurrentItemSelected());
    	if (isChecked) {
    		cssClass += " " + CSS_CLASS_FOR_TABLE_CHECKED_DEFAULT;
    	}
    	return cssClass;
    }
    
}
