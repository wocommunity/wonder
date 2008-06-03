/*
 * WOAggregateEventRow.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOAggregateEvent;
import com.webobjects.eocontrol.EOEvent;

public class WOAggregateEventRow extends WOEventRow {
    public WOAggregateEventRow(WOContext aContext)  {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() {
        // Do not sync with the bindings
        return false;
    }

    public EOAggregateEvent object()
    {
        return (EOAggregateEvent)_WOJExtensionsUtil.valueForBindingOrNull("object",this);
    }

    public WOEventDisplayPage controller()    {
        return (WOEventDisplayPage)_WOJExtensionsUtil.valueForBindingOrNull("controller",this);
    }

    public int displayMode()
    {
        int result = 1;
        Object resultStr = valueForBinding("displayMode");
        if (resultStr != null) {
            try {
                result = Integer.parseInt(resultStr.toString());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("WOAggregateEventRow - problem parsing int from displayMode binding "+e);
            }
        }
        return result;
    }
    
    public EOEvent event()
    {
        return (EOEvent)object().events().objectAtIndex(0);
    }

    public String displayComponentName()
    {
        WOEventDisplayPage ctr;
        int level, group;
        EOEvent obj;

        obj = object();
        ctr = controller();
        level = ctr.displayLevelForEvent(obj);
        group = ctr.groupTagForDisplayLevel(level);

        if (group != -1)
            return "WOEventRow";
        else
            return event().displayComponentName();
    }
}