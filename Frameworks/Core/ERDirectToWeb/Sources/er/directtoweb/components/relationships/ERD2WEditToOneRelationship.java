/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditToOneRelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * Improves superclass by adding restrictions on the choices and uses ERXToOneRelationship, thus can handle localization
 * and has better layout options.
 * @d2wKey restrictedChoiceKey keypath off the component that returns the list of objects to display
 * @d2wKey restrictingFetchSpecification name of the fetchSpec to use for the list of objects.
 * @d2wKey extraRestrictingQualifier pass a qualifier using @ERDDelayedExtraQualifierAssignment
 * @d2wKey sortKey
 * @d2wKey numCols
 * @d2wKey propertyKey
 * @d2wKey size
 * @d2wKey toOneUIStyle
 * @d2wKey noSelectionString
 * @d2wKey popupName
 * @d2wKey localizeDisplayKeys
 * @d2wKey uniqueID
 * @d2wKey destinationEntityName
 * @d2wKey sortCaseInsensitive
 * @d2wKey id
 * @d2wKey title
 * @d2wKey goingVertically
 */
public class ERD2WEditToOneRelationship extends D2WEditToOneRelationship {

    /**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
  
	public interface Keys extends ERDCustomEditComponent.Keys {
	    public static final String restrictedChoiceKey = "restrictedChoiceKey";
	    public static final String restrictingFetchSpecification = "restrictingFetchSpecification";
	    public static final String extraRestrictingQualifier = "extraRestrictingQualifier";
	    public static final String sortKey = "sortKey";
	    public static final String numCols = "numCols";
	    public static final String propertyKey = "propertyKey";
	    public static final String size = "size";
	    public static final String toOneUIStyle = "toOneUIStyle";
	    public static final String noSelectionString = "noSelectionString";
	    public static final String popupName = "popupName";
	    public static final String localizeDisplayKeys = "localizeDisplayKeys";
	    public static final String uniqueID = "uniqueID";
	    public static final String destinationEntityName = "destinationEntityName";
	    public static final String sortCaseInsensitive = "sortCaseInsensitive";
	    public static final String id = "id";
	    public static final String title = "title";
	    public static final String goingVertically = "goingVertically";
	}
	
	private EOQualifier _extraQualifier;

    public ERD2WEditToOneRelationship(WOContext context) {
        super(context);
    }
    
    // Validation Support
    @Override
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }

    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)d2wContext().valueForKey(Keys.restrictedChoiceKey);
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)d2wContext().valueForKey(Keys.restrictingFetchSpecification);
        if(fetchSpecName != null) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(object()
                    .editingContext(), relationship().destinationEntity().name(),
                    fetchSpecName, null);
        }
        if (extraQualifier() != null) {
            EOFetchSpecification fs = new EOFetchSpecification(relationship()
                    .destinationEntity().name(), extraQualifier(), null);
            return object().editingContext().objectsWithFetchSpecification(fs);
        }
        return null;
    }
    
    public EOQualifier extraQualifier() {
        if (_extraQualifier == null) {
            _extraQualifier = (EOQualifier) d2wContext().valueForKey(Keys.extraRestrictingQualifier);
        }
        return _extraQualifier;
    }

}
