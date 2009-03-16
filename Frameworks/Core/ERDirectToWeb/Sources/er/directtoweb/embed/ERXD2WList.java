/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.embed;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WList;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXArrayUtilities;

// Only difference between this component and D2WList is that this one uses ERD2WSwitchComponent
/**
 * Same as D2WList but uses ERD2WSwitchComponent so that its context won't be cached in case the page is reused.<br />
 * 
 * @binding action
 * @binding list
 * @binding entityName
 * @binding dataSource
 * @binding pageConfiguration
 * @binding displayKeys
 */

public class ERXD2WList extends D2WList {
    Logger log = Logger.getLogger(ERXD2WList.class);
    
    protected EOArrayDataSource _dataSource = null;

    public ERXD2WList(WOContext context) {
        super(context);
    }

    /**
     * Calling super is a bad thing in 5.2 when used as an embedded list.
     */
    public void awake() {}

    public EODataSource dataSource() {
        if (this.hasBinding("dataSource") && this.valueForBinding("list") == null)
            return (EODataSource) this.valueForBinding("dataSource");
        if (this.hasBinding("list")) {
            NSArray nsarray = (NSArray) this.valueForBinding("list");
            nsarray = ERXArrayUtilities.removeNullValues(nsarray);
            EOEditingContext eoeditingcontext
                = (nsarray != null && nsarray.count() > 0
                   ? ((EOEnterpriseObject) nsarray.objectAtIndex(0))
                   .editingContext()
                   : null);
            String entityName = (String) this.valueForBinding("entityName");
            if(entityName == null) {
                entityName = (nsarray != null && nsarray.count() > 0
                              ? ((EOEnterpriseObject) nsarray.objectAtIndex(0))
                              .entityName()
                              : null);
            }
            if(entityName != null) {
                if (_dataSource == null || !entityName.equals(_dataSource.classDescriptionForObjects().entityName()) || _dataSource.editingContext() != eoeditingcontext)
                    _dataSource = (eoeditingcontext != null
                                   ? (new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(entityName), eoeditingcontext)) : null);
            }
            if (_dataSource != null)
                _dataSource.setArray(nsarray);
        }
        return _dataSource;
    }
}
