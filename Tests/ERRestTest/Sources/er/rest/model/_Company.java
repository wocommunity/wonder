// $LastChangedRevision: 5773 $ DO NOT EDIT.  Make changes to Company.java instead.
package er.rest.model;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

@SuppressWarnings("all")
public abstract class _Company extends  EOGenericRecord {
	public static final String ENTITY_NAME = "Company";

	// Attributes
	public static final String NAME_KEY = "name";
	public static final String REVENUE_KEY = "revenue";

	// Relationships
	public static final String EMPLOYEES_KEY = "employees";

  private static Logger LOG = Logger.getLogger(_Company.class);

  public Company localInstanceIn(EOEditingContext editingContext) {
    Company localInstance = (Company)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_Company.LOG.isDebugEnabled()) {
    	_Company.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public BigDecimal revenue() {
    return (BigDecimal) storedValueForKey("revenue");
  }

  public void setRevenue(Double value) {
    if (_Company.LOG.isDebugEnabled()) {
    	_Company.LOG.debug( "updating revenue from " + revenue() + " to " + value);
    }
    takeStoredValueForKey(value, "revenue");
  }

  public NSArray<er.rest.model.Person> employees() {
    return (NSArray<er.rest.model.Person>)storedValueForKey("employees");
  }

  public NSArray<er.rest.model.Person> employees(EOQualifier qualifier) {
    return employees(qualifier, null, false);
  }

  public NSArray<er.rest.model.Person> employees(EOQualifier qualifier, boolean fetch) {
    return employees(qualifier, null, fetch);
  }

  public NSArray<er.rest.model.Person> employees(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.rest.model.Person> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.rest.model.Person.COMPANY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.rest.model.Person.fetchPersons(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = employees();
      if (qualifier != null) {
        results = (NSArray<er.rest.model.Person>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.rest.model.Person>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToEmployeesRelationship(er.rest.model.Person object) {
    if (_Company.LOG.isDebugEnabled()) {
      _Company.LOG.debug("adding " + object + " to employees relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "employees");
  }

  public void removeFromEmployeesRelationship(er.rest.model.Person object) {
    if (_Company.LOG.isDebugEnabled()) {
      _Company.LOG.debug("removing " + object + " from employees relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "employees");
  }

  public er.rest.model.Person createEmployeesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Person");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "employees");
    return (er.rest.model.Person) eo;
  }

  public void deleteEmployeesRelationship(er.rest.model.Person object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "employees");
    editingContext().deleteObject(object);
  }

  public void deleteAllEmployeesRelationships() {
    Enumeration objects = employees().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteEmployeesRelationship((er.rest.model.Person)objects.nextElement());
    }
  }


  public static Company createCompany(EOEditingContext editingContext, String name
) {
    Company eo = (Company) EOUtilities.createAndInsertInstance(editingContext, _Company.ENTITY_NAME);    
		eo.setName(name);
    return eo;
  }

  public static NSArray<Company> fetchAllCompanies(EOEditingContext editingContext) {
    return _Company.fetchAllCompanies(editingContext, null);
  }

  public static NSArray<Company> fetchAllCompanies(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Company.fetchCompanies(editingContext, null, sortOrderings);
  }

  public static NSArray<Company> fetchCompanies(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Company.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Company> eoObjects = (NSArray<Company>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Company fetchCompany(EOEditingContext editingContext, String keyName, Object value) {
    return _Company.fetchCompany(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Company fetchCompany(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Company> eoObjects = _Company.fetchCompanies(editingContext, qualifier, null);
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

  public static Company localInstanceIn(EOEditingContext editingContext, Company eo) {
    Company localInstance = (eo == null) ? null : (Company)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
