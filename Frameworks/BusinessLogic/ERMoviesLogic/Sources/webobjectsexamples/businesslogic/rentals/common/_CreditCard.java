// DO NOT EDIT.  Make changes to CreditCard.java instead.
package webobjectsexamples.businesslogic.rentals.common;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _CreditCard extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "CreditCard";

  // Attribute Keys
  public static final ERXKey<NSTimestamp> AUTHORIZATION_DATE = new ERXKey<NSTimestamp>("authorizationDate");
  public static final ERXKey<String> AUTHORIZATION_NUM = new ERXKey<String>("authorizationNum");
  public static final ERXKey<String> CARD_NUMBER = new ERXKey<String>("cardNumber");
  public static final ERXKey<NSTimestamp> EXPIRATION_DATE = new ERXKey<NSTimestamp>("expirationDate");
  public static final ERXKey<java.math.BigDecimal> LIMIT = new ERXKey<java.math.BigDecimal>("limit");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Customer> CUSTOMER = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Customer>("customer");

  // Attributes
  public static final String AUTHORIZATION_DATE_KEY = AUTHORIZATION_DATE.key();
  public static final String AUTHORIZATION_NUM_KEY = AUTHORIZATION_NUM.key();
  public static final String CARD_NUMBER_KEY = CARD_NUMBER.key();
  public static final String EXPIRATION_DATE_KEY = EXPIRATION_DATE.key();
  public static final String LIMIT_KEY = LIMIT.key();
  // Relationships
  public static final String CUSTOMER_KEY = CUSTOMER.key();

  private static Logger LOG = Logger.getLogger(_CreditCard.class);

  public CreditCard localInstanceIn(EOEditingContext editingContext) {
    CreditCard localInstance = (CreditCard)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSTimestamp authorizationDate() {
    return (NSTimestamp) storedValueForKey(_CreditCard.AUTHORIZATION_DATE_KEY);
  }

  public void setAuthorizationDate(NSTimestamp value) {
    if (_CreditCard.LOG.isDebugEnabled()) {
    	_CreditCard.LOG.debug( "updating authorizationDate from " + authorizationDate() + " to " + value);
    }
    takeStoredValueForKey(value, _CreditCard.AUTHORIZATION_DATE_KEY);
  }

  public String authorizationNum() {
    return (String) storedValueForKey(_CreditCard.AUTHORIZATION_NUM_KEY);
  }

  public void setAuthorizationNum(String value) {
    if (_CreditCard.LOG.isDebugEnabled()) {
    	_CreditCard.LOG.debug( "updating authorizationNum from " + authorizationNum() + " to " + value);
    }
    takeStoredValueForKey(value, _CreditCard.AUTHORIZATION_NUM_KEY);
  }

  public String cardNumber() {
    return (String) storedValueForKey(_CreditCard.CARD_NUMBER_KEY);
  }

  public void setCardNumber(String value) {
    if (_CreditCard.LOG.isDebugEnabled()) {
    	_CreditCard.LOG.debug( "updating cardNumber from " + cardNumber() + " to " + value);
    }
    takeStoredValueForKey(value, _CreditCard.CARD_NUMBER_KEY);
  }

  public NSTimestamp expirationDate() {
    return (NSTimestamp) storedValueForKey(_CreditCard.EXPIRATION_DATE_KEY);
  }

  public void setExpirationDate(NSTimestamp value) {
    if (_CreditCard.LOG.isDebugEnabled()) {
    	_CreditCard.LOG.debug( "updating expirationDate from " + expirationDate() + " to " + value);
    }
    takeStoredValueForKey(value, _CreditCard.EXPIRATION_DATE_KEY);
  }

  public java.math.BigDecimal limit() {
    return (java.math.BigDecimal) storedValueForKey(_CreditCard.LIMIT_KEY);
  }

  public void setLimit(java.math.BigDecimal value) {
    if (_CreditCard.LOG.isDebugEnabled()) {
    	_CreditCard.LOG.debug( "updating limit from " + limit() + " to " + value);
    }
    takeStoredValueForKey(value, _CreditCard.LIMIT_KEY);
  }

  public webobjectsexamples.businesslogic.rentals.common.Customer customer() {
    return (webobjectsexamples.businesslogic.rentals.common.Customer)storedValueForKey(_CreditCard.CUSTOMER_KEY);
  }
  
  public void setCustomer(webobjectsexamples.businesslogic.rentals.common.Customer value) {
    takeStoredValueForKey(value, _CreditCard.CUSTOMER_KEY);
  }

  public void setCustomerRelationship(webobjectsexamples.businesslogic.rentals.common.Customer value) {
    if (_CreditCard.LOG.isDebugEnabled()) {
      _CreditCard.LOG.debug("updating customer from " + customer() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCustomer(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.rentals.common.Customer oldValue = customer();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _CreditCard.CUSTOMER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _CreditCard.CUSTOMER_KEY);
    }
  }
  

  public static CreditCard createCreditCard(EOEditingContext editingContext, NSTimestamp authorizationDate
, String authorizationNum
, String cardNumber
, NSTimestamp expirationDate
, java.math.BigDecimal limit
, webobjectsexamples.businesslogic.rentals.common.Customer customer) {
    CreditCard eo = (CreditCard) EOUtilities.createAndInsertInstance(editingContext, _CreditCard.ENTITY_NAME);    
		eo.setAuthorizationDate(authorizationDate);
		eo.setAuthorizationNum(authorizationNum);
		eo.setCardNumber(cardNumber);
		eo.setExpirationDate(expirationDate);
		eo.setLimit(limit);
    eo.setCustomerRelationship(customer);
    return eo;
  }

  public static ERXFetchSpecification<CreditCard> fetchSpec() {
    return new ERXFetchSpecification<CreditCard>(_CreditCard.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<CreditCard> fetchAllCreditCards(EOEditingContext editingContext) {
    return _CreditCard.fetchAllCreditCards(editingContext, null);
  }

  public static NSArray<CreditCard> fetchAllCreditCards(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _CreditCard.fetchCreditCards(editingContext, null, sortOrderings);
  }

  public static NSArray<CreditCard> fetchCreditCards(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<CreditCard> fetchSpec = new ERXFetchSpecification<CreditCard>(_CreditCard.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<CreditCard> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static CreditCard fetchCreditCard(EOEditingContext editingContext, String keyName, Object value) {
    return _CreditCard.fetchCreditCard(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static CreditCard fetchCreditCard(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<CreditCard> eoObjects = _CreditCard.fetchCreditCards(editingContext, qualifier, null);
    CreditCard eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one CreditCard that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static CreditCard fetchRequiredCreditCard(EOEditingContext editingContext, String keyName, Object value) {
    return _CreditCard.fetchRequiredCreditCard(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static CreditCard fetchRequiredCreditCard(EOEditingContext editingContext, EOQualifier qualifier) {
    CreditCard eoObject = _CreditCard.fetchCreditCard(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no CreditCard that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static CreditCard localInstanceIn(EOEditingContext editingContext, CreditCard eo) {
    CreditCard localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
