/*
 * WOSimpleArrayDisplay.java
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

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

import java.util.*;

public class WOSimpleArrayDisplay extends WOComponent
{
    // required
    public NSArray list;
    // optional
    public int numberToDisplay;
    public String itemDisplayKey;
    public String listAction;
    public String listActionString;
    public Object currentItem; // used by subclasses
    // internal/private
    protected int _realSize;
    protected NSArray _subList;

    public WOSimpleArrayDisplay(WOContext aContext)  {
        super(aContext);
        numberToDisplay = -1;
    }

    public boolean isStateless() {
        return true;
    }

    public void reset()  {
        _invalidateCaches();
    }

    public NSArray list()  {
        list = (list != null) ? list : (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("list",this);
        if (list == null) {
            throw new IllegalStateException("<"+getClass().getName()+" list binding required. list value is null or missing>");
        }
        return list;
    }

    public String itemDisplayKey()  {
        if (null==itemDisplayKey) {
            if (hasBinding("itemDisplayKey")) {
                itemDisplayKey = (String)_WOJExtensionsUtil.valueForBindingOrNull("itemDisplayKey",this);
            }
        }
        return itemDisplayKey;
    }

    public String listAction()  {
        if (null==listAction)
            listAction=(String)_WOJExtensionsUtil.valueForBindingOrNull("listAction",this);
        return listAction;
    }

    public String listActionString() {
        if (null==listActionString)
            listActionString=(String)_WOJExtensionsUtil.valueForBindingOrNull("listActionString",this);
        return listActionString;
    }

    
    public int numberToDisplay()  {
        if (numberToDisplay == -1) {
            Object numStr = valueForBinding("numberToDisplay");
            if (numStr != null) {
                try {
                    numberToDisplay = Integer.parseInt(numStr.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("WOSimpleArrayDisplay - problem parsing int from numberToDisplay binding "+e);
                }
            } else {
                numberToDisplay = 5;
            }
            if (numberToDisplay <= 0) {
                throw new RuntimeException ("<"+getClass().getName()+" numberToDisplay can not be <=0 !");
            }
        }
        return numberToDisplay;
    }



    public int realSize()  {
        return _realSize;
    }

    public NSArray subList()  {
        if (null==_subList) {
            _realSize = list().count();
            if (_realSize > numberToDisplay()) {
                int anIndex;
                int count = numberToDisplay();
                NSMutableArray aSubList = new NSMutableArray(count);
                for (anIndex = 0; anIndex < count ; anIndex++) {
                    aSubList.addObject(list().objectAtIndex(anIndex));
                }
                _subList = aSubList;
            } else {
                _subList = list();
            }
        }
        return _subList;
    }

    public String displayStringForItem()  {
        String displayStringForItem = null;
        if (itemDisplayKey()!=null) {
            displayStringForItem = (String) NSKeyValueCodingAdditions.Utility.valueForKeyPath(currentItem, itemDisplayKey());
        } else {
            displayStringForItem = (String) NSKeyValueCoding.Utility.valueForKey(currentItem, "userPresentableDescription");
        }
        return displayStringForItem;
    }

    public boolean isDisplayingSubset()  {
        return (realSize() > numberToDisplay());
    }

    public WOActionResults listActionClicked()  {
        return performParentAction(listAction());
    }

    public boolean hasItems()  {
        return (list().count()!=0);
    }

    protected void _invalidateCaches() {
        // ** By setting these to null, we allow for cycling of the page)
        _subList = null;
        list = null;
        itemDisplayKey = null;
        listAction = null;
        listActionString = null;
        numberToDisplay=-1;
    }
}