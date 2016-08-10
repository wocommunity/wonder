/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditToManyRelationship;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;

import er.extensions.foundation.ERXUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Improves superclass by adding restrictions on the choices and uses ERXToManyRelationship, thus can handle localization
 * and has better layout options.
 * @d2wKey restrictedChoiceKey keypath off the component that returns the list of objects to display
 * @d2wKey restrictingFetchSpecification name of the fetchSpec to use for the list of objects.
 * @d2wKey collapsibleComponentName
 * @d2wKey allowCollapsing
 * @d2wKey relationship
 * @d2wKey numCols
 * @d2wKey propertyKey
 * @d2wKey size
 * @d2wKey entity
 * @d2wKey itemWidth
 * @d2wKey toManyUIStyle
 * @d2wKey goingVertically
 * @d2wKey sortKey
 * @d2wKey localizeDisplayKeys
 * @d2wKey destinationEntityName
 * @d2wKey sortCaseInsensitive
 * @d2wKey id
 * @d2wKey title
 * @d2wKey name
 * @d2wKey selectedChoiceKey
 * @d2wKey shouldShowSelectAllButtons
 */
public class ERD2WEditToManyRelationship extends D2WEditToManyRelationship {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WEditToManyRelationship(WOContext context) { super(context); }
    
    // Validation Support
    @Override
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }
    
    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
        if( restrictedChoiceKey!=null && restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)d2wContext().valueForKey("restrictingFetchSpecification");
        if(fetchSpecName != null) {
            EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath(object(), (String)d2wContext().valueForKey("propertyKey"));
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
