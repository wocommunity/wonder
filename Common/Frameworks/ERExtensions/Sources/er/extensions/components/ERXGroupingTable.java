/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Table implementation of a grouping repetition.<br />
 * 
 * @binding list
 * @binding item
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
 * @binding goingVertically" defaults="Boolean
 */

public class ERXGroupingTable extends ERXGroupingRepetition {

    public ERXGroupingTable(WOContext context) {
        super(context);
    }
}
