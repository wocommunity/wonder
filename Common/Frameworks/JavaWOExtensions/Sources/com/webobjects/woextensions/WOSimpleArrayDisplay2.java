/*
 * WOSimpleArrayDisplay2.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;

import java.util.*;

public class WOSimpleArrayDisplay2 extends WOSimpleArrayDisplay
{
    // internal/private
    protected String listTarget;
    protected String itemTarget;

    public WOSimpleArrayDisplay2(WOContext aContext)  {
        super(aContext);
    }

    public String  listTarget()  {
        if (null==listTarget) {
            listTarget=(String)_WOJExtensionsUtil.valueForBindingOrNull("listTarget",this);
        }
        return listTarget;
    }
    public String  itemTarget()  {
        if (null==itemTarget) {
            itemTarget=(String)_WOJExtensionsUtil.valueForBindingOrNull("itemTarget",this);
        }
        return itemTarget;
    }

    public void setCurrentItem(Object newItem)  {
        if (hasBinding("item")) {
            setValueForBinding(newItem, "item");
        }
        currentItem=newItem;
    }

    public WOElement displayItemClicked()  {
        return (WOElement)_WOJExtensionsUtil.valueForBindingOrNull("displayItemAction",this);
    }

    public String  inspectText()  {
        return list().count()+" items";
    }

    protected void _invalidateCaches() {
        listTarget = null;
        itemTarget = null;
        super._invalidateCaches();
    }
}
