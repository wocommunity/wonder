/*
 * WXBar.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WXBar extends WOComponent {
    public WXBar(WOContext aContext)  {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() {
        // Do not sync with the bindings
        return false;
    }

    public String middleWidth() {
        int aFullWidth = 0;
        double aPercentage = 0.0;
        Object aFullWidthString = _WOJExtensionsUtil.valueForBindingOrNull("fullWidth",this);
        Object aPercentageString = _WOJExtensionsUtil.valueForBindingOrNull("percentage",this);

        if (aFullWidthString instanceof Number) {
            aFullWidth = ((Number)aFullWidthString).intValue();
        } else {
            try {
                 if (aFullWidthString != null) {
                     aFullWidth = Integer.parseInt(aFullWidthString.toString());
                 }
             } catch (NumberFormatException e) {
                 throw new IllegalStateException("WXBar - problem parsing int from fullWidth and percentage bindings "+e);
             }
        }
        
        if (aPercentageString instanceof Number) {
            aPercentage = ((Number)aPercentageString).doubleValue();
        } else {
            try {
                if (aPercentageString != null) {
                    aPercentage = (new Double(aPercentageString.toString())).doubleValue();
                }
            } catch (NumberFormatException e) {
                throw new IllegalStateException("WXBar - problem parsing int from fullWidth and percentage bindings "+e);
            }
        }

        
        return Double.toString(aPercentage * aFullWidth);
    }
}
