/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * description forthcoming!<br />
 * 
 * @binding dataSource
 * @binding destinationDisplayKey
 * @binding isMandatory
 * @binding relationshipKey
 * @binding sourceEntityName
 * @binding destinationEntityName
 * @binding sourceObject
 * @binding uiStyle
 * @binding qualifier
 * @binding possibleChoices
 * @binding maxColumns
 * @binding size
 * @binding width
 * @binding destinationSortKey
 * @binding goingVertically
 * @binding localizeDisplayKeys" defaults="Boolean
 */

public class ERXToManyRelationship extends WOToManyRelationship {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXToManyRelationship.class);
 
    public ERXToManyRelationship(WOContext context) {
        super(context);
    }
    
    public NSArray selections() {
        if (_selections == null && canGetValueForBinding("selectedObjects")) {
            NSArray selectedObjects = (NSArray)valueForBinding("selectedObjects");
            if (selectedObjects != null)
                setSelections(selectedObjects);
        }
        return super.selections();
    }

    public EODataSource dataSource() {
        if (_dataSource==null) {
            _dataSource = super.dataSource();
            if (_dataSource != null && _dataSource instanceof EODatabaseDataSource) {
                if (hasBinding("qualifier")) {
                    ((EODatabaseDataSource)_dataSource).setAuxiliaryQualifier((EOQualifier)valueForBinding("qualifier"));
                }
            }
        }
        return _dataSource;
    }
}
