/*
 * Copyright (c) 2000 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 *
 * Portions Copyright (c) 2000 Apple Computer, Inc.  All Rights
 * Reserved.  This file contains Original Code and/or Modifications of
 * Original Code as defined in and that are subject to the Apple Public
 * Source License Version 1.1 (the "License").  You may not use this file
 * except in compliance with the License.  Please obtain a copy of the
 * License at http://www.apple.com/publicsource and read it before using
 * this file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON- INFRINGEMENT.  Please see the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * @APPLE_LICENSE_HEADER_END@
 */
package er.extensions;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

// Almost direct port of WOSortOrder from WO 5's WOExtensions.
/**
 * (Back port from WO 5 WOExtensions)<br />
 * 
 */

public class WOSortOrder extends WOComponent {

    public WOSortOrder(WOContext aContext) {
        super(aContext);
    }
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }
    public void reset() {
        _invalidateCaches();
    }

    public WODisplayGroup displayGroup() {
        if (null == _displayGroup)
            _displayGroup = (WODisplayGroup)valueForBinding("displayGroup");
        return _displayGroup;
    }

    public String key() {
        if (null == _key)
            _key = (String)valueForBinding("key");
        return _key;
    }

    public String displayKey() {
        if (null == _displayKey)
            _displayKey = (String)valueForBinding("displayKey");
        return _displayKey;
    }

    private EOSortOrdering _primarySortOrdering() {
        NSArray nsarray = displayGroup().sortOrderings();
        if (nsarray != null && nsarray.count() > 0) {
            EOSortOrdering eosortordering = (EOSortOrdering)nsarray.objectAtIndex(0);
            return eosortordering;
        } else {
            return null;
        }
    }

    private boolean _isCurrentKeyPrimary() {
        EOSortOrdering eosortordering = _primarySortOrdering();
        return eosortordering != null && eosortordering.key().equals(key());
    }

    private NSSelector _primaryKeySortOrderingSelector()
    {
        EOSortOrdering eosortordering = _primarySortOrdering();
        NSSelector nsselector = null;
        if(eosortordering != null)
            nsselector = eosortordering.selector();
        return nsselector;
    }

    public String imageName()
    {
        String s = "Unsorted.gif";
        if(_isCurrentKeyPrimary())
        {
            NSSelector nsselector = _primaryKeySortOrderingSelector();
            if(nsselector.equals(EOSortOrdering.CompareAscending))
                s = "Ascending.gif";
            else
            if(nsselector.equals(EOSortOrdering.CompareDescending))
                s = "Descending.gif";
        }
        return s;
    }

    private void _removeSortOrderingWithKey(String s)
    {
        int i = 0;
        Object obj = null;
        WODisplayGroup wodisplaygroup = displayGroup();
        NSArray nsarray = wodisplaygroup.sortOrderings();
        if(nsarray != null)
        {
            NSMutableArray nsmutablearray = new NSMutableArray(nsarray); //.mutableClone();
            for(Enumeration enumeration = nsmutablearray.objectEnumerator(); enumeration.hasMoreElements(); i++)
            {
                EOSortOrdering eosortordering = (EOSortOrdering)enumeration.nextElement();
                if(!s.equals(eosortordering.key()))
                    continue;
                nsmutablearray.removeObjectAtIndex(i);
                break;
            }

            wodisplaygroup.setSortOrderings(nsmutablearray);
        }
    }

    private void _makePrimarySortOrderingWithSelector(NSSelector nsselector)
    {
        String s = key();
        WODisplayGroup wodisplaygroup = displayGroup();
        NSArray nsarray = wodisplaygroup.sortOrderings();
        NSMutableArray nsmutablearray;
        if(nsarray != null)
            nsmutablearray = new NSMutableArray(nsarray);
        else
            nsmutablearray = new NSMutableArray();
        EOSortOrdering eosortordering = EOSortOrdering.sortOrderingWithKey(s, nsselector);
        nsmutablearray.insertObjectAtIndex(eosortordering, 0);
        if(nsmutablearray.count() > 3)
            nsmutablearray.removeLastObject();
        wodisplaygroup.setSortOrderings(nsmutablearray);
    }

    public String helpString()
    {
        return "Push to toggle sorting order according to " + displayKey();
    }

    public WOComponent toggleClicked()
    {
        String s = key();
        if(_isCurrentKeyPrimary())
        {
            NSSelector nsselector = _primaryKeySortOrderingSelector();
            if(nsselector.equals(EOSortOrdering.CompareAscending)) {
                _removeSortOrderingWithKey(s);
                _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareDescending);
            } else if (nsselector.equals(EOSortOrdering.CompareDescending)) {
                _removeSortOrderingWithKey(s);
                _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
            } else {
                _removeSortOrderingWithKey(s);
                _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
            }
        } else {
            _removeSortOrderingWithKey(s);
            _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
        }
        displayGroup().updateDisplayedObjects();
        return null;
    }

    private void _invalidateCaches()
    {
        _key = null;
        _displayKey = null;
        _displayGroup = null;
    }

    private String _key;
    private String _displayKey;
    WODisplayGroup _displayGroup;
}
