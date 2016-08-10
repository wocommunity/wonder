/*
 * WXOutlineEntry.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSRange;

public class WXOutlineEntry extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected int _nestingLevel;

    public WXOutlineEntry(WOContext aContext)  {
        super(aContext);
    }

    /////////////
    // No-Sync
    ////////////
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    @Override
    public void awake() {
        super.awake();
        Object nestLevelBinding = _WOJExtensionsUtil.valueForBindingOrNull("nestingLevel",this);
        if (nestLevelBinding instanceof Number) {
            _nestingLevel = ((Number)nestLevelBinding).intValue();
            return;
        }

        if ((nestLevelBinding == null) || nestLevelBinding.equals("")) {
            _nestingLevel = 0;
            return;
        }
        try {
            _nestingLevel = Integer.parseInt(nestLevelBinding.toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("WXOutLineEntry - problem parsing int from nestingLevel binding "+e);
        }
    }

    public int nestingLevel() {
        return _nestingLevel;
    }

    public boolean isExpanded() {
        Object currentItem = valueForBinding("item");
        NSArray selectionPath = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("selectionPath",this);
        return (_nestingLevel < selectionPath.count())
            && selectionPath.objectAtIndex(_nestingLevel).equals(currentItem);
    }

    public int nestingLevelForChildren() {
        return _nestingLevel+1;
    }

    public WOComponent toggleExpansion() {
        @SuppressWarnings("unchecked")
		NSArray<Object> selectionPath = (NSArray<Object>)_WOJExtensionsUtil.valueForBindingOrNull("selectionPath",this);

        selectionPath = selectionPath.subarrayWithRange(new NSRange(0, _nestingLevel));

        if (!isExpanded()) {
            Object currentItem = valueForBinding("item");
    //        NSLog(@"*** currentItem=%@", currentItem);
            selectionPath = selectionPath.arrayByAddingObject(currentItem);
        }

        setValueForBinding(selectionPath, "selectionPath");
        return null;
    }

    public boolean hasChildren() {
        return ((Boolean)valueForBinding("hasChildren")).booleanValue();
    }


    @Override
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        session().setObjectForKey(this, "_outlineEntry");
        super.takeValuesFromRequest(aRequest, aContext);
    }

    @Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
        WOActionResults returnElement;
        session().setObjectForKey(this, "_outlineEntry");
        returnElement = super.invokeAction(aRequest, aContext);
        return returnElement;
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        session().setObjectForKey(this, "_outlineEntry");
        super.appendToResponse(aResponse, aContext);
    }
}
