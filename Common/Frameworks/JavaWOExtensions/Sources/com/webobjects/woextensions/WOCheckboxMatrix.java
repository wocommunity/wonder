/*
 * WOCheckboxMatrix.java
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

// ** This uses a technique of having the WORepetition compute the form values for us.  This is a bit strange to have the Repetition having form values.  It may well be clearer to simply use takeValuesFromRequest... in here and not use this trick.  The ability to ask an element for its elementID seems logical and useful (as we use it for the umbrealla name here).  Of course, we could have this on the component just as easily, and this may be clearer.  However, if there is a repetition with a repetition in it, then the component's elementID isn't enough.

import com.webobjects.appserver.*;

import com.webobjects.foundation.*;
import java.util.*;

public class WOCheckboxMatrix extends WOComponent {

    public Object currentItem;
    public int index;
    public String wrapperElementID;
    protected NSArray _selections = null;

    public WOCheckboxMatrix(WOContext aContext) {
        super(aContext);
    }
    
    public boolean isStateless() {
        return true;
    }

    public void setCurrentItem(Object anItem) {
        currentItem = anItem;
        setValueForBinding(currentItem, "item");
    }

    public NSArray selections() {
        if (_selections == null) {
            _selections = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("selections",this);
        }
        return _selections;
    }

    public void setSelections(NSArray aFormValuesArray) {
        // ** This is where we accept the formValues.  Kind of weird.
        NSMutableArray aSelectionsArray = new NSMutableArray();
        if (aFormValuesArray != null) {
            Number anIndex = null;
            Enumeration anIndexEnumerator = aFormValuesArray.objectEnumerator();
            NSArray anItemList = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("list",this);
            int anItemCount = anItemList.count();
            while (anIndexEnumerator.hasMoreElements()) {
                anIndex = new Integer((String)anIndexEnumerator.nextElement());
                int i = anIndex.intValue();
                if (i < anItemCount) {
                    Object anObject = anItemList.objectAtIndex(i);
                    aSelectionsArray.addObject(anObject);
                } else {
                    // ** serious problem here. Raise an exception?
                }
            }
        }
        setValueForBinding(aSelectionsArray, "selections");
        _selections = null;
    }

    public String isCurrentItemChecked() {
        if ((selections() != null) && selections().containsObject(currentItem)) {
            return "checked";
        }
        return null;
    }


    protected void _invalidateCaches() {
        _selections=null;
        currentItem = null;
    }

    public void reset()  {
        _invalidateCaches();
    }
}