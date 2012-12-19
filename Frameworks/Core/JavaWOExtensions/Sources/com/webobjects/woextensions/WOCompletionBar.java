/*
 * WOCompletionBar.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

// This component should be made stateless

public class WOCompletionBar extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected int _cachedPercentValue;
    public int value;
    public int showedValue;
    public int valueMin;
    public int valueMax;
    public String width;
    public String barColor;
    public String backgroundColor;
    public String border;
    public java.text.DecimalFormat numberformat = null;
    public String align;

    public WOCompletionBar(WOContext aContext)  {
        super(aContext);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    protected int _intValue(String bindingName, int defaultValue) {
        Object binding = valueForBinding(bindingName);
        int result = defaultValue;
        if (binding != null) {
            try {
                result = Integer.parseInt(binding.toString());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("WOCompletionBar - problem parsing int from "+bindingName+" binding "+e);
            }
        }
        return result;
    }
    
    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        Object v = valueForBinding("value");
        valueMin = _intValue("valueMin",0);
        valueMax = _intValue("valueMax",100);
        backgroundColor = (String)_WOJExtensionsUtil.valueForBindingOrNull("backgroundColor",this);
        barColor = (String)_WOJExtensionsUtil.valueForBindingOrNull("barColor",this);
        width = (String)_WOJExtensionsUtil.valueForBindingOrNull("width",this);
        align = (String)_WOJExtensionsUtil.valueForBindingOrNull("align",this);

        if (backgroundColor==null) backgroundColor="#2020af";
        if (barColor==null) barColor="#22aaff";
        if (width==null) width="100%";
        if (align==null) align = "CENTER";
        
        value = _intValue("value",valueMin);
        showedValue = value;
        if ((valueMax < valueMin) || (valueMax == valueMin)) {
            throw new RuntimeException("<"+getClass().getName()+"> valueMax is smaller than or equal to valueMin !");
        }
        if (value > valueMax) {
            value = valueMax;
        }
        if (value < valueMin) {
            value = valueMin;
        }
        super.appendToResponse(aResponse, aContext);
    }

    public int percentValue() {
        int number = (int)(100.*(value - valueMin)/(valueMax - valueMin));
        if (number < 1) {
            number = 1;
        }
        _cachedPercentValue = number;
        return _cachedPercentValue;
    }

    public int percentNotValue() {
        return 100 - _cachedPercentValue;
    }

    public boolean inProgress() {
        if ((value < valueMax) && (value > valueMin)) return true;
        return false;
    }

    public String notInProgressBackgroundColor() {
        if (value == valueMin) return backgroundColor;
        return barColor;
    }

    public String notInProgressColor() {
        if (value == valueMax) return backgroundColor;
        return barColor;
    }
}
