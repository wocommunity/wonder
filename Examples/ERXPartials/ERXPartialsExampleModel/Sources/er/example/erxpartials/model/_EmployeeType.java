// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to EmployeeType.java instead.
/** Partial template to fix relationships */
package er.example.erxpartials.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _EmployeeType extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "EmployeeType";

  // Attribute Keys
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys
  public static final ERXKey<er.example.erxpartials.model.Person> PARTIAL__EMPLOYEE_PERSONS = new ERXKey<er.example.erxpartials.model.Person>("partial_EmployeePersons");

  // Attributes
  public static final String NAME_KEY = NAME.key();
  // Relationships
  public static final String PARTIAL__EMPLOYEE_PERSONS_KEY = PARTIAL__EMPLOYEE_PERSONS.key();


  private static Logger LOG = Logger.getLogger(_EmployeeType.class);

  public EmployeeType localInstanceIn(EOEditingContext editingContext) {
    EmployeeType localInstance = (EmployeeType)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_EmployeeType.LOG.isDebugEnabled()) {
    	_EmployeeType.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons() {
    return (NSArray<er.example.erxpartials.model.Person>)storedValueForKey("partial_EmployeePersons");
  }

  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier) {
    return partial_EmployeePersons(qualifier, null, false);
  }

  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier, boolean fetch) {
    return partial_EmployeePersons(qualifier, null, fetch);
  }

  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.example.erxpartials.model.Person> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.example.erxpartials.model.Partial_EmployeePerson.EMPLOYEE_TYPE_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.example.erxpartials.model.Person.fetchPersons(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = partial_EmployeePersons();
      if (qualifier != null) {
        results = (NSArray<er.example.erxpartials.model.Person>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.example.erxpartials.model.Person>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToPartial_EmployeePersons(er.example.erxpartials.model.Person object) {
    includeObjectIntoPropertyWithKey(object, "partial_EmployeePersons");
  }

  public void removeFromPartial_EmployeePersons(er.example.erxpartials.model.Person object) {
    excludeObjectFromPropertyWithKey(object, "partial_EmployeePersons");
  }

  public void addToPartial_EmployeePersonsRelationship(er.example.erxpartials.model.Person object) {
    if (_EmployeeType.LOG.isDebugEnabled()) {
      _EmployeeType.LOG.debug("adding " + object + " to partial_EmployeePersons relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPartial_EmployeePersons(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "partial_EmployeePersons");
    }
  }

  public void removeFromPartial_EmployeePersonsRelationship(er.example.erxpartials.model.Person object) {
    if (_EmployeeType.LOG.isDebugEnabled()) {
      _EmployeeType.LOG.debug("removing " + object + " from partial_EmployeePersons relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPartial_EmployeePersons(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "partial_EmployeePersons");
    }
  }

  public er.example.erxpartials.model.Person createPartial_EmployeePersonsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Person");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "partial_EmployeePersons");
    return (er.example.erxpartials.model.Person) eo;
  }

  public void deletePartial_EmployeePersonsRelationship(er.example.erxpartials.model.Person object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "partial_EmployeePersons");
    editingContext().deleteObject(object);
  }

  public void deleteAllPartial_EmployeePersonsRelationships() {
    Enumeration objects = partial_EmployeePersons().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePartial_EmployeePersonsRelationship((er.example.erxpartials.model.Person)objects.nextElement());
    }
  }


  public static EmployeeType createEmployeeType(EOEditingContext editingContext, String name
) {
    EmployeeType eo = (EmployeeType) EOUtilities.createAndInsertInstance(editingContext, _EmployeeType.ENTITY_NAME);    
		eo.setName(name);
    return eo;
  }

  public static NSArray<EmployeeType> fetchAllEmployeeTypes(EOEditingContext editingContext) {
    return _EmployeeType.fetchAllEmployeeTypes(editingContext, null);
  }

  public static NSArray<EmployeeType> fetchAllEmployeeTypes(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _EmployeeType.fetchEmployeeTypes(editingContext, null, sortOrderings);
  }

  public static NSArray<EmployeeType> fetchEmployeeTypes(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_EmployeeType.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<EmployeeType> eoObjects = (NSArray<EmployeeType>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static EmployeeType fetchEmployeeType(EOEditingContext editingContext, String keyName, Object value) {
    return _EmployeeType.fetchEmployeeType(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static EmployeeType fetchEmployeeType(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<EmployeeType> eoObjects = _EmployeeType.fetchEmployeeTypes(editingContext, qualifier, null);
    EmployeeType eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (EmployeeType)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one EmployeeType that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static EmployeeType fetchRequiredEmployeeType(EOEditingContext editingContext, String keyName, Object value) {
    return _EmployeeType.fetchRequiredEmployeeType(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static EmployeeType fetchRequiredEmployeeType(EOEditingContext editingContext, EOQualifier qualifier) {
    EmployeeType eoObject = _EmployeeType.fetchEmployeeType(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no EmployeeType that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static EmployeeType localInstanceIn(EOEditingContext editingContext, EmployeeType eo) {
    EmployeeType localInstance = (eo == null) ? null : (EmployeeType)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
