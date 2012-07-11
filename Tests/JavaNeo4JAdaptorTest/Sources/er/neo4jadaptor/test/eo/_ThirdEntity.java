// $LastChangedRevision: 5773 $ DO NOT EDIT.  Make changes to ThirdEntity.java instead.
package er.neo4jadaptor.test.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _ThirdEntity extends  EOGenericRecord {
	public static final String ENTITY_NAME = "ThirdEntity";

	// Attributes

	// Relationships
	public static final String FIRST_ENTITY_KEY = "firstEntity";
	public static final String FIRST_ENTITY_JOINS_KEY = "firstEntity_joins";
	public static final String FIRST_ENTITY_JOINS_SECOND_ENTITY_KEY = "firstEntity_joins_secondEntity";
	public static final String FIRST_ENTITY_JOINS_SECOND_ENTITY_FIRST_ENTITIES_KEY = "firstEntity_joins_secondEntity_firstEntities";
	public static final String SECOND_ENTITIES_KEY = "secondEntities";

  private static Logger LOG = Logger.getLogger(_ThirdEntity.class);

  public ThirdEntity localInstanceIn(EOEditingContext editingContext) {
    ThirdEntity localInstance = (ThirdEntity)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public er.neo4jadaptor.test.eo.FirstEntity firstEntity() {
    return (er.neo4jadaptor.test.eo.FirstEntity)storedValueForKey("firstEntity");
  }

  public void setFirstEntityRelationship(er.neo4jadaptor.test.eo.FirstEntity value) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("updating firstEntity from " + firstEntity() + " to " + value);
    }
    if (value == null) {
    	er.neo4jadaptor.test.eo.FirstEntity oldValue = firstEntity();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "firstEntity");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "firstEntity");
    }
  }
  
  public NSArray<er.neo4jadaptor.test.eo.Join> firstEntity_joins() {
    return (NSArray<er.neo4jadaptor.test.eo.Join>)storedValueForKey("firstEntity_joins");
  }

  public NSArray<er.neo4jadaptor.test.eo.Join> firstEntity_joins(EOQualifier qualifier) {
    return firstEntity_joins(qualifier, null);
  }

  public NSArray<er.neo4jadaptor.test.eo.Join> firstEntity_joins(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.neo4jadaptor.test.eo.Join> results;
      results = firstEntity_joins();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.Join>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.Join>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToFirstEntity_joinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("adding " + object + " to firstEntity_joins relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "firstEntity_joins");
  }

  public void removeFromFirstEntity_joinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("removing " + object + " from firstEntity_joins relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntity_joins");
  }

  public er.neo4jadaptor.test.eo.Join createFirstEntity_joinsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Join");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "firstEntity_joins");
    return (er.neo4jadaptor.test.eo.Join) eo;
  }

  public void deleteFirstEntity_joinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntity_joins");
    editingContext().deleteObject(object);
  }

  public void deleteAllFirstEntity_joinsRelationships() {
    Enumeration objects = firstEntity_joins().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteFirstEntity_joinsRelationship((er.neo4jadaptor.test.eo.Join)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> firstEntity_joins_secondEntity() {
    return (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)storedValueForKey("firstEntity_joins_secondEntity");
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> firstEntity_joins_secondEntity(EOQualifier qualifier) {
    return firstEntity_joins_secondEntity(qualifier, null);
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> firstEntity_joins_secondEntity(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.neo4jadaptor.test.eo.SecondEntity> results;
      results = firstEntity_joins_secondEntity();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToFirstEntity_joins_secondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("adding " + object + " to firstEntity_joins_secondEntity relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "firstEntity_joins_secondEntity");
  }

  public void removeFromFirstEntity_joins_secondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("removing " + object + " from firstEntity_joins_secondEntity relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntity_joins_secondEntity");
  }

  public er.neo4jadaptor.test.eo.SecondEntity createFirstEntity_joins_secondEntityRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("SecondEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "firstEntity_joins_secondEntity");
    return (er.neo4jadaptor.test.eo.SecondEntity) eo;
  }

  public void deleteFirstEntity_joins_secondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntity_joins_secondEntity");
    editingContext().deleteObject(object);
  }

  public void deleteAllFirstEntity_joins_secondEntityRelationships() {
    Enumeration objects = firstEntity_joins_secondEntity().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteFirstEntity_joins_secondEntityRelationship((er.neo4jadaptor.test.eo.SecondEntity)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntity_joins_secondEntity_firstEntities() {
    return (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)storedValueForKey("firstEntity_joins_secondEntity_firstEntities");
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntity_joins_secondEntity_firstEntities(EOQualifier qualifier) {
    return firstEntity_joins_secondEntity_firstEntities(qualifier, null);
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> firstEntity_joins_secondEntity_firstEntities(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.neo4jadaptor.test.eo.FirstEntity> results;
      results = firstEntity_joins_secondEntity_firstEntities();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToFirstEntity_joins_secondEntity_firstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("adding " + object + " to firstEntity_joins_secondEntity_firstEntities relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "firstEntity_joins_secondEntity_firstEntities");
  }

  public void removeFromFirstEntity_joins_secondEntity_firstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("removing " + object + " from firstEntity_joins_secondEntity_firstEntities relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntity_joins_secondEntity_firstEntities");
  }

  public er.neo4jadaptor.test.eo.FirstEntity createFirstEntity_joins_secondEntity_firstEntitiesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("FirstEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "firstEntity_joins_secondEntity_firstEntities");
    return (er.neo4jadaptor.test.eo.FirstEntity) eo;
  }

  public void deleteFirstEntity_joins_secondEntity_firstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "firstEntity_joins_secondEntity_firstEntities");
    editingContext().deleteObject(object);
  }

  public void deleteAllFirstEntity_joins_secondEntity_firstEntitiesRelationships() {
    Enumeration objects = firstEntity_joins_secondEntity_firstEntities().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteFirstEntity_joins_secondEntity_firstEntitiesRelationship((er.neo4jadaptor.test.eo.FirstEntity)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> secondEntities() {
    return (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)storedValueForKey("secondEntities");
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> secondEntities(EOQualifier qualifier) {
    return secondEntities(qualifier, null, false);
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> secondEntities(EOQualifier qualifier, boolean fetch) {
    return secondEntities(qualifier, null, fetch);
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> secondEntities(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.neo4jadaptor.test.eo.SecondEntity> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.neo4jadaptor.test.eo.SecondEntity.THIRD_ENTITY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.neo4jadaptor.test.eo.SecondEntity.fetchSecondEntities(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = secondEntities();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToSecondEntitiesRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("adding " + object + " to secondEntities relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "secondEntities");
  }

  public void removeFromSecondEntitiesRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    if (_ThirdEntity.LOG.isDebugEnabled()) {
      _ThirdEntity.LOG.debug("removing " + object + " from secondEntities relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "secondEntities");
  }

  public er.neo4jadaptor.test.eo.SecondEntity createSecondEntitiesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("SecondEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "secondEntities");
    return (er.neo4jadaptor.test.eo.SecondEntity) eo;
  }

  public void deleteSecondEntitiesRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "secondEntities");
    editingContext().deleteObject(object);
  }

  public void deleteAllSecondEntitiesRelationships() {
    Enumeration objects = secondEntities().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteSecondEntitiesRelationship((er.neo4jadaptor.test.eo.SecondEntity)objects.nextElement());
    }
  }


  public static ThirdEntity createThirdEntity(EOEditingContext editingContext) {
    ThirdEntity eo = (ThirdEntity) EOUtilities.createAndInsertInstance(editingContext, _ThirdEntity.ENTITY_NAME);    
    return eo;
  }

  public static NSArray<ThirdEntity> fetchAllThirdEntities(EOEditingContext editingContext) {
    return _ThirdEntity.fetchAllThirdEntities(editingContext, null);
  }

  public static NSArray<ThirdEntity> fetchAllThirdEntities(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ThirdEntity.fetchThirdEntities(editingContext, null, sortOrderings);
  }

  public static NSArray<ThirdEntity> fetchThirdEntities(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_ThirdEntity.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ThirdEntity> eoObjects = (NSArray<ThirdEntity>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static ThirdEntity fetchThirdEntity(EOEditingContext editingContext, String keyName, Object value) {
    return _ThirdEntity.fetchThirdEntity(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ThirdEntity fetchThirdEntity(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ThirdEntity> eoObjects = _ThirdEntity.fetchThirdEntities(editingContext, qualifier, null);
    ThirdEntity eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (ThirdEntity)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ThirdEntity that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ThirdEntity fetchRequiredThirdEntity(EOEditingContext editingContext, String keyName, Object value) {
    return _ThirdEntity.fetchRequiredThirdEntity(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ThirdEntity fetchRequiredThirdEntity(EOEditingContext editingContext, EOQualifier qualifier) {
    ThirdEntity eoObject = _ThirdEntity.fetchThirdEntity(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ThirdEntity that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ThirdEntity localInstanceIn(EOEditingContext editingContext, ThirdEntity eo) {
    ThirdEntity localInstance = (eo == null) ? null : (ThirdEntity)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
