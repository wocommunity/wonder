/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

/**
 * Used to edit a toMany relationship by allowing the user to pick the eos that belong in the relationship.<br />
 * 
 * @binding object
 * @binding key
 * @binding emptyListMessage
 * @binding listPageConfiguration
 * @binding list
 */

public class ERDList extends ERDCustomEditComponent {

    /* logging support */
    static final ERXLogger log = ERXLogger.getERXLogger(ERDList.class);
    
    protected NSArray list;

    public ERDList(WOContext context) { super(context); }

    public boolean synchronizesVariablesWithBindings() { return false; }

    public void reset() {
        list = null;
        super.reset();
    }

    // we will get asked quite a lot of times, so caching is in order
    
    public NSArray list() {
        if (list == null) {
            try {
                if (hasBinding("list")) {
                    list = (NSArray)valueForBinding("list");
                } else {
                    list = (NSArray)objectKeyPathValue();
                }
            } catch(java.lang.ClassCastException ex) {
                // (ak) This happens quite often when you haven't set up all display keys...
                // the statement makes this more easy to debug
                log.error(ex + " while getting " + key() + " of " + object());
            }
            if (list == null)
                list = NSArray.EmptyArray;
        }
        return list;
    }

    // This is fine because we only use the D2WList if we have at least one element in the list.

    // FIXME: This sucks.
    public boolean isTargetXML(){
        String listConfigurationName = stringValueForBinding("listConfigurationName");
        return listConfigurationName != null && listConfigurationName.indexOf("XML") > -1;
    }

    public boolean erD2WListOmitCenterTag() {
        return hasBinding("erD2WListOmitCenterTag") ? booleanValueForBinding("erD2WListOmitCenterTag") : false;
    }
}
