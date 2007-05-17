/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditToManyRelationship;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.ERXUtilities;
import er.extensions.ERXValueUtilities;

/**
 * Improves superclass by adding restrictions on the choices and uses ERXToManyRelationship, thus can handle localization
 * and has better layout options.
 */

public class ERD2WEditToManyRelationship extends D2WEditToManyRelationship {

    public ERD2WEditToManyRelationship(WOContext context) { super(context); }
    
    // Validation Support
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }
    
    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
        if( restrictedChoiceKey!=null && restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)d2wContext().valueForKey("restrictingFetchSpecification");
        if(fetchSpecName != null) {
            EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath((EOEnterpriseObject)object(), (String)d2wContext().valueForKey("propertyKey"));
            if(relationship != null)
                return EOUtilities.objectsWithFetchSpecificationAndBindings(object().editingContext(), relationship.destinationEntity().name(),fetchSpecName,null);
        }
        return null;
    }

    private Object _selectedChoiceList = null;
    public Object selectedChoiceList() {
        if (_selectedChoiceList == null) {
            String selectedChoiceKey=(String)d2wContext().valueForKey("selectedChoiceKey");
            _selectedChoiceList = selectedChoiceKey!=null && selectedChoiceKey.length()>0 ? valueForKeyPath(selectedChoiceKey) : null;
        }
        return _selectedChoiceList;
    }
    public void setSelectedChoiceList(Object selectedChoiceList) {
        _selectedChoiceList = selectedChoiceList;
    }

    public boolean shouldShowSelectAllButtons() {
        boolean result = false;
        if (canGetValueForBinding("shouldShowSelectAllButtons")) {
            result = ERXValueUtilities.booleanValue(valueForBinding("shouldShowSelectAllButtons"));
        } else {
            result = ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldShowSelectAllButtons"));
        }
        return result;
    }
}
