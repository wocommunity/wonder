package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryToManyRelationship;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;
import er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier;
import er.extensions.eof.qualifiers.ERXToManyQualifier;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Enhanced relationship query component to to-many relationships.
 * @d2wKey multiple when true, the user can choose multiple items
 * @d2wKey matchesAllValues when true matching values need all selected values, when false only one value of the selection is enough
 * @d2wKey restrictedChoiceKey keypath off the component that returns the list of objects to display
 * @d2wKey restrictingFetchSpecification name of the fetchSpec to use for the list of objects.
 * @d2wKey keyWhenRelationship
 * @d2wKey numCols
 * @d2wKey size
 * @d2wKey entity
 * @d2wKey toOneUIStyle
 * @d2wKey localizeDisplayKeys
 * @d2wKey destinationEntityName
 * @d2wKey isMandatory
 * @d2wKey sortKey
 * @d2wKey noSelectionString
 * @d2wKey id
 * @d2wKey popupName
 * @d2wKey propertyKey
 */
public class ERD2WQueryToManyRelationship extends D2WQueryToManyRelationship {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERD2WQueryToManyRelationship.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WQueryToManyRelationship(WOContext context) {
        super(context);
    }

    public boolean hasMultipleSelection() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("multiple"));
    }
    
    public String componentName() {
        return !hasMultipleSelection() ? "ERXToOneRelationship" :  "ERXToManyRelationship";
    }
    
    public WOComponent self() {
        return this;
    }
    
    public boolean matchesAllValues() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("matchesAllValues"));
    }

    @Override
    public void setValue(Object newValue) {
        if(hasMultipleSelection()) {
            if (newValue instanceof NSArray) {
                NSArray array = (NSArray) newValue;
                if(array.count() == 0) {
                    newValue = null;
                }
            }
            String operator = ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName;
            if(matchesAllValues()) {
                operator = ERXToManyQualifier.MatchesAllInArraySelectorName;
            }
            displayGroup().queryOperator().takeValueForKey(operator, propertyKey());
        }
        super.setValue(newValue);
    }

    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length() > 0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)d2wContext().valueForKey("restrictingFetchSpecification");
        if(fetchSpecName != null) {
            EOEditingContext ec = ERXEC.newEditingContext();
            EOEntity entity = d2wContext().entity();
            EORelationship relationship = entity.relationshipNamed((String)d2wContext().valueForKey("propertyKey"));
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, relationship.destinationEntity().name(),fetchSpecName,null);
        }
        return null;
    }
}
