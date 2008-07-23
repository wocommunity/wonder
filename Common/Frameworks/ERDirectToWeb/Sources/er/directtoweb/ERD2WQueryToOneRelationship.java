/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryToOneRelationship;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

/**
 * Same as original but used ERToOneRelationship.<br />
 * 
 */

public class ERD2WQueryToOneRelationship extends D2WQueryToOneRelationship {
    static final ERXLogger log = ERXLogger.getERXLogger(ERD2WQueryToOneRelationship.class);

    public ERD2WQueryToOneRelationship(WOContext context) {
        super(context);
    }

    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)d2wContext().valueForKey("restrictingFetchSpecification");
        if(fetchSpecName != null) {
            EOEditingContext ec = ERXEC.newEditingContext();
            EOEntity entity = d2wContext().entity();
            EORelationship relationship = entity.relationshipNamed((String)d2wContext().valueForKey("propertyKey"));
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, relationship.destinationEntity().name(),fetchSpecName,null);
        }
        return null;
    }
}
