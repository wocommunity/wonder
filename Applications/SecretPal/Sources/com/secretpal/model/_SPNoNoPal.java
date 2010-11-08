// DO NOT EDIT.  Make changes to SPNoNoPal.java instead.
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
public abstract class _SPNoNoPal extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPNoNoPal";

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

  private static Logger LOG = Logger.getLogger(_SPNoNoPal.class);

  public SPNoNoPal localInstanceIn(EOEditingContext editingContext) {
    SPNoNoPal localInstance = (SPNoNoPal)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public com.secretpal.model.SPEvent event() {
    return (com.secretpal.model.SPEvent)storedValueForKey(_SPNoNoPal.EVENT_KEY);
  }
  
  public void setEvent(com.secretpal.model.SPEvent value) {
    takeStoredValueForKey(value, _SPNoNoPal.EVENT_KEY);
  }

  public void setEventRelationship(com.secretpal.model.SPEvent value) {
    if (_SPNoNoPal.LOG.isDebugEnabled()) {
      _SPNoNoPal.LOG.debug("updating event from " + event() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setEvent(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPEvent oldValue = event();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPNoNoPal.EVENT_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPNoNoPal.EVENT_KEY);
    }
  }
  
  public com.secretpal.model.SPPerson giver() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPNoNoPal.GIVER_KEY);
  }
  
  public void setGiver(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPNoNoPal.GIVER_KEY);
  }

  public void setGiverRelationship(com.secretpal.model.SPPerson value) {
    if (_SPNoNoPal.LOG.isDebugEnabled()) {
      _SPNoNoPal.LOG.debug("updating giver from " + giver() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setGiver(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = giver();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPNoNoPal.GIVER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPNoNoPal.GIVER_KEY);
    }
  }
  
  public com.secretpal.model.SPPerson receiver() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPNoNoPal.RECEIVER_KEY);
  }
  
  public void setReceiver(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPNoNoPal.RECEIVER_KEY);
  }

  public void setReceiverRelationship(com.secretpal.model.SPPerson value) {
    if (_SPNoNoPal.LOG.isDebugEnabled()) {
      _SPNoNoPal.LOG.debug("updating receiver from " + receiver() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setReceiver(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = receiver();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPNoNoPal.RECEIVER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPNoNoPal.RECEIVER_KEY);
    }
  }
  

  public static SPNoNoPal createSPNoNoPal(EOEditingContext editingContext, com.secretpal.model.SPEvent event, com.secretpal.model.SPPerson giver, com.secretpal.model.SPPerson receiver) {
    SPNoNoPal eo = (SPNoNoPal) EOUtilities.createAndInsertInstance(editingContext, _SPNoNoPal.ENTITY_NAME);    
    eo.setEventRelationship(event);
    eo.setGiverRelationship(giver);
    eo.setReceiverRelationship(receiver);
    return eo;
  }

  public static ERXFetchSpecification<SPNoNoPal> fetchSpec() {
    return new ERXFetchSpecification<SPNoNoPal>(_SPNoNoPal.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<SPNoNoPal> fetchAllSPNoNoPals(EOEditingContext editingContext) {
    return _SPNoNoPal.fetchAllSPNoNoPals(editingContext, null);
  }

  public static NSArray<SPNoNoPal> fetchAllSPNoNoPals(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPNoNoPal.fetchSPNoNoPals(editingContext, null, sortOrderings);
  }

  public static NSArray<SPNoNoPal> fetchSPNoNoPals(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPNoNoPal> fetchSpec = new ERXFetchSpecification<SPNoNoPal>(_SPNoNoPal.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPNoNoPal> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPNoNoPal fetchSPNoNoPal(EOEditingContext editingContext, String keyName, Object value) {
    return _SPNoNoPal.fetchSPNoNoPal(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPNoNoPal fetchSPNoNoPal(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPNoNoPal> eoObjects = _SPNoNoPal.fetchSPNoNoPals(editingContext, qualifier, null);
    SPNoNoPal eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPNoNoPal that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPNoNoPal fetchRequiredSPNoNoPal(EOEditingContext editingContext, String keyName, Object value) {
    return _SPNoNoPal.fetchRequiredSPNoNoPal(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPNoNoPal fetchRequiredSPNoNoPal(EOEditingContext editingContext, EOQualifier qualifier) {
    SPNoNoPal eoObject = _SPNoNoPal.fetchSPNoNoPal(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPNoNoPal that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPNoNoPal localInstanceIn(EOEditingContext editingContext, SPNoNoPal eo) {
    SPNoNoPal localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
