// DO NOT EDIT.  Make changes to SPWish.java instead.
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
public abstract class _SPWish extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "SPWish";

  // Attribute Keys
  public static final ERXKey<java.math.BigDecimal> COST = new ERXKey<java.math.BigDecimal>("cost");
  public static final ERXKey<String> DESCRIPTION = new ERXKey<String>("description");
  public static final ERXKey<Boolean> PURCHASED = new ERXKey<Boolean>("purchased");
  // Relationship Keys
  public static final ERXKey<com.secretpal.model.SPPerson> SUGGESTED_BY = new ERXKey<com.secretpal.model.SPPerson>("suggestedBy");
  public static final ERXKey<com.secretpal.model.SPPerson> SUGGESTED_FOR = new ERXKey<com.secretpal.model.SPPerson>("suggestedFor");

  // Attributes
  public static final String COST_KEY = COST.key();
  public static final String DESCRIPTION_KEY = DESCRIPTION.key();
  public static final String PURCHASED_KEY = PURCHASED.key();
  // Relationships
  public static final String SUGGESTED_BY_KEY = SUGGESTED_BY.key();
  public static final String SUGGESTED_FOR_KEY = SUGGESTED_FOR.key();

  private static Logger LOG = Logger.getLogger(_SPWish.class);

  public SPWish localInstanceIn(EOEditingContext editingContext) {
    SPWish localInstance = (SPWish)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public java.math.BigDecimal cost() {
    return (java.math.BigDecimal) storedValueForKey(_SPWish.COST_KEY);
  }

  public void setCost(java.math.BigDecimal value) {
    if (_SPWish.LOG.isDebugEnabled()) {
    	_SPWish.LOG.debug( "updating cost from " + cost() + " to " + value);
    }
    takeStoredValueForKey(value, _SPWish.COST_KEY);
  }

  public String description() {
    return (String) storedValueForKey(_SPWish.DESCRIPTION_KEY);
  }

  public void setDescription(String value) {
    if (_SPWish.LOG.isDebugEnabled()) {
    	_SPWish.LOG.debug( "updating description from " + description() + " to " + value);
    }
    takeStoredValueForKey(value, _SPWish.DESCRIPTION_KEY);
  }

  public Boolean purchased() {
    return (Boolean) storedValueForKey(_SPWish.PURCHASED_KEY);
  }

  public void setPurchased(Boolean value) {
    if (_SPWish.LOG.isDebugEnabled()) {
    	_SPWish.LOG.debug( "updating purchased from " + purchased() + " to " + value);
    }
    takeStoredValueForKey(value, _SPWish.PURCHASED_KEY);
  }

  public com.secretpal.model.SPPerson suggestedBy() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPWish.SUGGESTED_BY_KEY);
  }
  
  public void setSuggestedBy(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPWish.SUGGESTED_BY_KEY);
  }

  public void setSuggestedByRelationship(com.secretpal.model.SPPerson value) {
    if (_SPWish.LOG.isDebugEnabled()) {
      _SPWish.LOG.debug("updating suggestedBy from " + suggestedBy() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setSuggestedBy(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = suggestedBy();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPWish.SUGGESTED_BY_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPWish.SUGGESTED_BY_KEY);
    }
  }
  
  public com.secretpal.model.SPPerson suggestedFor() {
    return (com.secretpal.model.SPPerson)storedValueForKey(_SPWish.SUGGESTED_FOR_KEY);
  }
  
  public void setSuggestedFor(com.secretpal.model.SPPerson value) {
    takeStoredValueForKey(value, _SPWish.SUGGESTED_FOR_KEY);
  }

  public void setSuggestedForRelationship(com.secretpal.model.SPPerson value) {
    if (_SPWish.LOG.isDebugEnabled()) {
      _SPWish.LOG.debug("updating suggestedFor from " + suggestedFor() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setSuggestedFor(value);
    }
    else if (value == null) {
    	com.secretpal.model.SPPerson oldValue = suggestedFor();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _SPWish.SUGGESTED_FOR_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _SPWish.SUGGESTED_FOR_KEY);
    }
  }
  

  public static SPWish createSPWish(EOEditingContext editingContext, Boolean purchased
, com.secretpal.model.SPPerson suggestedBy, com.secretpal.model.SPPerson suggestedFor) {
    SPWish eo = (SPWish) EOUtilities.createAndInsertInstance(editingContext, _SPWish.ENTITY_NAME);    
		eo.setPurchased(purchased);
    eo.setSuggestedByRelationship(suggestedBy);
    eo.setSuggestedForRelationship(suggestedFor);
    return eo;
  }

  public static ERXFetchSpecification<SPWish> fetchSpec() {
    return new ERXFetchSpecification<SPWish>(_SPWish.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<SPWish> fetchAllSPWishs(EOEditingContext editingContext) {
    return _SPWish.fetchAllSPWishs(editingContext, null);
  }

  public static NSArray<SPWish> fetchAllSPWishs(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _SPWish.fetchSPWishs(editingContext, null, sortOrderings);
  }

  public static NSArray<SPWish> fetchSPWishs(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<SPWish> fetchSpec = new ERXFetchSpecification<SPWish>(_SPWish.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<SPWish> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static SPWish fetchSPWish(EOEditingContext editingContext, String keyName, Object value) {
    return _SPWish.fetchSPWish(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPWish fetchSPWish(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<SPWish> eoObjects = _SPWish.fetchSPWishs(editingContext, qualifier, null);
    SPWish eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one SPWish that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPWish fetchRequiredSPWish(EOEditingContext editingContext, String keyName, Object value) {
    return _SPWish.fetchRequiredSPWish(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static SPWish fetchRequiredSPWish(EOEditingContext editingContext, EOQualifier qualifier) {
    SPWish eoObject = _SPWish.fetchSPWish(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no SPWish that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static SPWish localInstanceIn(EOEditingContext editingContext, SPWish eo) {
    SPWish localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
