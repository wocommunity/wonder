package er.directtoweb.components.relationships;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier;

/**
 * Create queries that consist of a drilldown.
 * Example: consider a Person working in a department, a Department belongs to a Company.
 * You'd query for Persons by setting
 * key = "whatever"                         , not used
 * multiple = true                          , we want to select many departments
 * size = 5                                 , we want to select many departments
 * destinationEntityName = "Company" 		 , The entity in the first popup
 * secondaryKey = "departments" 			     , Company.departments
 * primaryQueryKey = "department.company" 	 , query Person.department.company (unused of ommited or null)
 * secondaryQueryKey = "department" 			 , query Person.department
 * keyWhenRelationship = "companyName"		 , Display key for Company
 * secondaryKeyWhenRelationship = "departmentName"	, Display key for Dept
 * displayNameForEntity = "Company"			, Label for Company
 * displayNameForSecondaryEntity = "Departments"	, Label for Dept
 * restrictedChoiceKey = "session.user.visibleCompanies", Restriction for the main entity, if unset
 *           all objects of destinationEntityName are used
 * restrictedChildrenChoiceKey = "session.user.visibleDepartments", Restriction on the children entity,
 *          if unset all children are shown
 * displayGroup = display group the query is in
 * 
 * @author ak on Fri Nov 21 2003
 */
public class ERDQueryTwoLevelRelationship extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDQueryTwoLevelRelationship.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryTwoLevelRelationship(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    /** eg. city */
    public String secondaryQueryKey() {
        return (String)valueForBinding("secondaryQueryKey");
    }
    /** eg. city.state */
    public String primaryQueryKey() {
        return (String)valueForBinding("primaryQueryKey");
    }

    public boolean multiple() {
        return booleanValueForBinding("multiple");
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
            if(multiple()) {
                displayGroup().queryOperator().setObjectForKey(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName, 
                        secondaryQueryKey());
            } else {
                displayGroup().queryOperator().setObjectForKey(EOQualifier.QualifierOperatorEqual.name(), secondaryQueryKey());
            }
        }
    }
    
    @Override
    public Object displayGroupQueryMatchValue() {
        return primaryQueryKey() != null && displayGroup() != null ? displayGroup().queryMatch().objectForKey(primaryQueryKey()) : null;
    }
    
    @Override
    public void setDisplayGroupQueryMatchValue (Object newValue) {
        if (primaryQueryKey() != null && displayGroup () != null && displayGroup().queryMatch()!=null ) {
            if(newValue != null) {
                displayGroup().queryMatch().setObjectForKey(newValue,primaryQueryKey());
            } else {
                displayGroup().queryMatch().removeObjectForKey(primaryQueryKey());
            }
            if(multiple()) {
                displayGroup().queryOperator().setObjectForKey(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName, 
                        primaryQueryKey());
            } else {
                displayGroup().queryOperator().setObjectForKey(EOQualifier.QualifierOperatorEqual.name(), primaryQueryKey());
            }
        }
    }
    
    public Object theList() {
        String restrictedChoiceKey=(String)valueForBinding("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        EOEditingContext ec = displayGroup().dataSource().editingContext();
        String destinationEntityName = (String)valueForBinding("destinationEntityName");
        String restrictingFetchSpecification=(String)valueForBinding("restrictingFetchSpecification");
        if(restrictingFetchSpecification != null &&  restrictingFetchSpecification.length()>0) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, destinationEntityName, restrictingFetchSpecification,null);
        }
        return EOUtilities.objectsForEntityNamed(ec, destinationEntityName);
    }
    
    public NSArray possibleChildren() {
        String key = (String)valueForBinding("restrictedChildrenKey");
        return (NSArray) (key == null ? null : valueForKeyPath(key));
    }
}
