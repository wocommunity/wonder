// DO NOT EDIT.  Make changes to SPGroup.java instead.
package com.secretpal.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _SPGroup extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPGroup";

  // Attribute Keys
  public static final ERXKey<String> DESCRIPTION = new ERXKey<String>("description");
  public static final ERXKey<String> GROUP_PASSWORD = new ERXKey<String>("groupPassword");
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys
  public static final ERXKey<com.secretpal.model.SPEvent> EVENTS = new ERXKey<com.secretpal.model.SPEvent>("events");
  public static final ERXKey<com.secretpal.model.SPMembership> MEMBERSHIPS = new ERXKey<com.secretpal.model.SPMembership>("memberships");
  public static final ERXKey<com.secretpal.model.SPPerson> OWNER = new ERXKey<com.secretpal.model.SPPerson>("owner");

  // Attributes
  public static final String DESCRIPTION_KEY = DESCRIPTION.key();
  public static final String GROUP_PASSWORD_KEY = GROUP_PASSWORD.key();
  public static final String NAME_KEY = NAME.key();
  // Relationships
  public static final String EVENTS_KEY = EVENTS.key();
  public static final String MEMBERSHIPS_KEY = MEMBERSHIPS.key();
  public static final String OWNER_KEY = OWNER.key();

  private static Logger LOG = Logger.getLogger(_SPGroup.class);

  public SPGroup localInstanceIn(EOEditingContext editingContext) {
    SPGroup localInstance = (SPGroup)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String description() {
    return (String) storedValueForKey(_SPGroup.DESCRIPTION_KEY);
  }

  public void setDescription(String value) {
    if (_SPGroup.LOG.isDebugEnabled()) {
    	_SPGroup.LOG.debug( "updating description from " + description() + " to " + value);
    }
    takeStoredValueForKey(value, _SPGroup.DESCRIPTION_KEY);
  }

  public String groupPassword() {
    return (String) storedValueForKey(_SPGroup.GROUP_PASSWORD_KEY);
  }

  public void setGroupPassword(String value) {
    if (_SPGroup.LOG.isDebugEnabled()) {
    	_SPGroup.LOG.debug( "updating groupPassword from " + groupPassword() + " to " + value);
    }
    takeStoredValueForKey(value, _SPGroup.GROUP_PASSWORD_KEY);
  }

  public String name() {
    return (String) storedValueForKey(_SPGroup.NAME_KEY);
  }

  public void setName(String value) {
    if (_SPGroup.LOG.isDebugEnabled()) {
    	_SPGroup.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _SPGroup.NAME_KEY);
  }

  public com.secretpal.model.SPPerson owner() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPGroup.OWNER_KEY);
  }
  
  public void setOwner(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPGroup.OWNER_KEY);
  }

  public void setOwnerRelationship(com.secretpal.model.SPPerson value) {
    if (_SPGroup.LOG.isDebugEnabled()) {
      _SPGroup.LOG.debug("updating owner from " + owner() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setOwner(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = owner();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPGroup.OWNER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPGroup.OWNER_KEY);
    }
  }
  
  public NSArray<com.secretpal.model.SPEvent> events() {
    return (NSArray<com.secretpal.model.SPEvent>)storedValueForKey(_SPGroup.EVENTS_KEY);
  }

  public NSArray<com.secretpal.model.SPEvent> events(EOQualifier qualifier) {
    return events(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPEvent> events(EOQualifier qualifier, boolean fetch) {
    return events(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPEvent> events(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPEvent> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPEvent.GROUP_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPEvent.fetchSPEvents(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = events();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPEvent>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPEvent>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToEvents(com.secretpal.model.SPEvent object) {
    includeObjectIntoPropertyWithKey(object, _SPGroup.EVENTS_KEY);
  }

  public void removeFromEvents(com.secretpal.model.SPEvent object) {
    excludeObjectFromPropertyWithKey(object, _SPGroup.EVENTS_KEY);
  }

  public void addToEventsRelationship(com.secretpal.model.SPEvent object) {
    if (_SPGroup.LOG.isDebugEnabled()) {
      _SPGroup.LOG.debug("adding " + object + " to events relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToEvents(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPGroup.EVENTS_KEY);
    }
  }

  public void removeFromEventsRelationship(com.secretpal.model.SPEvent object) {
    if (_SPGroup.LOG.isDebugEnabled()) {
      _SPGroup.LOG.debug("removing " + object + " from events relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromEvents(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPGroup.EVENTS_KEY);
    }
  }

  public com.secretpal.model.SPEvent createEventsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPEvent.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPGroup.EVENTS_KEY);
    return (com.secretpal.model.SPEvent) eo;
  }

  public void deleteEventsRelationship(com.secretpal.model.SPEvent object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPGroup.EVENTS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllEventsRelationships() {
    Enumeration<com.secretpal.model.SPEvent> objects = events().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteEventsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPMembership> memberships() {
    return (NSArray<com.secretpal.model.SPMembership>)storedValueForKey(_SPGroup.MEMBERSHIPS_KEY);
  }

  public NSArray<com.secretpal.model.SPMembership> memberships(EOQualifier qualifier) {
    return memberships(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPMembership> memberships(EOQualifier qualifier, boolean fetch) {
    return memberships(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPMembership> memberships(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPMembership> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPMembership.GROUP_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPMembership.fetchSPMemberships(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = memberships();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPMembership>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPMembership>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToMemberships(com.secretpal.model.SPMembership object) {
    includeObjectIntoPropertyWithKey(object, _SPGroup.MEMBERSHIPS_KEY);
  }

  public void removeFromMemberships(com.secretpal.model.SPMembership object) {
    excludeObjectFromPropertyWithKey(object, _SPGroup.MEMBERSHIPS_KEY);
  }

  public void addToMembershipsRelationship(com.secretpal.model.SPMembership object) {
    if (_SPGroup.LOG.isDebugEnabled()) {
      _SPGroup.LOG.debug("adding " + object + " to memberships relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToMemberships(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPGroup.MEMBERSHIPS_KEY);
    }
  }

  public void removeFromMembershipsRelationship(com.secretpal.model.SPMembership object) {
    if (_SPGroup.LOG.isDebugEnabled()) {
      _SPGroup.LOG.debug("removing " + object + " from memberships relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromMemberships(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPGroup.MEMBERSHIPS_KEY);
    }
  }

  public com.secretpal.model.SPMembership createMembershipsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPMembership.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPGroup.MEMBERSHIPS_KEY);
    return (com.secretpal.model.SPMembership) eo;
  }

  public void deleteMembershipsRelationship(com.secretpal.model.SPMembership object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPGroup.MEMBERSHIPS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllMembershipsRelationships() {
    Enumeration<com.secretpal.model.SPMembership> objects = memberships().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteMembershipsRelationship(objects.nextElement());
    }
  }


  public static SPGroup createSPGroup(EOEditingContext editingContext, String name
, com.secretpal.model.SPPerson owner) {
    SPGroup eo = (SPGroup) EOUtilities.createAndInsertInstance(editingContext, _SPGroup.ENTITY_NAME);    
		eo.setName(name);
    eo.setOwnerRelationship(owner);
    return eo;
  }

  public static NSArray<SPGroup> fetchAllSPGroups(EOEditingContext editingContext) {
    return _SPGroup.fetchAllSPGroups(editingContext, null);
  }

  public static NSArray<SPGroup> fetchAllSPGroups(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPGroup.fetchSPGroups(editingContext, null, sortOrderings);
  }

  public static NSArray<SPGroup> fetchSPGroups(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPGroup> fetchSpec = new ERXFetchSpecification<SPGroup>(_SPGroup.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPGroup> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPGroup fetchSPGroup(EOEditingContext editingContext, String keyName, Object value) {
    return _SPGroup.fetchSPGroup(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPGroup fetchSPGroup(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPGroup> eoObjects = _SPGroup.fetchSPGroups(editingContext, qualifier, null);
    SPGroup eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPGroup that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPGroup fetchRequiredSPGroup(EOEditingContext editingContext, String keyName, Object value) {
    return _SPGroup.fetchRequiredSPGroup(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPGroup fetchRequiredSPGroup(EOEditingContext editingContext, EOQualifier qualifier) {
    SPGroup eoObject = _SPGroup.fetchSPGroup(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPGroup that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPGroup localInstanceIn(EOEditingContext editingContext, SPGroup eo) {
    SPGroup localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
