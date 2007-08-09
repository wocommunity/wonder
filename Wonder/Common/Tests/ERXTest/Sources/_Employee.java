// _Employee.java
// DO NOT EDIT.  Make changes to Employee.java instead.

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _Employee extends EOGenericRecord {
	public static final String ENTITY_NAME = "Employee";

	public static final String ADDRESS1_KEY = "address1";
	public static final String ADDRESS2_KEY = "address2";
	public static final String CITY_KEY = "city";
	public static final String MANAGER_KEY = "manager";
	public static final String NAME_KEY = "name";
	public static final String STATE_KEY = "state";
	public static final String ZIPCODE_KEY = "zipcode";

	public static final String COMPANY_KEY = "company";

	public static final String PAYCHECKS_KEY = "paychecks";
	private static Logger LOG = Logger.getLogger(_Employee.class);

	public _Employee() {
		super();
	}

	public Employee localInstanceOfEmployee(EOEditingContext editingContext) {
		Employee localInstance = (Employee)EOUtilities.localInstanceOfObject(editingContext, this);
		if (localInstance == null) {
			throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
		}
		return localInstance;
	}


	public String address1() {
		return (String) storedValueForKey("address1");
	}

	public void setAddress1(String aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating address1 from "+address1()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "address1");
	}

	public String address2() {
		return (String) storedValueForKey("address2");
	}

	public void setAddress2(String aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating address2 from "+address2()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "address2");
	}

	public String city() {
		return (String) storedValueForKey("city");
	}

	public void setCity(String aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating city from "+city()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "city");
	}

	public java.lang.Boolean manager() {
		return (java.lang.Boolean) storedValueForKey("manager");
	}

	public void setManager(java.lang.Boolean aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating manager from "+manager()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "manager");
	}

	public String name() {
		return (String) storedValueForKey("name");
	}

	public void setName(String aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating name from "+name()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "name");
	}

	public String state() {
		return (String) storedValueForKey("state");
	}

	public void setState(String aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating state from "+state()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "state");
	}

	public String zipcode() {
		return (String) storedValueForKey("zipcode");
	}

	public void setZipcode(String aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug( "updating zipcode from "+zipcode()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "zipcode");
	}

	public Company company() {
		return (Company)storedValueForKey("company");
	}

	public void setCompanyRelationship(Company aValue) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug("updating company from " + company() + " to " + aValue);
		}
		if (aValue == null) {
			Company object = company();
			if (object != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(object, "company");
			}
		} else {
	    		addObjectToBothSidesOfRelationshipWithKey(aValue, "company");
		}
	}

	public NSArray<Paycheck> paychecks() {
		return (NSArray<Paycheck>)storedValueForKey("paychecks");
	}



	public NSArray<Paycheck> paychecks(EOQualifier qualifier) {
		return paychecks(qualifier, null, false);
	}

	public NSArray<Paycheck> paychecks(EOQualifier qualifier, boolean fetch) { 
		return paychecks(qualifier, null, fetch);
	}


	public NSArray<Paycheck> paychecks(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		NSArray<Paycheck> results;

		if (fetch) {
			EOQualifier fullQualifier;
			EOQualifier inverseQualifier = new EOKeyValueQualifier(Paycheck.EMPLOYEE_KEY, EOQualifier.QualifierOperatorEqual, this);
			if (qualifier == null) {
				fullQualifier = inverseQualifier;
			}
			else {
				NSMutableArray qualifiers = new NSMutableArray();
				qualifiers.addObject(qualifier);
				qualifiers.addObject(inverseQualifier);
				fullQualifier = new EOAndQualifier(qualifiers);
			}
			results = Paycheck.fetchPaychecks(editingContext(), fullQualifier, sortOrderings);
		}
		else {

			results = paychecks();
			if (qualifier != null) {
				results = (NSArray<Paycheck>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<Paycheck>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}

		}

		return results;
	}
    
	public void addToPaychecksRelationship(Paycheck object) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug("adding " + object + " to paychecks relationship");
		}
		addObjectToBothSidesOfRelationshipWithKey( object, "paychecks" );
	}

	public void removeFromPaychecksRelationship(Paycheck object) {
		if (_Employee.LOG.isDebugEnabled()) {
			_Employee.LOG.debug("removing " + object + " from paychecks relationship");
		}
		removeObjectFromBothSidesOfRelationshipWithKey( object, "paychecks" );
	}
    
	public Paycheck createPaychecksRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Paycheck");
		EOEnterpriseObject eoObject = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eoObject);
		addObjectToBothSidesOfRelationshipWithKey(eoObject, "paychecks");
		return (Paycheck) eoObject;
	}
    
	public void deletePaychecksRelationship(Paycheck object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "paychecks");
		editingContext().deleteObject(object);
	}
    
	public void deleteAllPaychecksRelationships() {
		Enumeration objects = paychecks().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deletePaychecksRelationship((Paycheck)objects.nextElement());
		}
	}

	public static Employee createEmployee(EOEditingContext editingContext, java.lang.Boolean manager, String name, Company company) {
		Employee eoObject = (Employee)EOUtilities.createAndInsertInstance(editingContext, _Employee.ENTITY_NAME);
		eoObject.setManager(manager);
		eoObject.setName(name);
		eoObject.setCompanyRelationship(company);
		return eoObject;
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

	public static Employee localInstanceOfEmployee(EOEditingContext editingContext, Employee eo) {
		Employee localInstance = (eo == null) ? null : (Employee)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;		
	}

}
