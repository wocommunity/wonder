// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Employee.java instead.
package er.erxtest.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;
import er.extensions.ERXGenericRecord;
import er.extensions.ERXKey;

@SuppressWarnings("all")
public abstract class _Employee extends er.extensions.ERXGenericRecord {
	public static final String ENTITY_NAME = "Employee";

	// Attributes
	public static final String ADDRESS1_KEY = "address1";
	public static final ERXKey ADDRESS1 = new ERXKey(ADDRESS1_KEY);
	public static final String ADDRESS2_KEY = "address2";
	public static final ERXKey ADDRESS2 = new ERXKey(ADDRESS2_KEY);
	public static final String CITY_KEY = "city";
	public static final ERXKey CITY = new ERXKey(CITY_KEY);
	public static final String MANAGER_KEY = "manager";
	public static final ERXKey MANAGER = new ERXKey(MANAGER_KEY);
	public static final String NAME_KEY = "name";
	public static final ERXKey NAME = new ERXKey(NAME_KEY);
	public static final String STATE_KEY = "state";
	public static final ERXKey STATE = new ERXKey(STATE_KEY);
	public static final String ZIPCODE_KEY = "zipcode";
	public static final ERXKey ZIPCODE = new ERXKey(ZIPCODE_KEY);

	// Relationships
	public static final String COMPANY_KEY = "company";
	public static final ERXKey COMPANY = new ERXKey(COMPANY_KEY);
	public static final String PAYCHECKS_KEY = "paychecks";
	public static final ERXKey PAYCHECKS = new ERXKey(PAYCHECKS_KEY);
	public static final String ROLES_KEY = "roles";
	public static final ERXKey ROLES = new ERXKey(ROLES_KEY);

  private static Logger LOG = Logger.getLogger(_Employee.class);

  public Employee localInstanceIn(EOEditingContext editingContext) {
    Employee localInstance = (Employee)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String address1() {
    return (String) storedValueForKey("address1");
  }

  public void setAddress1(String value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating address1 from " + address1() + " to " + value);
    }
    takeStoredValueForKey(value, "address1");
  }

  public String address2() {
    return (String) storedValueForKey("address2");
  }

