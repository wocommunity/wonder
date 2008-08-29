/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WList;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import er.extensions.ERXArrayUtilities;
import org.apache.log4j.Logger;

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
    
    protected EOArrayDataSource _dataSource;

    public ERXD2WList(WOContext context) {
        super(context);
    }

    /**
     * Calling super is a bad thing in 5.2 when used as an embedded list.
     */
    public void awake() {}

    public EODataSource dataSource() {
        if (this.hasBinding("dataSource") && (this.valueForBinding("list") == null || ((NSArray)this.valueForBinding("list")).count() == 0))
            return (EODataSource) this.valueForBinding("dataSource");
        if (this.hasBinding("list")) {
            NSArray nsarray = (NSArray) this.valueForBinding("list");
            if( nsarray != null && nsarray.count() > 0 ) {
                nsarray = ERXArrayUtilities.removeNullValues(nsarray);
                if( nsarray != null && nsarray.count() > 0 ) {
                    EOEnterpriseObject firstObject = (EOEnterpriseObject)ERXArrayUtilities.firstObject(nsarray); //if our array is composed of non-EOs, we will blow up here; that's convenient, since the stacktrace will lead us to look here, and discover that binding a dataSource rather than a list is the solution
                    if( firstObject != null ) { //the null checks before here aren't strictly needed, but will save some allocations
                        EOEditingContext eoeditingcontext = firstObject.editingContext();
                        if( eoeditingcontext != null ) {
                            EOClassDescription classDescription = null;

                            String entityName = (String) this.valueForBinding("entityName");
                            if( entityName != null ) {
                                classDescription = EOClassDescription.classDescriptionForEntityName(entityName);
                            }

                            if(classDescription == null) {
                                classDescription = firstObject.classDescription();
                            }

                            if(classDescription != null) {
                                if (_dataSource == null || !classDescription.equals(_dataSource.classDescriptionForObjects()) || _dataSource.editingContext() != eoeditingcontext) {
                                    _dataSource = new EOArrayDataSource(classDescription, eoeditingcontext);
                                }
                                if (_dataSource != null) {
                                    _dataSource.setArray(nsarray);
                                }
                                return _dataSource;
                            }
                        }
                    }
                }
            }
            _dataSource = null; //if we got here then some check above failed, so clear out our cached data source
        }
        return null;
    }
}
