// DO NOT EDIT.  Make changes to SPPerson.java instead.
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
public abstract class _SPPerson extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPPerson";

  // Attribute Keys
  public static final ERXKey<Boolean> ADMIN = new ERXKey<Boolean>("admin");
  public static final ERXKey<String> EMAIL_ADDRESS = new ERXKey<String>("emailAddress");
  public static final ERXKey<Boolean> EMAIL_DELIVERY_FAILURE = new ERXKey<Boolean>("emailDeliveryFailure");
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  public static final ERXKey<String> PASSWORD = new ERXKey<String>("password");
  // Relationship Keys
  public static final ERXKey<er.attachment.model.ERAttachment> AVATAR = new ERXKey<er.attachment.model.ERAttachment>("avatar");
  public static final ERXKey<com.secretpal.model.SPNoNoPal> CANNOT_GIVE_PALS = new ERXKey<com.secretpal.model.SPNoNoPal>("cannotGivePals");
  public static final ERXKey<com.secretpal.model.SPNoNoPal> CANNOT_RECEIVE_PALS = new ERXKey<com.secretpal.model.SPNoNoPal>("cannotReceivePals");
  public static final ERXKey<com.secretpal.model.SPSecretPal> GIVER_SECRET_PALS = new ERXKey<com.secretpal.model.SPSecretPal>("giverSecretPals");
  public static final ERXKey<com.secretpal.model.SPMembership> MEMBERSHIPS = new ERXKey<com.secretpal.model.SPMembership>("memberships");
  public static final ERXKey<com.secretpal.model.SPGroup> OWNED_GROUPS = new ERXKey<com.secretpal.model.SPGroup>("ownedGroups");
  public static final ERXKey<com.secretpal.model.SPSecretPal> RECEIVER_SECRET_PALS = new ERXKey<com.secretpal.model.SPSecretPal>("receiverSecretPals");
  public static final ERXKey<com.secretpal.model.SPWish> SUGGESTIONS = new ERXKey<com.secretpal.model.SPWish>("suggestions");
  public static final ERXKey<com.secretpal.model.SPWish> WISHES = new ERXKey<com.secretpal.model.SPWish>("wishes");

  // Attributes
  public static final String ADMIN_KEY = ADMIN.key();
  public static final String EMAIL_ADDRESS_KEY = EMAIL_ADDRESS.key();
  public static final String EMAIL_DELIVERY_FAILURE_KEY = EMAIL_DELIVERY_FAILURE.key();
  public static final String NAME_KEY = NAME.key();
  public static final String PASSWORD_KEY = PASSWORD.key();
  // Relationships
  public static final String AVATAR_KEY = AVATAR.key();
  public static final String CANNOT_GIVE_PALS_KEY = CANNOT_GIVE_PALS.key();
  public static final String CANNOT_RECEIVE_PALS_KEY = CANNOT_RECEIVE_PALS.key();
  public static final String GIVER_SECRET_PALS_KEY = GIVER_SECRET_PALS.key();
  public static final String MEMBERSHIPS_KEY = MEMBERSHIPS.key();
  public static final String OWNED_GROUPS_KEY = OWNED_GROUPS.key();
  public static final String RECEIVER_SECRET_PALS_KEY = RECEIVER_SECRET_PALS.key();
  public static final String SUGGESTIONS_KEY = SUGGESTIONS.key();
  public static final String WISHES_KEY = WISHES.key();

  private static Logger LOG = Logger.getLogger(_SPPerson.class);

  public SPPerson localInstanceIn(EOEditingContext editingContext) {
    SPPerson localInstance = (SPPerson)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Boolean admin() {
    return (Boolean) storedValueForKey(_SPPerson.ADMIN_KEY);
  }

  public void setAdmin(Boolean value) {
    if (_SPPerson.LOG.isDebugEnabled()) {
    	_SPPerson.LOG.debug( "updating admin from " + admin() + " to " + value);
    }
    takeStoredValueForKey(value, _SPPerson.ADMIN_KEY);
  }

  public String emailAddress() {
    return (String) storedValueForKey(_SPPerson.EMAIL_ADDRESS_KEY);
  }

  public void setEmailAddress(String value) {
    if (_SPPerson.LOG.isDebugEnabled()) {
    	_SPPerson.LOG.debug( "updating emailAddress from " + emailAddress() + " to " + value);
    }
    takeStoredValueForKey(value, _SPPerson.EMAIL_ADDRESS_KEY);
  }

  public Boolean emailDeliveryFailure() {
    return (Boolean) storedValueForKey(_SPPerson.EMAIL_DELIVERY_FAILURE_KEY);
  }

  public void setEmailDeliveryFailure(Boolean value) {
    if (_SPPerson.LOG.isDebugEnabled()) {
    	_SPPerson.LOG.debug( "updating emailDeliveryFailure from " + emailDeliveryFailure() + " to " + value);
    }
    takeStoredValueForKey(value, _SPPerson.EMAIL_DELIVERY_FAILURE_KEY);
  }

  public String name() {
    return (String) storedValueForKey(_SPPerson.NAME_KEY);
  }

  public void setName(String value) {
    if (_SPPerson.LOG.isDebugEnabled()) {
    	_SPPerson.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _SPPerson.NAME_KEY);
  }

  public String password() {
    return (String) storedValueForKey(_SPPerson.PASSWORD_KEY);
  }

  public void setPassword(String value) {
    if (_SPPerson.LOG.isDebugEnabled()) {
    	_SPPerson.LOG.debug( "updating password from " + password() + " to " + value);
    }
    takeStoredValueForKey(value, _SPPerson.PASSWORD_KEY);
  }

  public er.attachment.model.ERAttachment avatar() {
    return (er.attachment.model.ERAttachment)storedValueForKey(_SPPerson.AVATAR_KEY);
  }
  
  public void setAvatar(er.attachment.model.ERAttachment value) {
    takeStoredValueForKey(value, _SPPerson.AVATAR_KEY);
  }

  public void setAvatarRelationship(er.attachment.model.ERAttachment value) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("updating avatar from " + avatar() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setAvatar(value);
    }
    else if (value == null) {
    	er.attachment.model.ERAttachment oldValue = avatar();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPPerson.AVATAR_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPPerson.AVATAR_KEY);
    }
  }
  
  public NSArray<com.secretpal.model.SPNoNoPal> cannotGivePals() {
    return (NSArray<com.secretpal.model.SPNoNoPal>)storedValueForKey(_SPPerson.CANNOT_GIVE_PALS_KEY);
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotGivePals(EOQualifier qualifier) {
    return cannotGivePals(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotGivePals(EOQualifier qualifier, boolean fetch) {
    return cannotGivePals(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotGivePals(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPNoNoPal> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPNoNoPal.GIVER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPNoNoPal.fetchSPNoNoPals(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = cannotGivePals();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPNoNoPal>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPNoNoPal>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToCannotGivePals(com.secretpal.model.SPNoNoPal object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.CANNOT_GIVE_PALS_KEY);
  }

  public void removeFromCannotGivePals(com.secretpal.model.SPNoNoPal object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.CANNOT_GIVE_PALS_KEY);
  }

  public void addToCannotGivePalsRelationship(com.secretpal.model.SPNoNoPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to cannotGivePals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToCannotGivePals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.CANNOT_GIVE_PALS_KEY);
    }
  }

  public void removeFromCannotGivePalsRelationship(com.secretpal.model.SPNoNoPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from cannotGivePals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromCannotGivePals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.CANNOT_GIVE_PALS_KEY);
    }
  }

  public com.secretpal.model.SPNoNoPal createCannotGivePalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPNoNoPal.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.CANNOT_GIVE_PALS_KEY);
    return (com.secretpal.model.SPNoNoPal) eo;
  }

  public void deleteCannotGivePalsRelationship(com.secretpal.model.SPNoNoPal object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.CANNOT_GIVE_PALS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllCannotGivePalsRelationships() {
    Enumeration<com.secretpal.model.SPNoNoPal> objects = cannotGivePals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteCannotGivePalsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotReceivePals() {
    return (NSArray<com.secretpal.model.SPNoNoPal>)storedValueForKey(_SPPerson.CANNOT_RECEIVE_PALS_KEY);
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotReceivePals(EOQualifier qualifier) {
    return cannotReceivePals(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotReceivePals(EOQualifier qualifier, boolean fetch) {
    return cannotReceivePals(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPNoNoPal> cannotReceivePals(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPNoNoPal> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPNoNoPal.RECEIVER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPNoNoPal.fetchSPNoNoPals(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = cannotReceivePals();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPNoNoPal>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPNoNoPal>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToCannotReceivePals(com.secretpal.model.SPNoNoPal object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.CANNOT_RECEIVE_PALS_KEY);
  }

  public void removeFromCannotReceivePals(com.secretpal.model.SPNoNoPal object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.CANNOT_RECEIVE_PALS_KEY);
  }

  public void addToCannotReceivePalsRelationship(com.secretpal.model.SPNoNoPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to cannotReceivePals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToCannotReceivePals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.CANNOT_RECEIVE_PALS_KEY);
    }
  }

  public void removeFromCannotReceivePalsRelationship(com.secretpal.model.SPNoNoPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from cannotReceivePals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromCannotReceivePals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.CANNOT_RECEIVE_PALS_KEY);
    }
  }

  public com.secretpal.model.SPNoNoPal createCannotReceivePalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPNoNoPal.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.CANNOT_RECEIVE_PALS_KEY);
    return (com.secretpal.model.SPNoNoPal) eo;
  }

  public void deleteCannotReceivePalsRelationship(com.secretpal.model.SPNoNoPal object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.CANNOT_RECEIVE_PALS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllCannotReceivePalsRelationships() {
    Enumeration<com.secretpal.model.SPNoNoPal> objects = cannotReceivePals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteCannotReceivePalsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPSecretPal> giverSecretPals() {
    return (NSArray<com.secretpal.model.SPSecretPal>)storedValueForKey(_SPPerson.GIVER_SECRET_PALS_KEY);
  }

  public NSArray<com.secretpal.model.SPSecretPal> giverSecretPals(EOQualifier qualifier) {
    return giverSecretPals(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPSecretPal> giverSecretPals(EOQualifier qualifier, boolean fetch) {
    return giverSecretPals(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPSecretPal> giverSecretPals(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPSecretPal> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPSecretPal.GIVER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
      results = giverSecretPals();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPSecretPal>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPSecretPal>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToGiverSecretPals(com.secretpal.model.SPSecretPal object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.GIVER_SECRET_PALS_KEY);
  }

  public void removeFromGiverSecretPals(com.secretpal.model.SPSecretPal object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.GIVER_SECRET_PALS_KEY);
  }

  public void addToGiverSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to giverSecretPals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToGiverSecretPals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.GIVER_SECRET_PALS_KEY);
    }
  }

  public void removeFromGiverSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from giverSecretPals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromGiverSecretPals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.GIVER_SECRET_PALS_KEY);
    }
  }

  public com.secretpal.model.SPSecretPal createGiverSecretPalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPSecretPal.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.GIVER_SECRET_PALS_KEY);
    return (com.secretpal.model.SPSecretPal) eo;
  }

  public void deleteGiverSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.GIVER_SECRET_PALS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllGiverSecretPalsRelationships() {
    Enumeration<com.secretpal.model.SPSecretPal> objects = giverSecretPals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteGiverSecretPalsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPMembership> memberships() {
    return (NSArray<com.secretpal.model.SPMembership>)storedValueForKey(_SPPerson.MEMBERSHIPS_KEY);
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
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPMembership.PERSON_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
    includeObjectIntoPropertyWithKey(object, _SPPerson.MEMBERSHIPS_KEY);
  }

  public void removeFromMemberships(com.secretpal.model.SPMembership object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.MEMBERSHIPS_KEY);
  }

  public void addToMembershipsRelationship(com.secretpal.model.SPMembership object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to memberships relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToMemberships(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.MEMBERSHIPS_KEY);
    }
  }

  public void removeFromMembershipsRelationship(com.secretpal.model.SPMembership object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from memberships relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromMemberships(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.MEMBERSHIPS_KEY);
    }
  }

  public com.secretpal.model.SPMembership createMembershipsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPMembership.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.MEMBERSHIPS_KEY);
    return (com.secretpal.model.SPMembership) eo;
  }

  public void deleteMembershipsRelationship(com.secretpal.model.SPMembership object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.MEMBERSHIPS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllMembershipsRelationships() {
    Enumeration<com.secretpal.model.SPMembership> objects = memberships().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteMembershipsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPGroup> ownedGroups() {
    return (NSArray<com.secretpal.model.SPGroup>)storedValueForKey(_SPPerson.OWNED_GROUPS_KEY);
  }

  public NSArray<com.secretpal.model.SPGroup> ownedGroups(EOQualifier qualifier) {
    return ownedGroups(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPGroup> ownedGroups(EOQualifier qualifier, boolean fetch) {
    return ownedGroups(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPGroup> ownedGroups(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPGroup> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPGroup.OWNER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPGroup.fetchSPGroups(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = ownedGroups();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPGroup>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPGroup>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToOwnedGroups(com.secretpal.model.SPGroup object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.OWNED_GROUPS_KEY);
  }

  public void removeFromOwnedGroups(com.secretpal.model.SPGroup object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.OWNED_GROUPS_KEY);
  }

  public void addToOwnedGroupsRelationship(com.secretpal.model.SPGroup object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to ownedGroups relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToOwnedGroups(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.OWNED_GROUPS_KEY);
    }
  }

  public void removeFromOwnedGroupsRelationship(com.secretpal.model.SPGroup object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from ownedGroups relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromOwnedGroups(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.OWNED_GROUPS_KEY);
    }
  }

  public com.secretpal.model.SPGroup createOwnedGroupsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPGroup.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.OWNED_GROUPS_KEY);
    return (com.secretpal.model.SPGroup) eo;
  }

  public void deleteOwnedGroupsRelationship(com.secretpal.model.SPGroup object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.OWNED_GROUPS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllOwnedGroupsRelationships() {
    Enumeration<com.secretpal.model.SPGroup> objects = ownedGroups().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteOwnedGroupsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPSecretPal> receiverSecretPals() {
    return (NSArray<com.secretpal.model.SPSecretPal>)storedValueForKey(_SPPerson.RECEIVER_SECRET_PALS_KEY);
  }

  public NSArray<com.secretpal.model.SPSecretPal> receiverSecretPals(EOQualifier qualifier) {
    return receiverSecretPals(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPSecretPal> receiverSecretPals(EOQualifier qualifier, boolean fetch) {
    return receiverSecretPals(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPSecretPal> receiverSecretPals(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPSecretPal> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPSecretPal.RECEIVER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
      results = receiverSecretPals();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPSecretPal>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPSecretPal>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToReceiverSecretPals(com.secretpal.model.SPSecretPal object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.RECEIVER_SECRET_PALS_KEY);
  }

  public void removeFromReceiverSecretPals(com.secretpal.model.SPSecretPal object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.RECEIVER_SECRET_PALS_KEY);
  }

  public void addToReceiverSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to receiverSecretPals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToReceiverSecretPals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.RECEIVER_SECRET_PALS_KEY);
    }
  }

  public void removeFromReceiverSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from receiverSecretPals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromReceiverSecretPals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.RECEIVER_SECRET_PALS_KEY);
    }
  }

  public com.secretpal.model.SPSecretPal createReceiverSecretPalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPSecretPal.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.RECEIVER_SECRET_PALS_KEY);
    return (com.secretpal.model.SPSecretPal) eo;
  }

  public void deleteReceiverSecretPalsRelationship(com.secretpal.model.SPSecretPal object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.RECEIVER_SECRET_PALS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllReceiverSecretPalsRelationships() {
    Enumeration<com.secretpal.model.SPSecretPal> objects = receiverSecretPals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteReceiverSecretPalsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPWish> suggestions() {
    return (NSArray<com.secretpal.model.SPWish>)storedValueForKey(_SPPerson.SUGGESTIONS_KEY);
  }

  public NSArray<com.secretpal.model.SPWish> suggestions(EOQualifier qualifier) {
    return suggestions(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPWish> suggestions(EOQualifier qualifier, boolean fetch) {
    return suggestions(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPWish> suggestions(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPWish> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPWish.SUGGESTED_BY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPWish.fetchSPWishs(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = suggestions();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPWish>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPWish>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToSuggestions(com.secretpal.model.SPWish object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.SUGGESTIONS_KEY);
  }

  public void removeFromSuggestions(com.secretpal.model.SPWish object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.SUGGESTIONS_KEY);
  }

  public void addToSuggestionsRelationship(com.secretpal.model.SPWish object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to suggestions relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToSuggestions(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.SUGGESTIONS_KEY);
    }
  }

  public void removeFromSuggestionsRelationship(com.secretpal.model.SPWish object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from suggestions relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromSuggestions(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.SUGGESTIONS_KEY);
    }
  }

  public com.secretpal.model.SPWish createSuggestionsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPWish.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.SUGGESTIONS_KEY);
    return (com.secretpal.model.SPWish) eo;
  }

  public void deleteSuggestionsRelationship(com.secretpal.model.SPWish object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.SUGGESTIONS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllSuggestionsRelationships() {
    Enumeration<com.secretpal.model.SPWish> objects = suggestions().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteSuggestionsRelationship(objects.nextElement());
    }
  }

  public NSArray<com.secretpal.model.SPWish> wishes() {
    return (NSArray<com.secretpal.model.SPWish>)storedValueForKey(_SPPerson.WISHES_KEY);
  }

  public NSArray<com.secretpal.model.SPWish> wishes(EOQualifier qualifier) {
    return wishes(qualifier, null, false);
  }

  public NSArray<com.secretpal.model.SPWish> wishes(EOQualifier qualifier, boolean fetch) {
    return wishes(qualifier, null, fetch);
  }

  public NSArray<com.secretpal.model.SPWish> wishes(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<com.secretpal.model.SPWish> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(com.secretpal.model.SPWish.SUGGESTED_FOR_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = com.secretpal.model.SPWish.fetchSPWishs(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = wishes();
      if (qualifier != null) {
        results = (NSArray<com.secretpal.model.SPWish>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<com.secretpal.model.SPWish>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToWishes(com.secretpal.model.SPWish object) {
    includeObjectIntoPropertyWithKey(object, _SPPerson.WISHES_KEY);
  }

  public void removeFromWishes(com.secretpal.model.SPWish object) {
    excludeObjectFromPropertyWithKey(object, _SPPerson.WISHES_KEY);
  }

  public void addToWishesRelationship(com.secretpal.model.SPWish object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("adding " + object + " to wishes relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToWishes(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _SPPerson.WISHES_KEY);
    }
  }

  public void removeFromWishesRelationship(com.secretpal.model.SPWish object) {
    if (_SPPerson.LOG.isDebugEnabled()) {
      _SPPerson.LOG.debug("removing " + object + " from wishes relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromWishes(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.WISHES_KEY);
    }
  }

  public com.secretpal.model.SPWish createWishesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.secretpal.model.SPWish.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _SPPerson.WISHES_KEY);
    return (com.secretpal.model.SPWish) eo;
  }

  public void deleteWishesRelationship(com.secretpal.model.SPWish object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _SPPerson.WISHES_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllWishesRelationships() {
    Enumeration<com.secretpal.model.SPWish> objects = wishes().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteWishesRelationship(objects.nextElement());
    }
  }


  public static SPPerson createSPPerson(EOEditingContext editingContext, Boolean admin
, String emailAddress
, Boolean emailDeliveryFailure
, String name
) {
    SPPerson eo = (SPPerson) EOUtilities.createAndInsertInstance(editingContext, _SPPerson.ENTITY_NAME);    
		eo.setAdmin(admin);
		eo.setEmailAddress(emailAddress);
		eo.setEmailDeliveryFailure(emailDeliveryFailure);
		eo.setName(name);
    return eo;
  }

  public static ERXFetchSpecification<SPPerson> fetchSpec() {
    return new ERXFetchSpecification<SPPerson>(_SPPerson.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<SPPerson> fetchAllSPPersons(EOEditingContext editingContext) {
    return _SPPerson.fetchAllSPPersons(editingContext, null);
  }

  public static NSArray<SPPerson> fetchAllSPPersons(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPPerson.fetchSPPersons(editingContext, null, sortOrderings);
  }

  public static NSArray<SPPerson> fetchSPPersons(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPPerson> fetchSpec = new ERXFetchSpecification<SPPerson>(_SPPerson.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPPerson> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPPerson fetchSPPerson(EOEditingContext editingContext, String keyName, Object value) {
    return _SPPerson.fetchSPPerson(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPPerson fetchSPPerson(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPPerson> eoObjects = _SPPerson.fetchSPPersons(editingContext, qualifier, null);
    SPPerson eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPPerson that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPPerson fetchRequiredSPPerson(EOEditingContext editingContext, String keyName, Object value) {
    return _SPPerson.fetchRequiredSPPerson(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPPerson fetchRequiredSPPerson(EOEditingContext editingContext, EOQualifier qualifier) {
    SPPerson eoObject = _SPPerson.fetchSPPerson(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPPerson that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPPerson localInstanceIn(EOEditingContext editingContext, SPPerson eo) {
    SPPerson localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