  public void setAddress2(String value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating address2 from " + address2() + " to " + value);
    }
    takeStoredValueForKey(value, "address2");
  }

  public String city() {
    return (String) storedValueForKey("city");
  }

  public void setCity(String value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating city from " + city() + " to " + value);
    }
    takeStoredValueForKey(value, "city");
  }

  public java.lang.Boolean manager() {
    return (java.lang.Boolean) storedValueForKey("manager");
  }

  public void setManager(java.lang.Boolean value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating manager from " + manager() + " to " + value);
    }
    takeStoredValueForKey(value, "manager");
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public String state() {
    return (String) storedValueForKey("state");
  }

  public void setState(String value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating state from " + state() + " to " + value);
    }
    takeStoredValueForKey(value, "state");
  }

  public String zipcode() {
    return (String) storedValueForKey("zipcode");
  }

  public void setZipcode(String value) {
    if (_Employee.LOG.isDebugEnabled()) {
    	_Employee.LOG.debug( "updating zipcode from " + zipcode() + " to " + value);
    }
    takeStoredValueForKey(value, "zipcode");
  }

  public er.erxtest.model.Company company() {
    return (er.erxtest.model.Company)storedValueForKey("company");
  }

  public void setCompanyRelationship(er.erxtest.model.Company value) {
    if (_Employee.LOG.isDebugEnabled()) {
      _Employee.LOG.debug("updating company from " + company() + " to " + value);
    }
    if (value == null) {
    	er.erxtest.model.Company oldValue = company();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "company");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "company");
    }
  }
  
  public NSArray<er.erxtest.model.Paycheck> paychecks() {
    return (NSArray<er.erxtest.model.Paycheck>)storedValueForKey("paychecks");
  }

  public NSArray<er.erxtest.model.Paycheck> paychecks(EOQualifier qualifier) {
    return paychecks(qualifier, null, false);
  }

  public NSArray<er.erxtest.model.Paycheck> paychecks(EOQualifier qualifier, boolean fetch) {
    return paychecks(qualifier, null, fetch);
  }

  public NSArray<er.erxtest.model.Paycheck> paychecks(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.erxtest.model.Paycheck> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.erxtest.model.Paycheck.EMPLOYEE_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.erxtest.model.Paycheck.fetchPaychecks(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = paychecks();
      if (qualifier != null) {
        results = (NSArray<er.erxtest.model.Paycheck>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.erxtest.model.Paycheck>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToPaychecksRelationship(er.erxtest.model.Paycheck object) {
    if (_Employee.LOG.isDebugEnabled()) {
      _Employee.LOG.debug("adding " + object + " to paychecks relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "paychecks");
  }

  public void removeFromPaychecksRelationship(er.erxtest.model.Paycheck object) {
    if (_Employee.LOG.isDebugEnabled()) {
      _Employee.LOG.debug("removing " + object + " from paychecks relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "paychecks");
  }

  public er.erxtest.model.Paycheck createPaychecksRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Paycheck");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "paychecks");
    return (er.erxtest.model.Paycheck) eo;
  }

  public void deletePaychecksRelationship(er.erxtest.model.Paycheck object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "paychecks");
    editingContext().deleteObject(object);
  }

  public void deleteAllPaychecksRelationships() {
    Enumeration objects = paychecks().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePaychecksRelationship((er.erxtest.model.Paycheck)objects.nextElement());
    }
  }

  public NSArray<er.erxtest.model.Role> roles() {
    return (NSArray<er.erxtest.model.Role>)storedValueForKey("roles");
  }

  public NSArray<er.erxtest.model.Role> roles(EOQualifier qualifier) {
    return roles(qualifier, null);
  }

  public NSArray<er.erxtest.model.Role> roles(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.erxtest.model.Role> results;
      results = roles();
      if (qualifier != null) {
        results = (NSArray<er.erxtest.model.Role>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.erxtest.model.Role>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToRolesRelationship(er.erxtest.model.Role object) {
    if (_Employee.LOG.isDebugEnabled()) {
      _Employee.LOG.debug("adding " + object + " to roles relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "roles");
  }

  public void removeFromRolesRelationship(er.erxtest.model.Role object) {
    if (_Employee.LOG.isDebugEnabled()) {
      _Employee.LOG.debug("removing " + object + " from roles relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "roles");
  }

  public er.erxtest.model.Role createRolesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Role");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "roles");
    return (er.erxtest.model.Role) eo;
  }

  public void deleteRolesRelationship(er.erxtest.model.Role object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "roles");
    editingContext().deleteObject(object);
  }

  public void deleteAllRolesRelationships() {
    Enumeration objects = roles().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRolesRelationship((er.erxtest.model.Role)objects.nextElement());
    }
  }


  public static Employee createEmployee(EOEditingContext editingContext, java.lang.Boolean manager
, String name
, er.erxtest.model.Company company) {
    Employee eo = (Employee) EOUtilities.createAndInsertInstance(editingContext, _Employee.ENTITY_NAME);    
		eo.setManager(manager);
		eo.setName(name);
    eo.setCompanyRelationship(company);
    return eo;
  }

  public static NSArray<Employee> fetchAllEmployees(EOEditingContext editingContext) {
    return _Employee.fetchAllEmployees(editingContext, null);
  }

  public static NSArray<Employee> fetchAllEmployees(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Employee.fetchEmployees(editingContext, null, sortOrderings);
  }

  public static NSArray<Employee> fetchEmployees(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Employee.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Employee> eoObjects = (NSArray<Employee>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Employee fetchEmployee(EOEditingContext editingContext, String keyName, Object value) {
    return _Employee.fetchEmployee(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Employee fetchEmployee(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Employee> eoObjects = _Employee.fetchEmployees(editingContext, qualifier, null);
    Employee eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Employee)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Employee that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Employee fetchRequiredEmployee(EOEditingContext editingContext, String keyName, Object value) {
    return _Employee.fetchRequiredEmployee(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Employee fetchRequiredEmployee(EOEditingContext editingContext, EOQualifier qualifier) {
    Employee eoObject = _Employee.fetchEmployee(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Employee that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Employee localInstanceIn(EOEditingContext editingContext, Employee eo) {
    Employee localInstance = (eo == null) ? null : (Employee)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
