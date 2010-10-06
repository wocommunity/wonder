// DO NOT EDIT.  Make changes to SPSecretPal.java instead.
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
public abstract class _SPSecretPal extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPSecretPal";

  // Attribute Keys
  // Relationship Keys
  public static final ERXKey<com.secretpal.model.SPEvent> EVENT = new ERXKey<com.secretpal.model.SPEvent>("event");
  public static final ERXKey<com.secretpal.model.SPPerson> GIVER = new ERXKey<com.secretpal.model.SPPerson>("giver");
  public static final ERXKey<com.secretpal.model.SPPerson> RECEIVER = new ERXKey<com.secretpal.model.SPPerson>("receiver");

  // Attributes
  // Relationships
  public static final String EVENT_KEY = EVENT.key();
  public static final String GIVER_KEY = GIVER.key();
  public static final String RECEIVER_KEY = RECEIVER.key();

  private static Logger LOG = Logger.getLogger(_SPSecretPal.class);

  public SPSecretPal localInstanceIn(EOEditingContext editingContext) {
    SPSecretPal localInstance = (SPSecretPal)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public com.secretpal.model.SPEvent event() {
    return (com.secretpal.model.SPEvent)storedValueForKey(_SPSecretPal.EVENT_KEY);
  }
  
  public void setEvent(com.secretpal.model.SPEvent value) {
    takeStoredValueForKey(value, _SPSecretPal.EVENT_KEY);
  }

  public void setEventRelationship(com.secretpal.model.SPEvent value) {
    if (_SPSecretPal.LOG.isDebugEnabled()) {
      _SPSecretPal.LOG.debug("updating event from " + event() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setEvent(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPEvent oldValue = event();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPSecretPal.EVENT_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPSecretPal.EVENT_KEY);
    }
  }
  
  public com.secretpal.model.SPPerson giver() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPSecretPal.GIVER_KEY);
  }
  
  public void setGiver(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPSecretPal.GIVER_KEY);
  }

  public void setGiverRelationship(com.secretpal.model.SPPerson value) {
    if (_SPSecretPal.LOG.isDebugEnabled()) {
      _SPSecretPal.LOG.debug("updating giver from " + giver() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setGiver(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = giver();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPSecretPal.GIVER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPSecretPal.GIVER_KEY);
    }
  }
  
  public com.secretpal.model.SPPerson receiver() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPSecretPal.RECEIVER_KEY);
  }
  
  public void setReceiver(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPSecretPal.RECEIVER_KEY);
  }

  public void setReceiverRelationship(com.secretpal.model.SPPerson value) {
    if (_SPSecretPal.LOG.isDebugEnabled()) {
      _SPSecretPal.LOG.debug("updating receiver from " + receiver() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setReceiver(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = receiver();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPSecretPal.RECEIVER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPSecretPal.RECEIVER_KEY);
    }
  }
  

  public static SPSecretPal createSPSecretPal(EOEditingContext editingContext, com.secretpal.model.SPEvent event, com.secretpal.model.SPPerson giver, com.secretpal.model.SPPerson receiver) {
    SPSecretPal eo = (SPSecretPal) EOUtilities.createAndInsertInstance(editingContext, _SPSecretPal.ENTITY_NAME);    
    eo.setEventRelationship(event);
    eo.setGiverRelationship(giver);
    eo.setReceiverRelationship(receiver);
    return eo;
  }

  public static ERXFetchSpecification<SPSecretPal> fetchSpec() {
    return new ERXFetchSpecification<SPSecretPal>(_SPSecretPal.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<SPSecretPal> fetchAllSPSecretPals(EOEditingContext editingContext) {
    return _SPSecretPal.fetchAllSPSecretPals(editingContext, null);
  }

  public static NSArray<SPSecretPal> fetchAllSPSecretPals(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPSecretPal.fetchSPSecretPals(editingContext, null, sortOrderings);
  }

  public static NSArray<SPSecretPal> fetchSPSecretPals(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPSecretPal> fetchSpec = new ERXFetchSpecification<SPSecretPal>(_SPSecretPal.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPSecretPal> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPSecretPal fetchSPSecretPal(EOEditingContext editingContext, String keyName, Object value) {
    return _SPSecretPal.fetchSPSecretPal(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPSecretPal fetchSPSecretPal(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPSecretPal> eoObjects = _SPSecretPal.fetchSPSecretPals(editingContext, qualifier, null);
    SPSecretPal eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPSecretPal that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPSecretPal fetchRequiredSPSecretPal(EOEditingContext editingContext, String keyName, Object value) {
    return _SPSecretPal.fetchRequiredSPSecretPal(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPSecretPal fetchRequiredSPSecretPal(EOEditingContext editingContext, EOQualifier qualifier) {
    SPSecretPal eoObject = _SPSecretPal.fetchSPSecretPal(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPSecretPal that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPSecretPal localInstanceIn(EOEditingContext editingContext, SPSecretPal eo) {
    SPSecretPal localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
