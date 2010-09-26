// DO NOT EDIT.  Make changes to SPMembership.java instead.
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
public abstract class _SPMembership extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPMembership";

  // Attribute Keys
  public static final ERXKey<Boolean> ADMIN = new ERXKey<Boolean>("admin");
  public static final ERXKey<String> CONFIRMATION_CODE = new ERXKey<String>("confirmationCode");
  public static final ERXKey<Boolean> CONFIRMED = new ERXKey<Boolean>("confirmed");
  // Relationship Keys
  public static final ERXKey<com.secretpal.model.SPGroup> GROUP = new ERXKey<com.secretpal.model.SPGroup>("group");
  public static final ERXKey<com.secretpal.model.SPPerson> PERSON = new ERXKey<com.secretpal.model.SPPerson>("person");

  // Attributes
  public static final String ADMIN_KEY = ADMIN.key();
  public static final String CONFIRMATION_CODE_KEY = CONFIRMATION_CODE.key();
  public static final String CONFIRMED_KEY = CONFIRMED.key();
  // Relationships
  public static final String GROUP_KEY = GROUP.key();
  public static final String PERSON_KEY = PERSON.key();

  private static Logger LOG = Logger.getLogger(_SPMembership.class);

  public SPMembership localInstanceIn(EOEditingContext editingContext) {
    SPMembership localInstance = (SPMembership)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Boolean admin() {
    return (Boolean) storedValueForKey(_SPMembership.ADMIN_KEY);
  }

  public void setAdmin(Boolean value) {
    if (_SPMembership.LOG.isDebugEnabled()) {
    	_SPMembership.LOG.debug( "updating admin from " + admin() + " to " + value);
    }
    takeStoredValueForKey(value, _SPMembership.ADMIN_KEY);
  }

  public String confirmationCode() {
    return (String) storedValueForKey(_SPMembership.CONFIRMATION_CODE_KEY);
  }

  public void setConfirmationCode(String value) {
    if (_SPMembership.LOG.isDebugEnabled()) {
    	_SPMembership.LOG.debug( "updating confirmationCode from " + confirmationCode() + " to " + value);
    }
    takeStoredValueForKey(value, _SPMembership.CONFIRMATION_CODE_KEY);
  }

  public Boolean confirmed() {
    return (Boolean) storedValueForKey(_SPMembership.CONFIRMED_KEY);
  }

  public void setConfirmed(Boolean value) {
    if (_SPMembership.LOG.isDebugEnabled()) {
    	_SPMembership.LOG.debug( "updating confirmed from " + confirmed() + " to " + value);
    }
    takeStoredValueForKey(value, _SPMembership.CONFIRMED_KEY);
  }

  public com.secretpal.model.SPGroup group() {
    return (com.secretpal.model.SPGroup)storedValueForKey(_SPMembership.GROUP_KEY);
  }
  
  public void setGroup(com.secretpal.model.SPGroup value) {
    takeStoredValueForKey(value, _SPMembership.GROUP_KEY);
  }

  public void setGroupRelationship(com.secretpal.model.SPGroup value) {
    if (_SPMembership.LOG.isDebugEnabled()) {
      _SPMembership.LOG.debug("updating group from " + group() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setGroup(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPGroup oldValue = group();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPMembership.GROUP_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPMembership.GROUP_KEY);
    }
  }
  
  public com.secretpal.model.SPPerson person() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPMembership.PERSON_KEY);
  }
  
  public void setPerson(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPMembership.PERSON_KEY);
  }

  public void setPersonRelationship(com.secretpal.model.SPPerson value) {
    if (_SPMembership.LOG.isDebugEnabled()) {
      _SPMembership.LOG.debug("updating person from " + person() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setPerson(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = person();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPMembership.PERSON_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPMembership.PERSON_KEY);
    }
  }
  

  public static SPMembership createSPMembership(EOEditingContext editingContext, Boolean admin
, Boolean confirmed
, com.secretpal.model.SPGroup group, com.secretpal.model.SPPerson person) {
    SPMembership eo = (SPMembership) EOUtilities.createAndInsertInstance(editingContext, _SPMembership.ENTITY_NAME);    
		eo.setAdmin(admin);
		eo.setConfirmed(confirmed);
    eo.setGroupRelationship(group);
    eo.setPersonRelationship(person);
    return eo;
  }

  public static NSArray<SPMembership> fetchAllSPMemberships(EOEditingContext editingContext) {
    return _SPMembership.fetchAllSPMemberships(editingContext, null);
  }

  public static NSArray<SPMembership> fetchAllSPMemberships(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPMembership.fetchSPMemberships(editingContext, null, sortOrderings);
  }

  public static NSArray<SPMembership> fetchSPMemberships(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPMembership> fetchSpec = new ERXFetchSpecification<SPMembership>(_SPMembership.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPMembership> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPMembership fetchSPMembership(EOEditingContext editingContext, String keyName, Object value) {
    return _SPMembership.fetchSPMembership(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPMembership fetchSPMembership(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPMembership> eoObjects = _SPMembership.fetchSPMemberships(editingContext, qualifier, null);
    SPMembership eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPMembership that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPMembership fetchRequiredSPMembership(EOEditingContext editingContext, String keyName, Object value) {
    return _SPMembership.fetchRequiredSPMembership(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPMembership fetchRequiredSPMembership(EOEditingContext editingContext, EOQualifier qualifier) {
    SPMembership eoObject = _SPMembership.fetchSPMembership(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPMembership that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPMembership localInstanceIn(EOEditingContext editingContext, SPMembership eo) {
    SPMembership localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
