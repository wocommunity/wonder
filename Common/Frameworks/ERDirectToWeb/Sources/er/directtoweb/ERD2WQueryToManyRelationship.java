package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import er.extensions.*;

/**
 * Same as original but used ERXToOneRelationship and allows you to restrict the objects shown.<br />
 * 
 * @created ak on Wed Apr 07 2004
 * @project ERDirectToWeb
 */

public class ERD2WQueryToManyRelationship extends D2WQueryToManyRelationship {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WQueryToManyRelationship.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WQueryToManyRelationship(WOContext context) {
        super(context);
    }
    
    public void awake() {
        super.awake();
        //displayGroup().queryOperator().takeValueForKey(ERXPrimaryKeyListQualifier.IsContainedInSelectorName, propertyKey());
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
