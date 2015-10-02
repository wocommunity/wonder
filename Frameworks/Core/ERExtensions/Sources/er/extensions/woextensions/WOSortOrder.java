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
package er.extensions.woextensions;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.components.ERXStatelessComponent;

/**
 * Almost direct port of WOSortOrder from WO 5's WOExtensions
 * 
 * @binding caseInsensitive is ordering case sensitive or not
 */

public class WOSortOrder extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public WOSortOrder(WOContext aContext) {
        super(aContext);
    }
    
    @Override
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
        }
        return null;
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
        	if(nsselector.equals(EOSortOrdering.CompareAscending) 
        			|| nsselector.equals(EOSortOrdering.CompareCaseInsensitiveAscending)) {
        		s = "Ascending.gif";
        	} else if(nsselector.equals(EOSortOrdering.CompareDescending) 
        			|| nsselector.equals(EOSortOrdering.CompareCaseInsensitiveDescending)) {
        		s = "Descending.gif";
        	}
        }
        return s;
    }

    private void _removeSortOrderingWithKey(String s)
    {
        int i = 0;
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
        boolean caseInsensitive = caseInsensitive();
        NSSelector asc = caseInsensitive ? EOSortOrdering.CompareCaseInsensitiveAscending : EOSortOrdering.CompareAscending;
        NSSelector desc = caseInsensitive ? EOSortOrdering.CompareCaseInsensitiveDescending : EOSortOrdering.CompareDescending;
        if(_isCurrentKeyPrimary())
        {
            NSSelector nsselector = _primaryKeySortOrderingSelector();
            if(nsselector.equals(EOSortOrdering.CompareAscending) 
            		|| nsselector.equals(EOSortOrdering.CompareCaseInsensitiveAscending)) {
                _removeSortOrderingWithKey(s);
                _makePrimarySortOrderingWithSelector(desc);
            } else if (nsselector.equals(EOSortOrdering.CompareDescending) 
            		|| nsselector.equals(EOSortOrdering.CompareCaseInsensitiveDescending)) {
                _removeSortOrderingWithKey(s);
                _makePrimarySortOrderingWithSelector(asc);
            } else {
                _removeSortOrderingWithKey(s);
                _makePrimarySortOrderingWithSelector(asc);
            }
        } else {
            _removeSortOrderingWithKey(s);
            _makePrimarySortOrderingWithSelector(asc);
        }
        displayGroup().updateDisplayedObjects();
        return null;
    }

    public boolean caseInsensitive() {
		return booleanValueForBinding("caseInsensitive");
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
