/*
 * WOAggregateEventRow.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOAggregateEvent;
import com.webobjects.eocontrol.EOEvent;

public class WOAggregateEventRow extends WOEventRow {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public WOAggregateEventRow(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        // Do not sync with the bindings
        return false;
    }

    /** 
     * <span class="ja">object としてバインディングされている EOAggregateEvent を戻します。</span>
     */
    public EOAggregateEvent object()
    {
        return (EOAggregateEvent)_WOJExtensionsUtil.valueForBindingOrNull("object",this);
    }

    /** 
     * <span class="ja">WOEventDisplayPage コントロールを戻します </span>
     */
    public WOEventDisplayPage controller()    {
        return (WOEventDisplayPage)_WOJExtensionsUtil.valueForBindingOrNull("controller",this);
    }

    /** 
     * <span class="ja">表示モードを戻します </span>
     */
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
    
    /** 
     * <span class="ja">イベント </span>
     */
    public EOEvent event()
    {
        return object().events().objectAtIndex(0);
    }

    /** 
     * <span class="ja">表示するコンポーネント名 </span>
     */
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