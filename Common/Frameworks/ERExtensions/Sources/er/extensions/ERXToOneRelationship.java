/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;


import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOQualifier;

/**
 * Better layout options that the {@link WOToOneRelationship}, in addition you can set a 
 * qualifier on the dataSource if you passed any.<br />
 *
 * @binding dataSource
 * @binding destinationDisplayKey
 * @binding isMandatory
 * @binding relationshipKey
 * @binding sourceEntityName
 * @binding destinationEntityName
 * @binding sourceObject
 * @binding uiStyle
 * @binding destinationSortKey
 * @binding noSelectionString
 * @binding possibleChoices
 * @binding qualifier
 * @binding popupName
 * @binding size
 * @binding maxColumns
 * @binding localizeDisplayKeys" defaults="Boolean
 * @binding sortCaseInsensitive
 */
//CHECKME AK: does this make sense? Why not set the qualifier in the parent component?
public class ERXToOneRelationship extends WOToOneRelationship {

    public ERXToOneRelationship(WOContext context) {
        super(context);
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
