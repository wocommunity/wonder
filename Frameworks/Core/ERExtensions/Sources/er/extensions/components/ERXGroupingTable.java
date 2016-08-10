/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Table implementation of a grouping repetition.
 * 
 * @binding list NSArray
 * @binding item An object coming from the list
 * @binding sectionForItem
 * @binding subList
 * @binding subListSection
 * @binding sectionKey
 * @binding maxColumns
 * @binding col
 * @binding index
 * @binding row
 * @binding tableBackgroundColor
 * @binding border
 * @binding cellpadding
 * @binding cellspacing
 * @binding rowBackgroundColor
 * @binding cellBackgroundColor
 * @binding cellAlign
 * @binding cellVAlign
 * @binding cellWidth
 * @binding tableWidth
 * @binding goingVertically defaults=Boolean
 */

public class ERXGroupingTable extends ERXGroupingRepetition {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXGroupingTable(WOContext context) {
        super(context);
    }
}
