// $LastChangedRevision: 5773 $ DO NOT EDIT.  Make changes to SecondEntity.java instead.
package er.neo4jadaptor.test.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _SecondEntity extends  EOGenericRecord {
	public static final String ENTITY_NAME = "SecondEntity";

	// Attributes
	public static final String NUMBER_KEY = "number";

	// Relationships
	public static final String FIRST_ENTITIES_KEY = "firstEntities";
	public static final String JOINS_KEY = "joins";
	public static final String JOINS_FIRST_ENTITY_KEY = "joins_firstEntity";
	public static final String THIRD_ENTITY_KEY = "thirdEntity";

  private static Logger LOG = Logger.getLogger(_SecondEntity.class);

  public SecondEntity localInstanceIn(EOEditingContext editingContext) {
    SecondEntity localInstance = (SecondEntity)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Integer number() {
    return (Integer) storedValueForKey("number");
  }

  public void setNumber(Integer value) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
    	_SecondEntity.LOG.debug( "updating number from " + number() + " to " + value);
    }
    takeStoredValueForKey(value, "number");
  }

  public er.neo4jadaptor.test.eo.ThirdEntity thirdEntity() {
    return (er.neo4jadaptor.test.eo.ThirdEntity)storedValueForKey("thirdEntity");
  }

  public void setThirdEntityRelationship(er.neo4jadaptor.test.eo.ThirdEntity value) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("updating thirdEntity from " + thirdEntity() + " to " + value);
    }
    if (value == null) {
    	er.neo4jadaptor.test.eo.ThirdEntity oldValue = thirdEntity();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "thirdEntity");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "thirdEntity");
    }
  }
  
  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntities() {
    return (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)storedValueForKey("firstEntities");
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntities(EOQualifier qualifier) {
    return firstEntities(qualifier, null, false);
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntities(EOQualifier qualifier, boolean fetch) {
    return firstEntities(qualifier, null, fetch);
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntities(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.neo4jadaptor.test.eo.FirstEntity> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.neo4jadaptor.test.eo.FirstEntity.SECOND_ENTITY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.neo4jadaptor.test.eo.FirstEntity.fetchFirstEntities(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = firstEntities();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToFirstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("adding " + object + " to firstEntities relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "firstEntities");
  }

  public void removeFromFirstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("removing " + object + " from firstEntities relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntities");
  }

  public er.neo4jadaptor.test.eo.FirstEntity createFirstEntitiesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("FirstEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "firstEntities");
    return (er.neo4jadaptor.test.eo.FirstEntity) eo;
  }

  public void deleteFirstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntities");
    editingContext().deleteObject(object);
  }

  public void deleteAllFirstEntitiesRelationships() {
    Enumeration objects = firstEntities().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteFirstEntitiesRelationship((er.neo4jadaptor.test.eo.FirstEntity)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.Join> joins() {
    return (NSArray<er.neo4jadaptor.test.eo.Join>)storedValueForKey("joins");
  }

  public NSArray<er.neo4jadaptor.test.eo.Join> joins(EOQualifier qualifier) {
    return joins(qualifier, null, false);
  }

  public NSArray<er.neo4jadaptor.test.eo.Join> joins(EOQualifier qualifier, boolean fetch) {
    return joins(qualifier, null, fetch);
  }

  public NSArray<er.neo4jadaptor.test.eo.Join> joins(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.neo4jadaptor.test.eo.Join> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.neo4jadaptor.test.eo.Join.SECOND_ENTITY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.neo4jadaptor.test.eo.Join.fetchJoins(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = joins();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.Join>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.Join>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToJoinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("adding " + object + " to joins relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "joins");
  }

  public void removeFromJoinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("removing " + object + " from joins relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins");
  }

  public er.neo4jadaptor.test.eo.Join createJoinsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Join");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "joins");
    return (er.neo4jadaptor.test.eo.Join) eo;
  }

  public void deleteJoinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins");
    editingContext().deleteObject(object);
  }

  public void deleteAllJoinsRelationships() {
    Enumeration objects = joins().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteJoinsRelationship((er.neo4jadaptor.test.eo.Join)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> joins_firstEntity() {
    return (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)storedValueForKey("joins_firstEntity");
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> joins_firstEntity(EOQualifier qualifier) {
    return joins_firstEntity(qualifier, null);
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> joins_firstEntity(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.neo4jadaptor.test.eo.FirstEntity> results;
      results = joins_firstEntity();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToJoins_firstEntityRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("adding " + object + " to joins_firstEntity relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "joins_firstEntity");
  }

  public void removeFromJoins_firstEntityRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_SecondEntity.LOG.isDebugEnabled()) {
      _SecondEntity.LOG.debug("removing " + object + " from joins_firstEntity relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins_firstEntity");
  }

  public er.neo4jadaptor.test.eo.FirstEntity createJoins_firstEntityRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("FirstEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "joins_firstEntity");
    return (er.neo4jadaptor.test.eo.FirstEntity) eo;
  }

  public void deleteJoins_firstEntityRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins_firstEntity");
    editingContext().deleteObject(object);
  }

  public void deleteAllJoins_firstEntityRelationships() {
    Enumeration objects = joins_firstEntity().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteJoins_firstEntityRelationship((er.neo4jadaptor.test.eo.FirstEntity)objects.nextElement());
    }
  }


  public static SecondEntity createSecondEntity(EOEditingContext editingContext) {
    SecondEntity eo = (SecondEntity) EOUtilities.createAndInsertInstance(editingContext, _SecondEntity.ENTITY_NAME);    
    return eo;
  }

  public static NSArray<SecondEntity> fetchAllSecondEntities(EOEditingContext editingContext) {
    return _SecondEntity.fetchAllSecondEntities(editingContext, null);
  }

  public static NSArray<SecondEntity> fetchAllSecondEntities(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SecondEntity.fetchSecondEntities(editingContext, null, sortOrderings);
  }

  public static NSArray<SecondEntity> fetchSecondEntities(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_SecondEntity.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SecondEntity> eoObjects = (NSArray<SecondEntity>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static SecondEntity fetchSecondEntity(EOEditingContext editingContext, String keyName, Object value) {
    return _SecondEntity.fetchSecondEntity(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SecondEntity fetchSecondEntity(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SecondEntity> eoObjects = _SecondEntity.fetchSecondEntities(editingContext, qualifier, null);
    SecondEntity eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (SecondEntity)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SecondEntity that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SecondEntity fetchRequiredSecondEntity(EOEditingContext editingContext, String keyName, Object value) {
    return _SecondEntity.fetchRequiredSecondEntity(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SecondEntity fetchRequiredSecondEntity(EOEditingContext editingContext, EOQualifier qualifier) {
    SecondEntity eoObject = _SecondEntity.fetchSecondEntity(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SecondEntity that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SecondEntity localInstanceIn(EOEditingContext editingContext, SecondEntity eo) {
    SecondEntity localInstance = (eo == null) ? null : (SecondEntity)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
