// DO NOT EDIT.  Make changes to SPEvent.java instead.
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
public abstract class _SPEvent extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPEvent";

  // Attribute Keys
  public static final ERXKey<Boolean> ACTIVE = new ERXKey<Boolean>("active");
  public static final ERXKey<String> DESCRIPTION = new ERXKey<String>("description");
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys
  public static final ERXKey<com.secretpal.model.SPGroup> GROUP = new ERXKey<com.secretpal.model.SPGroup>("group");
  public static final ERXKey<com.secretpal.model.SPSecretPal> SECRET_PALS = new ERXKey<com.secretpal.model.SPSecretPal>("secretPals");

  // Attributes
  public static final String ACTIVE_KEY = ACTIVE.key();
  public static final String DESCRIPTION_KEY = DESCRIPTION.key();
  public static final String NAME_KEY = NAME.key();
  // Relationships
  public static final String GROUP_KEY = GROUP.key();
  public static final String SECRET_PALS_KEY = SECRET_PALS.key();

  private static Logger LOG = Logger.getLogger(_SPEvent.class);

  public SPEvent localInstanceIn(EOEditingContext editingContext) {
    SPEvent localInstance = (SPEvent)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Boolean active() {
    return (Boolean) storedValueForKey(_SPEvent.ACTIVE_KEY);
  }

  public void setActive(Boolean value) {
    if (_SPEvent.LOG.isDebugEnabled()) {
    	_SPEvent.LOG.debug( "updating active from " + active() + " to " + value);
    }
    takeStoredValueForKey(value, _SPEvent.ACTIVE_KEY);
  }

  public String description() {
    return (String) storedValueForKey(_SPEvent.DESCRIPTION_KEY);
  }

  public void setDescription(String value) {
    if (_SPEvent.LOG.isDebugEnabled()) {
    	_SPEvent.LOG.debug( "updating description from " + description() + " to " + value);
    }
    takeStoredValueForKey(value, _SPEvent.DESCRIPTION_KEY);
  }

  public String name() {
    return (String) storedValueForKey(_SPEvent.NAME_KEY);
  }

  public void setName(String value) {
    if (_SPEvent.LOG.isDebugEnabled()) {
    	_SPEvent.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _SPEvent.NAME_KEY);
  }

  public com.secretpal.model.SPGroup group() {
    return (com.secretpal.model.SPGroup)storedValueForKey(_SPEvent.GROUP_KEY);
  }
  
  public void setGroup(com.secretpal.model.SPGroup value) {
    takeStoredValueForKey(value, _SPEvent.GROUP_KEY);
  }

  public void setGroupRelationship(com.secretpal.model.SPGroup value) {
    if (_SPEvent.LOG.isDebugEnabled()) {
      _SPEvent.LOG.debug("updating group from " + group() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setGroup(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPGroup oldValue = group();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPEvent.GROUP_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPEvent.GROUP_KEY);
    }
  }
  
  public NSArray<com.secretpal.model.SPSecretPal> secretPals() {
    return (NSArray<com.secretpal.model.SPSecretPal>)storedValueForKey(_SPEvent.SECRET_PALS_KEY);
  }

  public NSArray<com.secretpal.model.SPSecretPal> secretPals(EOQualifier qualifier) {
    return secretPals(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPSecretPal> secretPals(EOQualifier qualifier, boolean fetch) {
    return secretPals(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPSecretPal> secretPals(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPSecretPal> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPSecretPal.EVENT_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPSecretPal.fetchSPSecretPals(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = secretPals();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPSecretPal>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPSecretPal>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToSecretPals(com.secretpal.model.SPSecretPal object) {
    includeObjectIntoPropertyWithKey(object, _SPEvent.SECRET_PALS_KEY);
  }

  public void removeFromSecretPals(com.secretpal.model.SPSecretPal object) {
    excludeObjectFromPropertyWithKey(object, _SPEvent.SECRET_PALS_KEY);
  }

  public void addToSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    if (_SPEvent.LOG.isDebugEnabled()) {
      _SPEvent.LOG.debug("adding " + object + " to secretPals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToSecretPals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPEvent.SECRET_PALS_KEY);
    }
  }

  public void removeFromSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    if (_SPEvent.LOG.isDebugEnabled()) {
      _SPEvent.LOG.debug("removing " + object + " from secretPals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromSecretPals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPEvent.SECRET_PALS_KEY);
    }
  }

  public com.secretpal.model.SPSecretPal createSecretPalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPSecretPal.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPEvent.SECRET_PALS_KEY);
    return (com.secretpal.model.SPSecretPal) eo;
  }

  public void deleteSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPEvent.SECRET_PALS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllSecretPalsRelationships() {
    Enumeration<com.secretpal.model.SPSecretPal> objects = secretPals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteSecretPalsRelationship(objects.nextElement());
    }
  }


  public static SPEvent createSPEvent(EOEditingContext editingContext, Boolean active
, String name
, com.secretpal.model.SPGroup group) {
    SPEvent eo = (SPEvent) EOUtilities.createAndInsertInstance(editingContext, _SPEvent.ENTITY_NAME);    
		eo.setActive(active);
		eo.setName(name);
    eo.setGroupRelationship(group);
    return eo;
  }

  public static NSArray<SPEvent> fetchAllSPEvents(EOEditingContext editingContext) {
    return _SPEvent.fetchAllSPEvents(editingContext, null);
  }

  public static NSArray<SPEvent> fetchAllSPEvents(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPEvent.fetchSPEvents(editingContext, null, sortOrderings);
  }

  public static NSArray<SPEvent> fetchSPEvents(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPEvent> fetchSpec = new ERXFetchSpecification<SPEvent>(_SPEvent.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPEvent> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPEvent fetchSPEvent(EOEditingContext editingContext, String keyName, Object value) {
    return _SPEvent.fetchSPEvent(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPEvent fetchSPEvent(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPEvent> eoObjects = _SPEvent.fetchSPEvents(editingContext, qualifier, null);
    SPEvent eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPEvent that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPEvent fetchRequiredSPEvent(EOEditingContext editingContext, String keyName, Object value) {
    return _SPEvent.fetchRequiredSPEvent(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPEvent fetchRequiredSPEvent(EOEditingContext editingContext, EOQualifier qualifier) {
    SPEvent eoObject = _SPEvent.fetchSPEvent(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPEvent that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPEvent localInstanceIn(EOEditingContext editingContext, SPEvent eo) {
    SPEvent localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
