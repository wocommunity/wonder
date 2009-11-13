/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditToOneRelationship;

/**
 * Uses JSTwoLevelEditToOneRelationship to edit the relationship.<br />
 * 
 * @d2wKey sortKey
 * @d2wKey restrictingRelationshipSortKey
 * @d2wKey entity
 * @d2wKey keyWhenRelationship
 * @d2wKey restrictingRelationshipKey
 * @d2wKey restrictingDestinationDisplayKey
 * @d2wKey defaultRestrictedRelationshipKey
 * @d2wKey restrictedRelationshipKey
 * @d2wKey restrictingSecondarySortKey
 */
public class ERD2WTwoLevelEditToOneRelationship extends D2WEditToOneRelationship {
    
    public ERD2WTwoLevelEditToOneRelationship(WOContext context) { super(context); }
}