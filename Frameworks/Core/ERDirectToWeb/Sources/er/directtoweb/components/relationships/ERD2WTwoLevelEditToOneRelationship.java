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
 * Uses JSTwoLevelEditToOneRelationship to edit the relationship.
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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WTwoLevelEditToOneRelationship(WOContext context) { super(context); }
}