/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.conditionals;

import java.util.List;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

/**
 * Conditional component that tests if a given item is contained in an
 * {@link java.util.List}.
 * 
 * @binding list array of objects
 * @binding item object to test inclusion in the list
 * @binding negate inverts the sense of the conditional.
 */
public class ERXListContainsItemConditional extends ERXWOConditional {

	protected WOAssociation _list;
	protected WOAssociation _item;

	public ERXListContainsItemConditional(String aName, NSDictionary aDict, WOElement aElement) {
		super(aName, aDict, aElement);
	}

	@Override
	protected void pullAssociations(NSDictionary<String, ? extends WOAssociation> dict) {
		_list = dict.objectForKey("list");
		_item = dict.objectForKey("item");
		if (_list == null || _item == null) {
			throw new WODynamicElementCreationException("list and item must be bound");
		}
	}

	/**
	 * Tests if the bound item is contained within the bound list.
	 */
	@Override
	protected boolean conditionInComponent(WOComponent component) {
		List list = (List) _list.valueInComponent(component);
		Object item = _item.valueInComponent(component);
		return list != null && list.contains(item);
	}
}
