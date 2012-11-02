/*
 * WOSimpleArrayDisplay.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

public class WOSimpleArrayDisplay extends WOComponent
{
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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

    @Override
    public boolean isStateless() {
        return true;
    }

    @Override
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
