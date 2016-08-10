// $LastChangedRevision: 5773 $ DO NOT EDIT.  Make changes to FirstEntity.java instead.
package er.neo4jadaptor.test.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _FirstEntity extends  EOGenericRecord {
	public static final String ENTITY_NAME = "FirstEntity";

	// Attributes
	public static final String BOOL_KEY = "bool";
	public static final String FLOAT_NUMBER_KEY = "floatNumber";
	public static final String NUMBER_KEY = "number";
	public static final String SECOND_ENTITY_ID_KEY = "secondEntityId";
	public static final String TEXT_KEY = "text";
	public static final String TIMESTAMP_KEY = "timestamp";

	// Relationships
	public static final String JOINS_KEY = "joins";
	public static final String JOINS_SECOND_ENTITY_KEY = "joins_secondEntity";
	public static final String JOINS_SECOND_ENTITY_FIRST_ENTITIES_KEY = "joins_secondEntity_firstEntities";
	public static final String SECOND_ENTITY_KEY = "secondEntity";
	public static final String THIRD_ENTITIES_KEY = "thirdEntities";

  private static Logger LOG = Logger.getLogger(_FirstEntity.class);

  public FirstEntity localInstanceIn(EOEditingContext editingContext) {
    FirstEntity localInstance = (FirstEntity)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Boolean bool() {
    return (Boolean) storedValueForKey("bool");
  }

  public void setBool(Boolean value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
    	_FirstEntity.LOG.debug( "updating bool from " + bool() + " to " + value);
    }
    takeStoredValueForKey(value, "bool");
  }

  public Float floatNumber() {
    return (Float) storedValueForKey("floatNumber");
  }

  public void setFloatNumber(Float value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
    	_FirstEntity.LOG.debug( "updating floatNumber from " + floatNumber() + " to " + value);
    }
    takeStoredValueForKey(value, "floatNumber");
  }

  public Integer number() {
    return (Integer) storedValueForKey("number");
  }

  public void setNumber(Integer value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
    	_FirstEntity.LOG.debug( "updating number from " + number() + " to " + value);
    }
    takeStoredValueForKey(value, "number");
  }

  public Integer secondEntityId() {
    return (Integer) storedValueForKey("secondEntityId");
  }

  public void setSecondEntityId(Integer value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
    	_FirstEntity.LOG.debug( "updating secondEntityId from " + secondEntityId() + " to " + value);
    }
    takeStoredValueForKey(value, "secondEntityId");
  }

  public String text() {
    return (String) storedValueForKey("text");
  }

  public void setText(String value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
    	_FirstEntity.LOG.debug( "updating text from " + text() + " to " + value);
    }
    takeStoredValueForKey(value, "text");
  }

  public NSTimestamp timestamp() {
    return (NSTimestamp) storedValueForKey("timestamp");
  }

  public void setTimestamp(NSTimestamp value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
    	_FirstEntity.LOG.debug( "updating timestamp from " + timestamp() + " to " + value);
    }
    takeStoredValueForKey(value, "timestamp");
  }

  public er.neo4jadaptor.test.eo.SecondEntity secondEntity() {
    return (er.neo4jadaptor.test.eo.SecondEntity)storedValueForKey("secondEntity");
  }

  public void setSecondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity value) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("updating secondEntity from " + secondEntity() + " to " + value);
    }
    if (value == null) {
    	er.neo4jadaptor.test.eo.SecondEntity oldValue = secondEntity();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "secondEntity");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "secondEntity");
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
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.neo4jadaptor.test.eo.Join.FIRST_ENTITY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("adding " + object + " to joins relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "joins");
  }

  public void removeFromJoinsRelationship(er.neo4jadaptor.test.eo.Join object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("removing " + object + " from joins relationship");
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

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> joins_secondEntity() {
    return (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)storedValueForKey("joins_secondEntity");
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> joins_secondEntity(EOQualifier qualifier) {
    return joins_secondEntity(qualifier, null);
  }

  public NSArray<er.neo4jadaptor.test.eo.SecondEntity> joins_secondEntity(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.neo4jadaptor.test.eo.SecondEntity> results;
      results = joins_secondEntity();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.SecondEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToJoins_secondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("adding " + object + " to joins_secondEntity relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "joins_secondEntity");
  }

  public void removeFromJoins_secondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("removing " + object + " from joins_secondEntity relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins_secondEntity");
  }

  public er.neo4jadaptor.test.eo.SecondEntity createJoins_secondEntityRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("SecondEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "joins_secondEntity");
    return (er.neo4jadaptor.test.eo.SecondEntity) eo;
  }

  public void deleteJoins_secondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins_secondEntity");
    editingContext().deleteObject(object);
  }

  public void deleteAllJoins_secondEntityRelationships() {
    Enumeration objects = joins_secondEntity().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteJoins_secondEntityRelationship((er.neo4jadaptor.test.eo.SecondEntity)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> joins_secondEntity_firstEntities() {
    return (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)storedValueForKey("joins_secondEntity_firstEntities");
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> joins_secondEntity_firstEntities(EOQualifier qualifier) {
    return joins_secondEntity_firstEntities(qualifier, null);
  }

  public NSArray<er.neo4jadaptor.test.eo.FirstEntity> joins_secondEntity_firstEntities(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.neo4jadaptor.test.eo.FirstEntity> results;
      results = joins_secondEntity_firstEntities();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.FirstEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToJoins_secondEntity_firstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("adding " + object + " to joins_secondEntity_firstEntities relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "joins_secondEntity_firstEntities");
  }

  public void removeFromJoins_secondEntity_firstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("removing " + object + " from joins_secondEntity_firstEntities relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins_secondEntity_firstEntities");
  }

  public er.neo4jadaptor.test.eo.FirstEntity createJoins_secondEntity_firstEntitiesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("FirstEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "joins_secondEntity_firstEntities");
    return (er.neo4jadaptor.test.eo.FirstEntity) eo;
  }

  public void deleteJoins_secondEntity_firstEntitiesRelationship(er.neo4jadaptor.test.eo.FirstEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "joins_secondEntity_firstEntities");
    editingContext().deleteObject(object);
  }

  public void deleteAllJoins_secondEntity_firstEntitiesRelationships() {
    Enumeration objects = joins_secondEntity_firstEntities().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteJoins_secondEntity_firstEntitiesRelationship((er.neo4jadaptor.test.eo.FirstEntity)objects.nextElement());
    }
  }

  public NSArray<er.neo4jadaptor.test.eo.ThirdEntity> thirdEntities() {
    return (NSArray<er.neo4jadaptor.test.eo.ThirdEntity>)storedValueForKey("thirdEntities");
  }

  public NSArray<er.neo4jadaptor.test.eo.ThirdEntity> thirdEntities(EOQualifier qualifier) {
    return thirdEntities(qualifier, null, false);
  }

  public NSArray<er.neo4jadaptor.test.eo.ThirdEntity> thirdEntities(EOQualifier qualifier, boolean fetch) {
    return thirdEntities(qualifier, null, fetch);
  }

  public NSArray<er.neo4jadaptor.test.eo.ThirdEntity> thirdEntities(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.neo4jadaptor.test.eo.ThirdEntity> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.neo4jadaptor.test.eo.ThirdEntity.FIRST_ENTITY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.neo4jadaptor.test.eo.ThirdEntity.fetchThirdEntities(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = thirdEntities();
      if (qualifier != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.ThirdEntity>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.neo4jadaptor.test.eo.ThirdEntity>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToThirdEntitiesRelationship(er.neo4jadaptor.test.eo.ThirdEntity object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("adding " + object + " to thirdEntities relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "thirdEntities");
  }

  public void removeFromThirdEntitiesRelationship(er.neo4jadaptor.test.eo.ThirdEntity object) {
    if (_FirstEntity.LOG.isDebugEnabled()) {
      _FirstEntity.LOG.debug("removing " + object + " from thirdEntities relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "thirdEntities");
  }

  public er.neo4jadaptor.test.eo.ThirdEntity createThirdEntitiesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("ThirdEntity");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "thirdEntities");
    return (er.neo4jadaptor.test.eo.ThirdEntity) eo;
  }

  public void deleteThirdEntitiesRelationship(er.neo4jadaptor.test.eo.ThirdEntity object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "thirdEntities");
    editingContext().deleteObject(object);
  }

  public void deleteAllThirdEntitiesRelationships() {
    Enumeration objects = thirdEntities().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteThirdEntitiesRelationship((er.neo4jadaptor.test.eo.ThirdEntity)objects.nextElement());
    }
  }


  public static FirstEntity createFirstEntity(EOEditingContext editingContext) {
    FirstEntity eo = (FirstEntity) EOUtilities.createAndInsertInstance(editingContext, _FirstEntity.ENTITY_NAME);    
    return eo;
  }

  public static NSArray<FirstEntity> fetchAllFirstEntities(EOEditingContext editingContext) {
    return _FirstEntity.fetchAllFirstEntities(editingContext, null);
  }

  public static NSArray<FirstEntity> fetchAllFirstEntities(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _FirstEntity.fetchFirstEntities(editingContext, null, sortOrderings);
  }

  public static NSArray<FirstEntity> fetchFirstEntities(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_FirstEntity.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<FirstEntity> eoObjects = (NSArray<FirstEntity>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static FirstEntity fetchFirstEntity(EOEditingContext editingContext, String keyName, Object value) {
    return _FirstEntity.fetchFirstEntity(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static FirstEntity fetchFirstEntity(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<FirstEntity> eoObjects = _FirstEntity.fetchFirstEntities(editingContext, qualifier, null);
    FirstEntity eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (FirstEntity)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one FirstEntity that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static FirstEntity fetchRequiredFirstEntity(EOEditingContext editingContext, String keyName, Object value) {
    return _FirstEntity.fetchRequiredFirstEntity(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static FirstEntity fetchRequiredFirstEntity(EOEditingContext editingContext, EOQualifier qualifier) {
    FirstEntity eoObject = _FirstEntity.fetchFirstEntity(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no FirstEntity that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static FirstEntity localInstanceIn(EOEditingContext editingContext, FirstEntity eo) {
    FirstEntity localInstance = (eo == null) ? null : (FirstEntity)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
