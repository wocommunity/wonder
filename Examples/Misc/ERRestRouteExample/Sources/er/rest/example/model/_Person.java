// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Person.java instead.
package er.rest.example.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Person extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "Person";

  // Attribute Keys
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys
  public static final ERXKey<er.rest.example.model.Company> COMPANY = new ERXKey<er.rest.example.model.Company>("company");
  public static final ERXKey<er.rest.example.model.Animal> PETS = new ERXKey<er.rest.example.model.Animal>("pets");

  // Attributes
  public static final String NAME_KEY = NAME.key();
  // Relationships
  public static final String COMPANY_KEY = COMPANY.key();
  public static final String PETS_KEY = PETS.key();

  private static Logger LOG = Logger.getLogger(_Person.class);

  public Person localInstanceIn(EOEditingContext editingContext) {
    Person localInstance = (Person)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_Person.LOG.isDebugEnabled()) {
    	_Person.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public er.rest.example.model.Company company() {
    return (er.rest.example.model.Company)storedValueForKey("company");
  }
  
  public void setCompany(er.rest.example.model.Company value) {
    takeStoredValueForKey(value, "company");
  }

  public void setCompanyRelationship(er.rest.example.model.Company value) {
    if (_Person.LOG.isDebugEnabled()) {
      _Person.LOG.debug("updating company from " + company() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCompany(value);
    }
    else if (value == null) {
    	er.rest.example.model.Company oldValue = company();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "company");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "company");
    }
  }
  
  public NSArray<er.rest.example.model.Animal> pets() {
    return (NSArray<er.rest.example.model.Animal>)storedValueForKey("pets");
  }

  public NSArray<er.rest.example.model.Animal> pets(EOQualifier qualifier) {
    return pets(qualifier, null, false);
  }

  public NSArray<er.rest.example.model.Animal> pets(EOQualifier qualifier, boolean fetch) {
    return pets(qualifier, null, fetch);
  }

  public NSArray<er.rest.example.model.Animal> pets(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.rest.example.model.Animal> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.rest.example.model.Animal.OWNER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.rest.example.model.Animal.fetchAnimals(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = pets();
      if (qualifier != null) {
        results = (NSArray<er.rest.example.model.Animal>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.rest.example.model.Animal>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToPets(er.rest.example.model.Animal object) {
    includeObjectIntoPropertyWithKey(object, "pets");
  }

  public void removeFromPets(er.rest.example.model.Animal object) {
    excludeObjectFromPropertyWithKey(object, "pets");
  }

  public void addToPetsRelationship(er.rest.example.model.Animal object) {
    if (_Person.LOG.isDebugEnabled()) {
      _Person.LOG.debug("adding " + object + " to pets relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPets(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "pets");
    }
  }

  public void removeFromPetsRelationship(er.rest.example.model.Animal object) {
    if (_Person.LOG.isDebugEnabled()) {
      _Person.LOG.debug("removing " + object + " from pets relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPets(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "pets");
    }
  }

  public er.rest.example.model.Animal createPetsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Animal");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "pets");
    return (er.rest.example.model.Animal) eo;
  }

  public void deletePetsRelationship(er.rest.example.model.Animal object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "pets");
    editingContext().deleteObject(object);
  }

  public void deleteAllPetsRelationships() {
    Enumeration objects = pets().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePetsRelationship((er.rest.example.model.Animal)objects.nextElement());
    }
  }


  public static Person createPerson(EOEditingContext editingContext, String name
, er.rest.example.model.Company company) {
    Person eo = (Person) EOUtilities.createAndInsertInstance(editingContext, _Person.ENTITY_NAME);    
		eo.setName(name);
    eo.setCompanyRelationship(company);
    return eo;
  }

  public static NSArray<Person> fetchAllPersons(EOEditingContext editingContext) {
    return _Person.fetchAllPersons(editingContext, null);
  }

  public static NSArray<Person> fetchAllPersons(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Person.fetchPersons(editingContext, null, sortOrderings);
  }

  public static NSArray<Person> fetchPersons(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Person.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Person> eoObjects = (NSArray<Person>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Person fetchPerson(EOEditingContext editingContext, String keyName, Object value) {
    return _Person.fetchPerson(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Person fetchPerson(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Person> eoObjects = _Person.fetchPersons(editingContext, qualifier, null);
    Person eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Person)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Person that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Person fetchRequiredPerson(EOEditingContext editingContext, String keyName, Object value) {
    return _Person.fetchRequiredPerson(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Person fetchRequiredPerson(EOEditingContext editingContext, EOQualifier qualifier) {
    Person eoObject = _Person.fetchPerson(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Person that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Person localInstanceIn(EOEditingContext editingContext, Person eo) {
    Person localInstance = (eo == null) ? null : (Person)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
