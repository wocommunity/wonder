// _Company.java
// DO NOT EDIT.  Make changes to Company.java instead.

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _Company extends EOGenericRecord {
	public static final String ENTITY_NAME = "Company";

	public static final String ADDRESS1_KEY = "address1";
	public static final String ADDRESS2_KEY = "address2";
	public static final String CITY_KEY = "city";
	public static final String NAME_KEY = "name";
	public static final String STATE_KEY = "state";
	public static final String ZIPCODE_KEY = "zipcode";


	public static final String EMPLOYEES_KEY = "employees";
	private static Logger LOG = Logger.getLogger(_Company.class);

	public _Company() {
		super();
	}

	public Company localInstanceOfCompany(EOEditingContext editingContext) {
		Company localInstance = (Company)EOUtilities.localInstanceOfObject(editingContext, this);
		if (localInstance == null) {
			throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
		}
		return localInstance;
	}


	public String address1() {
		return (String) storedValueForKey("address1");
	}

	public void setAddress1(String aValue) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug( "updating address1 from "+address1()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "address1");
	}

	public String address2() {
		return (String) storedValueForKey("address2");
	}

	public void setAddress2(String aValue) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug( "updating address2 from "+address2()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "address2");
	}

	public String city() {
		return (String) storedValueForKey("city");
	}

	public void setCity(String aValue) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug( "updating city from "+city()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "city");
	}

	public String name() {
		return (String) storedValueForKey("name");
	}

	public void setName(String aValue) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug( "updating name from "+name()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "name");
	}

	public String state() {
		return (String) storedValueForKey("state");
	}

	public void setState(String aValue) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug( "updating state from "+state()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "state");
	}

	public String zipcode() {
		return (String) storedValueForKey("zipcode");
	}

	public void setZipcode(String aValue) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug( "updating zipcode from "+zipcode()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "zipcode");
	}

	public NSArray<Employee> employees() {
		return (NSArray<Employee>)storedValueForKey("employees");
	}



	public NSArray<Employee> employees(EOQualifier qualifier) {
		return employees(qualifier, null, false);
	}

	public NSArray<Employee> employees(EOQualifier qualifier, boolean fetch) { 
		return employees(qualifier, null, fetch);
	}


	public NSArray<Employee> employees(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		NSArray<Employee> results;

		if (fetch) {
			EOQualifier fullQualifier;
			EOQualifier inverseQualifier = new EOKeyValueQualifier(Employee.COMPANY_KEY, EOQualifier.QualifierOperatorEqual, this);
			if (qualifier == null) {
				fullQualifier = inverseQualifier;
			}
			else {
				NSMutableArray qualifiers = new NSMutableArray();
				qualifiers.addObject(qualifier);
				qualifiers.addObject(inverseQualifier);
				fullQualifier = new EOAndQualifier(qualifiers);
			}
			results = Employee.fetchEmployees(editingContext(), fullQualifier, sortOrderings);
		}
		else {

			results = employees();
			if (qualifier != null) {
				results = (NSArray<Employee>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<Employee>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}

		}

		return results;
	}
    
	public void addToEmployeesRelationship(Employee object) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug("adding " + object + " to employees relationship");
		}
		addObjectToBothSidesOfRelationshipWithKey( object, "employees" );
	}

	public void removeFromEmployeesRelationship(Employee object) {
		if (_Company.LOG.isDebugEnabled()) {
			_Company.LOG.debug("removing " + object + " from employees relationship");
		}
		removeObjectFromBothSidesOfRelationshipWithKey( object, "employees" );
	}
    
	public Employee createEmployeesRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Employee");
		EOEnterpriseObject eoObject = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eoObject);
		addObjectToBothSidesOfRelationshipWithKey(eoObject, "employees");
		return (Employee) eoObject;
	}
    
	public void deleteEmployeesRelationship(Employee object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "employees");
		editingContext().deleteObject(object);
	}
    
	public void deleteAllEmployeesRelationships() {
		Enumeration objects = employees().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deleteEmployeesRelationship((Employee)objects.nextElement());
		}
	}

	public static Company createCompany(EOEditingContext editingContext, String name) {
		Company eoObject = (Company)EOUtilities.createAndInsertInstance(editingContext, _Company.ENTITY_NAME);
		eoObject.setName(name);
		return eoObject;
	}

	public static NSArray<Company> fetchAllCompanys(EOEditingContext editingContext) {
		return _Company.fetchAllCompanys(editingContext, null);
	}

	public static NSArray<Company> fetchAllCompanys(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _Company.fetchCompanys(editingContext, null, sortOrderings);
	}

	public static NSArray<Company> fetchCompanys(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Company.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<Company> eoObjects = (NSArray<Company>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	public static Company fetchCompany(EOEditingContext editingContext, String keyName, Object value) {
		return _Company.fetchCompany(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Company fetchCompany(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<Company> eoObjects = _Company.fetchCompanys(editingContext, qualifier, null);
		Company eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		}
		else if (count == 1) {
			eoObject = (Company)eoObjects.objectAtIndex(0);
		}
		else {
			throw new IllegalStateException("There was more than one Company that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}
	
	public static Company fetchRequiredCompany(EOEditingContext editingContext, String keyName, Object value) {
		return _Company.fetchRequiredCompany(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Company fetchRequiredCompany(EOEditingContext editingContext, EOQualifier qualifier) {
		Company eoObject = _Company.fetchCompany(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no Company that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Company localInstanceOfCompany(EOEditingContext editingContext, Company eo) {
		Company localInstance = (eo == null) ? null : (Company)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;		
	}

}
