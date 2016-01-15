/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.woextensions.WOSortOrder;

//////////////////////////////////////////////////////////////////////////////////////////////////////
// ERSortOrder is an extension of WOSortOrder.  It adds two main abilities to WOSortOrder.
//	1) It providers the ability to use custom images for the different sorting states
//	2) It posts a notification when the sort ordering changes.  This second one is nice
//		if you want the app to 'remember' what the last sort was for a user.
//////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Better sort order changer. Useful for providing custom sort order images and remembering the sort orderings.
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
 * @binding width the width of the image
 * @binding height the height of the image
 */

public class ERXSortOrder extends WOSortOrder {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXSortOrder(WOContext context) {
        super(context);
    }
    
    private final static Logger log = LoggerFactory.getLogger(ERXSortOrder.class);

    //////////////////////////////////////////////// Notification Hooks //////////////////////////////////////////
    public final static String SortOrderingChanged = "SortOrderingChanged";

    //////////////////////////////////////////////// States //////////////////////////////////////////////////////
    public final static int Reset = -1;
    public final static int Unsorted = 0;
    public final static int SortedAscending = 1;
    public final static int SortedDescending = 2;

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    @Override
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
                if (aCurrentState.equals(EOSortOrdering.CompareAscending) 
                		|| aCurrentState.equals(EOSortOrdering.CompareCaseInsensitiveAscending)) {
                    _currentState = SortedAscending;
                } else if (aCurrentState.equals(EOSortOrdering.CompareDescending) 
                		|| aCurrentState.equals(EOSortOrdering.CompareCaseInsensitiveDescending)) {
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
                hasCustomImage = !ERXStringUtilities.stringIsNullOrEmpty((String)valueForBinding("unsortedImageSrc")); break;
            case SortedAscending:
                hasCustomImage = !ERXStringUtilities.stringIsNullOrEmpty((String)valueForBinding("sortedAscendingImageSrc")); break;
            case SortedDescending:
                hasCustomImage = !ERXStringUtilities.stringIsNullOrEmpty((String)valueForBinding("sortedAscendingImageSrc")); break;
        }
        return hasCustomImage;
    }
    
    public boolean hasCustomImageNameForCurrentState() {
        boolean hasCustomImage = false;
        switch(currentState()) {
            case Unsorted:
                hasCustomImage = !ERXStringUtilities.stringIsNullOrEmpty((String)valueForBinding("unsortedImageName")); break;
            case SortedAscending:
                hasCustomImage = !ERXStringUtilities.stringIsNullOrEmpty((String)valueForBinding("sortedAscendingImageName")); break;
            case SortedDescending:
                hasCustomImage = !ERXStringUtilities.stringIsNullOrEmpty((String)valueForBinding("sortedDescendingImageName")); break;
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
    
    @Override
    public String frameworkName() {
        return hasCustomImageNameForCurrentState() ? 
                (hasBinding("frameworkName") ?
                        (String)valueForBinding("frameworkName") 
                        : "app") 
                : "JavaWOExtensions";
    }

    public String unsortedImageName() { return hasBinding("unsortedImageName") ? (String)valueForBinding("unsortedImageName") : "Unsorted.gif"; }
    public String sortedAscendingImageName() {
        return hasBinding("sortedAscendingImageName") ? (String)valueForBinding("sortedAscendingImageName") : "Ascending.gif";
    }
    public String sortedDescendingImageName() {
        return hasBinding("sortedDescendingImageName") ? (String)valueForBinding("sortedDescendingImageName") : "Descending.gif";
    }
    
    public Object width() {
    	return hasBinding("width") ? valueForBinding("width") : "9";
    }
    
    public Object height() {
    	return hasBinding("height") ? valueForBinding("height") : "11";
    }

    // FIXME: Should post a notification even if d2wContext isn't bound.
    @Override
    public WOComponent toggleClicked() {
        super.toggleClicked();
        Object context = valueForBinding("d2wContext");
        log.debug("toggleClicked {}", context);
        if (context != null) {
            NSNotificationCenter.defaultCenter().postNotification(SortOrderingChanged,
                                                                  displayGroup().sortOrderings(),
                                                                  new NSDictionary(context, "d2wContext"));
        }
        return null;
    }
    
    @Override
    public String helpString() {
       return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERXSortOrder.sortBy", this);
    }

    // These come right out of WOSortOrder, but have protected access instead of private.
    protected EOSortOrdering _primarySortOrdering() {
        NSArray nsarray = displayGroup().sortOrderings();
        if (nsarray != null && nsarray.count() > 0) {
            EOSortOrdering eosortordering = (EOSortOrdering)nsarray.objectAtIndex(0);
            return eosortordering;
        }
        return null;
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
        return eosortordering != null &&  eosortordering.key() != null &&  eosortordering.key().equals(key());
    }
}
