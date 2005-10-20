/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Embedded component that can be used for nesting page configurations, ie ERDInspect can be a customComponentName.<br />
 * 
 */

public class ERDInspect extends ERDCustomEditComponent {
    public ERDInspect(WOContext context) {
        super(context);
    }
    
    public boolean synchronizesVariablesWithBindings() { 
        return false; 
    }
    
    public NSDictionary settings() {
        String pc = d2wContext().dynamicPage();
        if(pc != null) {
            return new NSDictionary(pc, "parentPageConfiguration");
        }
        return null;
    }
    
    public WOComponent editRelationshipAction() {
        String editRelationshipConfigurationName = (String)valueForBinding("editRelationshipConfigurationName");
        EditRelationshipPageInterface epi = (EditRelationshipPageInterface)D2W.factory().pageForConfigurationNamed(editRelationshipConfigurationName, session());
        epi.setMasterObjectAndRelationshipKey(object(), key());
        epi.setNextPage(context().page());
        return (WOComponent)epi;
    }

    public WOComponent createObjectAction() {
        EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(object().editingContext(), (String) d2wContext().valueForKey("destinationEntityName"));
        object().addObjectToBothSidesOfRelationshipWithKey(eo, key());
        return context().page();
    }
    
    
    public WOComponent clearAction() {
        object().removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject)object().valueForKey(key()), key());
        return context().page();
    }
}
