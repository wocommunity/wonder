/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Uses JSPopUpRelationPicker to edit a toOne relationship.
 */

public class ERXJSTwoLevelEditToOneRelationship extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXJSTwoLevelEditToOneRelationship(WOContext aContext) {
        super(aContext);
    }

    private NSArray _parentList=null;
    
/*
 type --> category --> defaultType

    sourceEntityName = Gift;
    relationshipKey = type;
    destinationDisplayKey = textDescription;
    restrictingRelationshipKey = category;
    restrictingDestinationDisplayKey = textDescription;
    defaultRestrictedRelationshipKey = defaultType;


 */
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSArray parentList() {
        if (_parentList==null) {
            // FIXME this list should be shared with all other JSTwoLevel.. sharing the same sourceEntityName!
            String entityName=(String)valueForBinding("sourceEntityName");
            EOEditingContext ec=((EOEnterpriseObject)valueForBinding("sourceObject")).editingContext();
            EOEntity sourceEntity=EOUtilities.entityNamed(ec,entityName);
            EORelationship r1=sourceEntity.relationshipNamed((String)valueForBinding("relationshipKey"));
            EOEntity childEntity=r1.destinationEntity();
            EORelationship r2= childEntity.relationshipNamed((String)valueForBinding("restrictingRelationshipKey"));
            EOEntity parentEntity=r2.destinationEntity();
            NSArray unsortedList=EOUtilities.objectsForEntityNamed(ec,parentEntity.name());
            EOSortOrdering sortOrdering=new EOSortOrdering((String)valueForBinding("restrictingRelationshipSortKey"),
                                                           EOSortOrdering.CompareAscending);
            NSMutableArray sortArray=new NSMutableArray(sortOrdering);
            String secondarySortKey=(String)valueForBinding("restrictingSecondarySortKey");
            if (secondarySortKey!=null && secondarySortKey.length()>0) {
                sortOrdering=new EOSortOrdering(secondarySortKey,
                                                EOSortOrdering.CompareAscending);
                sortArray.addObject(sortOrdering);
            }
            _parentList=EOSortOrdering.sortedArrayUsingKeyOrderArray(unsortedList, sortArray);
        }
        return _parentList;
    }

    public EOEnterpriseObject selectedParent() {
        EOEnterpriseObject selectedChild=selectedChild();
        return selectedChild!=null ?
            (EOEnterpriseObject)selectedChild.valueForKey((String)valueForBinding("restrictingRelationshipKey")) : null;
    }
    public void setSelectedParent(EOEnterpriseObject newValue) { // FIXME
        // do nothing, there is always a setSelectedChild with the JS subcomponent
    }
    public EOEnterpriseObject selectedChild() {
        return (EOEnterpriseObject)
        ((EOEnterpriseObject)valueForBinding("sourceObject")).valueForKey((String)valueForBinding("relationshipKey"));
    }
    public void setSelectedChild(EOEnterpriseObject newValue) { 
        ((EOEnterpriseObject)valueForBinding("sourceObject")).takeValueForKey(newValue,
                                                                              (String)valueForBinding("relationshipKey"));   
    }
}
