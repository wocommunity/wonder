/*
 * WOSortOrder.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (ÒAppleÓ) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under AppleÕs copyrights in this original Apple 
 * software (the ÒApple SoftwareÓ), to use, reproduce, modify and 
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

import java.util.Enumeration;

public class WOSortOrder extends WOComponent {
    protected String _key;
    protected String _displayKey;
    // ** Internal Caching
    protected WODisplayGroup _displayGroup;

    public WOSortOrder(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public void reset()  {
        _invalidateCaches();
    }

    /////////////
    // Bindings
    ////////////
    public WODisplayGroup displayGroup() {
        if (null==_displayGroup) {
            _displayGroup = (WODisplayGroup)_WOJExtensionsUtil.valueForBindingOrNull("displayGroup",this);
        }
        return _displayGroup;
    }

    public String key() {
        if (null==_key) {
            _key = (String)_WOJExtensionsUtil.valueForBindingOrNull("key",this);
        }
        return _key;
    }

    public String displayKey() {
        if (null==_displayKey) {
            _displayKey = (String)_WOJExtensionsUtil.valueForBindingOrNull("displayKey",this);
        }
        return _displayKey;
    }

    ///////////
    // Utility
    ///////////
    protected EOSortOrdering _primarySortOrdering() {
        NSArray anArray = displayGroup().sortOrderings();
        if ((anArray!=null) && (anArray.count() > 0)) {
            EOSortOrdering anOrdering = (EOSortOrdering)anArray.objectAtIndex(0);
            return anOrdering;
        }
        return null;
    }

    protected boolean _isCurrentKeyPrimary() {
        EOSortOrdering anOrdering = _primarySortOrdering();
        if ((anOrdering!=null) && anOrdering.key().equals(key())) {
            return true;
        }
        return false;
    }

    protected NSSelector _primaryKeySortOrderingSelector() {
        EOSortOrdering anOrdering = _primarySortOrdering();
        NSSelector anOrderingSelector = null;
        if (anOrdering!=null) anOrderingSelector = anOrdering.selector();
        return anOrderingSelector;
    }

    public String imageName() {
        String anImageName = "Unsorted.gif";
        if (_isCurrentKeyPrimary()) {
            NSSelector aCurrentState = _primaryKeySortOrderingSelector();
            if (aCurrentState == EOSortOrdering.CompareAscending) {
                anImageName = "Ascending.gif";
            } else if (aCurrentState == EOSortOrdering.CompareDescending) {
                anImageName = "Descending.gif";
            }
        }
        return anImageName;
    }

    protected void _removeSortOrderingWithKey(String aKey) {
        int anIndex = 0;
        EOSortOrdering aSortOrdering = null;
        WODisplayGroup aDisplayGroup = displayGroup();
        NSArray sortOrderings = aDisplayGroup.sortOrderings();
        if (sortOrderings!=null) {
            NSMutableArray aSortOrderingArray = sortOrderings.mutableClone();
            Enumeration anEnumerator = aSortOrderingArray.objectEnumerator();
            while (anEnumerator.hasMoreElements()) {
                aSortOrdering = (EOSortOrdering) anEnumerator.nextElement();
                if (aKey.equals(aSortOrdering.key())) {
                    aSortOrderingArray.removeObjectAtIndex(anIndex);
                    break;
                }
                anIndex++;
            }
            aDisplayGroup.setSortOrderings(aSortOrderingArray);
        }
    }

    protected void _makePrimarySortOrderingWithSelector(NSSelector aSelector) {
        String aKey = key();
        WODisplayGroup aDisplayGroup = displayGroup();
        NSArray sortOrderings = aDisplayGroup.sortOrderings();
        NSMutableArray aSortOrderingArray;
        if (sortOrderings!=null) {
            aSortOrderingArray = new NSMutableArray(sortOrderings);
        } else {
            aSortOrderingArray = new NSMutableArray();
        }
        EOSortOrdering aNewSortOrdering = EOSortOrdering.sortOrderingWithKey(aKey, aSelector);
        aSortOrderingArray.insertObjectAtIndex(aNewSortOrdering, 0);
        if (aSortOrderingArray.count() > 3) {
            // ** limits sorting to 3 levels
            aSortOrderingArray.removeLastObject();
        }
        aDisplayGroup.setSortOrderings(aSortOrderingArray);
    }

    public String helpString() {
        return "Push to toggle sorting order according to "+displayKey();
    }

    /////////////
    // Actions
    /////////////
    public WOComponent toggleClicked() {
        String aKey = key();
        if (_isCurrentKeyPrimary()) {
            NSSelector aCurrentState = _primaryKeySortOrderingSelector();
            if (aCurrentState == EOSortOrdering.CompareAscending) {
                // from ascending to descending
                _removeSortOrderingWithKey(aKey);
                _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareDescending);
            } else if (aCurrentState == EOSortOrdering.CompareDescending) {
                // from descending to none
                _removeSortOrderingWithKey(aKey);
            } else {
                // from none to ascending
                _removeSortOrderingWithKey(aKey);
                _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
            }
        } else {
            _removeSortOrderingWithKey(aKey);
            _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
        }
        displayGroup().updateDisplayedObjects();
        return null;
    }
    
    protected void _invalidateCaches() {
        // ** By setting these to nil, we allow for cycling of the page)
        _key = null;
        _displayKey = null;
        _displayGroup = null;
    }
}
