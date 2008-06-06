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
 */

public class ERD2WTwoLevelEditToOneRelationship extends D2WEditToOneRelationship {
    
    public ERD2WTwoLevelEditToOneRelationship(WOContext context) { super(context); }
}