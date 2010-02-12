// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Animal.java instead.
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
public abstract class _Animal extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "Animal";

  // Attribute Keys
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys
  public static final ERXKey<er.rest.example.model.Person> OWNER = new ERXKey<er.rest.example.model.Person>("owner");

  // Attributes
  public static final String NAME_KEY = NAME.key();
  // Relationships
  public static final String OWNER_KEY = OWNER.key();

  private static Logger LOG = Logger.getLogger(_Animal.class);

  public Animal localInstanceIn(EOEditingContext editingContext) {
    Animal localInstance = (Animal)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_Animal.LOG.isDebugEnabled()) {
    	_Animal.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public er.rest.example.model.Person owner() {
    return (er.rest.example.model.Person)storedValueForKey("owner");
  }
  
  public void setOwner(er.rest.example.model.Person value) {
    takeStoredValueForKey(value, "owner");
  }

  public void setOwnerRelationship(er.rest.example.model.Person value) {
    if (_Animal.LOG.isDebugEnabled()) {
      _Animal.LOG.debug("updating owner from " + owner() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setOwner(value);
    }
    else if (value == null) {
    	er.rest.example.model.Person oldValue = owner();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "owner");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "owner");
    }
  }
  

  public static Animal createAnimal(EOEditingContext editingContext, String name
, er.rest.example.model.Person owner) {
    Animal eo = (Animal) EOUtilities.createAndInsertInstance(editingContext, _Animal.ENTITY_NAME);    
		eo.setName(name);
    eo.setOwnerRelationship(owner);
    return eo;
  }

  public static NSArray<Animal> fetchAllAnimals(EOEditingContext editingContext) {
    return _Animal.fetchAllAnimals(editingContext, null);
  }

  public static NSArray<Animal> fetchAllAnimals(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Animal.fetchAnimals(editingContext, null, sortOrderings);
  }

  public static NSArray<Animal> fetchAnimals(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Animal.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Animal> eoObjects = (NSArray<Animal>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Animal fetchAnimal(EOEditingContext editingContext, String keyName, Object value) {
    return _Animal.fetchAnimal(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Animal fetchAnimal(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Animal> eoObjects = _Animal.fetchAnimals(editingContext, qualifier, null);
    Animal eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Animal)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Animal that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Animal fetchRequiredAnimal(EOEditingContext editingContext, String keyName, Object value) {
    return _Animal.fetchRequiredAnimal(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Animal fetchRequiredAnimal(EOEditingContext editingContext, EOQualifier qualifier) {
    Animal eoObject = _Animal.fetchAnimal(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Animal that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Animal localInstanceIn(EOEditingContext editingContext, Animal eo) {
    Animal localInstance = (eo == null) ? null : (Animal)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
