/*
 * WOThresholdColoredNumber.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WOThresholdColoredNumber extends WOComponent
{
    protected Number _threshold;
    protected String _highColor;
    protected String _lowColor;

    public WOThresholdColoredNumber(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public void reset()  {
        _invalidateCaches();
    }

    protected Number numberForBinding(String theBinding) {
        Object bindingValue = valueForBinding(theBinding);
        if (bindingValue != null) {
            if (bindingValue instanceof String) {
                return (Number) new Long((String) bindingValue);
            }
            if (bindingValue instanceof Number) {
                return (Number) bindingValue;
            }
        }

        return new Integer(0);
    }
    
    public Number threshold()  {
        if (_threshold==null) {
            _threshold = numberForBinding("threshold");
        }
        return _threshold;
    }

    public String highColor()  {
        if (null==_highColor) {
            _highColor = (String)_WOJExtensionsUtil.valueForBindingOrNull("highColor",this);
            if (null==_highColor) {
                _highColor = "#00FF00";
            }
        }
        return _highColor;
    }

    public String lowColor()  {
        if (null==_lowColor) {
            _lowColor = (String)_WOJExtensionsUtil.valueForBindingOrNull("lowColor",this);
            if (null==_lowColor) {
                _lowColor = "#FF0000";
            }
        }
        return _lowColor;
    }

    public String color()  {
        String aColor = null;
                
        if (numberForBinding("value").longValue() >= threshold().longValue()) {
            aColor = highColor();
        } else {
            aColor = lowColor();
        }
        return aColor;
    }

    protected void _invalidateCaches() {
        // ** By setting these to nil, we allow for cycling of the page)
        _threshold = null;
        _highColor = null;
        _lowColor = null;
    }

}
