/*
 * WOCompletionBar.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import java.util.*;
import com.webobjects.appserver.*;

// This component should be made stateless

public class WOCompletionBar extends WOComponent {
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
