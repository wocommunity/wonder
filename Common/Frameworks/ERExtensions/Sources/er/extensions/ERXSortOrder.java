/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

//////////////////////////////////////////////////////////////////////////////////////////////////////
// ERSortOrder is an extension of WOSortOrder.  It adds two main abilities to WOSortOrder.
//	1) It providers the ability to use custom images for the different sorting states
//	2) It posts a notification when the sort ordering changes.  This second one is nice
//		if you want the app to 'remember' what the last sort was for a user.
//////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Better sort order changer. Useful for providing custom sort order images and remembering the sort orderings.<br />
 * 
 * @binding d2wContext
 * @binding displayGroup
 * @binding displayKey
 * @binding key
 * @binding unsortedImageSrc
 * @binding sortedAscendingImageSrc
 * @binding sortedDescendingImageSrc
 * @binding unsortedImageName
 * @binding sortedAscendingImageName
 * @binding sortedDescendingImageName
 * @binding imageFramework
 */

public class ERXSortOrder extends WOSortOrder {

    public ERXSortOrder(WOContext context) {
        super(context);
    }
    
    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXSortOrder.class);

    //////////////////////////////////////////////// Notification Hooks //////////////////////////////////////////
    public final static String SortOrderingChanged = "SortOrderingChanged";

    //////////////////////////////////////////////// States //////////////////////////////////////////////////////
    public final static int Reset = -1;
    public final static int Unsorted = 0;
    public final static int SortedAscending = 1;
    public final static int SortedDescending = 2;

    public boolean synchronizesVariablesWithBindings() { return false; }
    public void reset() {
        super.reset();
        _currentState = Reset;
    }

    protected int _currentState = Reset;
    public int currentState() {
        if (_currentState == Reset) {
            _currentState = Unsorted;
            if (_isCurrentKeyPrimary()) {
                NSSelector aCurrentState = _primaryKeySortOrderingSelector();
                if (aCurrentState.equals(EOSortOrdering.CompareAscending)) {
                    _currentState = SortedAscending;
                } else if (aCurrentState.equals(EOSortOrdering.CompareDescending)) {
                    _currentState = SortedDescending;
                }
            }
        }
        return _currentState;
    }
    
    public boolean hasCustomImageSrcForCurrentState() {
        boolean hasCustomImage = false;
        switch(currentState()) {
            case Unsorted:
                hasCustomImage = hasBinding("unsortedImageSrc"); break;
            case SortedAscending:
                hasCustomImage = hasBinding("sortedAscendingImageSrc"); break;
            case SortedDescending:
                hasCustomImage = hasBinding("sortedDescendingImageSrc"); break;
        }
        return hasCustomImage;
    }
    
    public boolean hasCustomImageNameForCurrentState() {
        boolean hasCustomImage = false;
        switch(currentState()) {
            case Unsorted:
                hasCustomImage = hasBinding("unsortedImageName"); break;
            case SortedAscending:
                hasCustomImage = hasBinding("sortedAscendingImageName"); break;
            case SortedDescending:
                hasCustomImage = hasBinding("sortedDescendingImageName"); break;
        }
        return hasCustomImage;
    }

    public String imageNameForCurrentState() {
        String imageName = null;
        switch(currentState()) {
            case Unsorted:
                imageName = unsortedImageName(); break;
            case SortedAscending:
                imageName = sortedAscendingImageName(); break;
            case SortedDescending:
                imageName = sortedDescendingImageName(); break;
        }
        return imageName;
    }

    public String customImageSrcForCurrentState() {
        String src = null;
        switch(currentState()) {
            case Unsorted:
                src = (String)valueForBinding("unsortedImageSrc"); break;
            case SortedAscending:
                src = (String)valueForBinding("sortedAscendingImageSrc"); break;
            case SortedDescending:
                src = (String)valueForBinding("sortedDescendingImageSrc"); break;
        }
        return src;
    }
    
    public String frameworkName() {
        return hasCustomImageNameForCurrentState() ? (hasBinding("frameworkName") ? (String)valueForBinding("frameworkName") : null) : "JavaWOExtensions";
    }

    public String unsortedImageName() { return hasBinding("unsortedImageName") ? (String)valueForBinding("unsortedImageName") : "Unsorted.gif"; }
    public String sortedAscendingImageName() {
        return hasBinding("sortedAscendingImageName") ? (String)valueForBinding("sortedAscendingImageName") : "Ascending.gif";
    }
    public String sortedDescendingImageName() {
        return hasBinding("sortedDescendingImageName") ? (String)valueForBinding("sortedDescendingImageName") : "Descending.gif";
    }

    // FIXME: Should post a notification even if d2wContext isn't bound.
    public WOComponent toggleClicked() {
        super.toggleClicked();
        if (log.isDebugEnabled()) log.debug("toggleClicked "+valueForBinding("d2wContext"));
        if (valueForBinding("d2wContext") != null) {
            NSNotificationCenter.defaultCenter().postNotification(SortOrderingChanged,
                                                                  displayGroup().sortOrderings(),
                                                                  new NSDictionary(valueForBinding("d2wContext"), "d2wContext"));
        }
        return null;
    }

    // These come right out of WOSortOrder, but have protected access instead of private.
    protected EOSortOrdering _primarySortOrdering() {
        NSArray nsarray = displayGroup().sortOrderings();
        if (nsarray != null && nsarray.count() > 0) {
            EOSortOrdering eosortordering = (EOSortOrdering)nsarray.objectAtIndex(0);
            return eosortordering;
        } else {
            return null;
        }
    }

    protected NSSelector _primaryKeySortOrderingSelector() {
        EOSortOrdering eosortordering = _primarySortOrdering();
        NSSelector nsselector = null;
        if(eosortordering != null)
            nsselector = eosortordering.selector();
        return nsselector;
    }

    protected boolean _isCurrentKeyPrimary() {
        EOSortOrdering eosortordering = _primarySortOrdering();
        return eosortordering != null && eosortordering.key().equals(key());
    }
}
