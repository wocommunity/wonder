/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * Embedded component that can be used for nesting page configurations, ie ERDInspect can be a customComponentName.
 * 
 * @d2wKey destinationEntityName
 */
public class ERDInspect extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDInspect(WOContext context) {
        super(context);
    }
    
    @Override
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
