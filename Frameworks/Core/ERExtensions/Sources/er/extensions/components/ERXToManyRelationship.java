/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.extensions.woextensions.WOToManyRelationship;

/**
 * Better layout options that the {@link WOToManyRelationship}, in addition you can set a 
 * qualifier on the dataSource if you passed any.
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
 * @binding localizeDisplayKeys
 * @binding sortCaseInsensitive
 */
public class ERXToManyRelationship extends WOToManyRelationship {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXToManyRelationship.class);
    
    public final String checkBoxComponentName = ERXProperties.stringForKeyWithDefault("er.extensions.components.ERXToManyRelationship.checkBoxComponentName", "ERXCheckboxMatrix");
 
    public ERXToManyRelationship(WOContext context) {
        super(context);
    }
    
    @Override
    public NSArray selections() {
        if (_selections == null && canGetValueForBinding("selectedObjects")) {
            NSArray selectedObjects = (NSArray)valueForBinding("selectedObjects");
            if (selectedObjects != null)
                setSelections(selectedObjects);
        }
        return super.selections();
    }

    @Override
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

    @Override
    public boolean isBrowser() {
        return !(isCheckBox() || isJSEditor()); // Browser is the default.
    }

    public boolean isJSEditor() {
        return "jsEditor".equalsIgnoreCase(uiStyle());
    }
}
