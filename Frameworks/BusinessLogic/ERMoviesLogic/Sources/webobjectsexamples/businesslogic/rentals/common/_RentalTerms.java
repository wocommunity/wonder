// DO NOT EDIT.  Make changes to RentalTerms.java instead.
package webobjectsexamples.businesslogic.rentals.common;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _RentalTerms extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "RentalTerms";

  // Attribute Keys
  public static final ERXKey<Integer> CHECK_OUT_LENGTH = new ERXKey<Integer>("checkOutLength");
  public static final ERXKey<java.math.BigDecimal> COST = new ERXKey<java.math.BigDecimal>("cost");
  public static final ERXKey<java.math.BigDecimal> DEPOSIT_AMOUNT = new ERXKey<java.math.BigDecimal>("depositAmount");
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys

  // Attributes
  public static final String CHECK_OUT_LENGTH_KEY = CHECK_OUT_LENGTH.key();
  public static final String COST_KEY = COST.key();
  public static final String DEPOSIT_AMOUNT_KEY = DEPOSIT_AMOUNT.key();
  public static final String NAME_KEY = NAME.key();
  // Relationships

  private static Logger LOG = LoggerFactory.getLogger(_RentalTerms.class);

  public RentalTerms localInstanceIn(EOEditingContext editingContext) {
    RentalTerms localInstance = (RentalTerms)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Integer checkOutLength() {
    return (Integer) storedValueForKey(_RentalTerms.CHECK_OUT_LENGTH_KEY);
  }

  public void setCheckOutLength(Integer value) {
    if (_RentalTerms.LOG.isDebugEnabled()) {
    	_RentalTerms.LOG.debug( "updating checkOutLength from " + checkOutLength() + " to " + value);
    }
    takeStoredValueForKey(value, _RentalTerms.CHECK_OUT_LENGTH_KEY);
  }

  public java.math.BigDecimal cost() {
    return (java.math.BigDecimal) storedValueForKey(_RentalTerms.COST_KEY);
  }

  public void setCost(java.math.BigDecimal value) {
    if (_RentalTerms.LOG.isDebugEnabled()) {
    	_RentalTerms.LOG.debug( "updating cost from " + cost() + " to " + value);
    }
    takeStoredValueForKey(value, _RentalTerms.COST_KEY);
  }

  public java.math.BigDecimal depositAmount() {
    return (java.math.BigDecimal) storedValueForKey(_RentalTerms.DEPOSIT_AMOUNT_KEY);
  }

  public void setDepositAmount(java.math.BigDecimal value) {
    if (_RentalTerms.LOG.isDebugEnabled()) {
    	_RentalTerms.LOG.debug( "updating depositAmount from " + depositAmount() + " to " + value);
    }
    takeStoredValueForKey(value, _RentalTerms.DEPOSIT_AMOUNT_KEY);
  }

  public String name() {
    return (String) storedValueForKey(_RentalTerms.NAME_KEY);
  }

  public void setName(String value) {
    if (_RentalTerms.LOG.isDebugEnabled()) {
    	_RentalTerms.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _RentalTerms.NAME_KEY);
  }


  public static RentalTerms createRentalTerms(EOEditingContext editingContext, Integer checkOutLength
, java.math.BigDecimal cost
, java.math.BigDecimal depositAmount
, String name
) {
    RentalTerms eo = (RentalTerms) EOUtilities.createAndInsertInstance(editingContext, _RentalTerms.ENTITY_NAME);    
		eo.setCheckOutLength(checkOutLength);
		eo.setCost(cost);
		eo.setDepositAmount(depositAmount);
		eo.setName(name);
    return eo;
  }

  public static ERXFetchSpecification<RentalTerms> fetchSpec() {
    return new ERXFetchSpecification<RentalTerms>(_RentalTerms.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<RentalTerms> fetchAllRentalTermses(EOEditingContext editingContext) {
    return _RentalTerms.fetchAllRentalTermses(editingContext, null);
  }

  public static NSArray<RentalTerms> fetchAllRentalTermses(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _RentalTerms.fetchRentalTermses(editingContext, null, sortOrderings);
  }

  public static NSArray<RentalTerms> fetchRentalTermses(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<RentalTerms> fetchSpec = new ERXFetchSpecification<RentalTerms>(_RentalTerms.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<RentalTerms> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static RentalTerms fetchRentalTerms(EOEditingContext editingContext, String keyName, Object value) {
    return _RentalTerms.fetchRentalTerms(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static RentalTerms fetchRentalTerms(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<RentalTerms> eoObjects = _RentalTerms.fetchRentalTermses(editingContext, qualifier, null);
    RentalTerms eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one RentalTerms that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static RentalTerms fetchRequiredRentalTerms(EOEditingContext editingContext, String keyName, Object value) {
    return _RentalTerms.fetchRequiredRentalTerms(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static RentalTerms fetchRequiredRentalTerms(EOEditingContext editingContext, EOQualifier qualifier) {
    RentalTerms eoObject = _RentalTerms.fetchRentalTerms(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no RentalTerms that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static RentalTerms localInstanceIn(EOEditingContext editingContext, RentalTerms eo) {
    RentalTerms localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
