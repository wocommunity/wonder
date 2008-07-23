package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Create queries that consist of a drilldown.
 * Example: consider a Person working in a department, a Department belongs to a Company.
 * You'd query a Person by setting
 * @binding key = "whatever"
 * @binding destinationEntityName = "Company" 			, The entity in the first popup
 * @binding secondaryKey = "departments" 			, Company.departments
 * @binding primaryQueryKey = "department.company" 		, query Person.department.company (unused of ommited or null)
 * @binding secondaryQueryKey = "department" 			, query Person.department
 * @binding keyWhenRelationship = "companyName"			, Display key for Company
 * @binding secondaryKeyWhenRelationship = "departmentName"	, Display key for Dept
 * @binding displayNameForEntity = "Company"			, Label for Company
 * @binding displayNameForSecondaryEntity = "Departments"	, Label for Dept
 * @binding restrictedChoiceKey = "session.user.visibleCompanies", Restriction for the main entity
 * @created ak on Fri Nov 21 2003
 * @project ERDirectToWeb
 */

public class ERDQueryTwoLevelRelationship extends ERDCustomQueryComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDQueryTwoLevelRelationship.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryTwoLevelRelationship(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    /** eg. city */
    public String secondaryQueryKey() {
        return (String)valueForBinding("secondaryQueryKey");
    }
    /** eg. city.state */
    public String primaryQueryKey() {
        return (String)valueForBinding("primaryQueryKey");
    }
    public Object secondaryDisplayGroupQueryMatchValue() {
        return key() != null && displayGroup() != null ? displayGroup().queryMatch().objectForKey(secondaryQueryKey()) : null;
    }
    public void setSecondaryDisplayGroupQueryMatchValue (Object newValue) {
        if (secondaryQueryKey() != null && displayGroup () != null && displayGroup().queryMatch()!=null ) {
            if(newValue != null) {
                displayGroup().queryMatch().setObjectForKey(newValue,secondaryQueryKey());
            } else {
                displayGroup().queryMatch().removeObjectForKey(secondaryQueryKey());
            }
        }
    }
    public Object displayGroupQueryMatchValue() {
        return primaryQueryKey() != null && displayGroup() != null ? displayGroup().queryMatch().objectForKey(primaryQueryKey()) : null;
    }
    public void setDisplayGroupQueryMatchValue (Object newValue) {
        if (primaryQueryKey() != null && displayGroup () != null && displayGroup().queryMatch()!=null ) {
            if(newValue != null) {
                displayGroup().queryMatch().setObjectForKey(newValue,primaryQueryKey());
            } else {
                displayGroup().queryMatch().removeObjectForKey(primaryQueryKey());
            }
        }
    }
    
    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)valueForBinding("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)valueForBinding("restrictingFetchSpecification");
        if(fetchSpecName != null &&  fetchSpecName.length()>0) {
            EOEditingContext ec = ERXEC.newEditingContext();
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, (String)valueForBinding("destinationEntityName"),fetchSpecName,null);
        }
        return null;
    }
}
