// DO NOT EDIT.  Make changes to Customer.java instead.
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
public abstract class _Customer extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Customer";

  // Attribute Keys
  public static final ERXKey<String> CITY = new ERXKey<String>("city");
  public static final ERXKey<String> FIRST_NAME = new ERXKey<String>("firstName");
  public static final ERXKey<String> LAST_NAME = new ERXKey<String>("lastName");
  public static final ERXKey<NSTimestamp> MEMBER_SINCE = new ERXKey<NSTimestamp>("memberSince");
  public static final ERXKey<String> PHONE = new ERXKey<String>("phone");
  public static final ERXKey<String> STATE = new ERXKey<String>("state");
  public static final ERXKey<String> STREET_ADDRESS = new ERXKey<String>("streetAddress");
  public static final ERXKey<String> ZIP = new ERXKey<String>("zip");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.CreditCard> CREDIT_CARD = new ERXKey<webobjectsexamples.businesslogic.rentals.common.CreditCard>("creditCard");
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Rental> RENTALS = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Rental>("rentals");

  // Attributes
  public static final String CITY_KEY = CITY.key();
  public static final String FIRST_NAME_KEY = FIRST_NAME.key();
  public static final String LAST_NAME_KEY = LAST_NAME.key();
  public static final String MEMBER_SINCE_KEY = MEMBER_SINCE.key();
  public static final String PHONE_KEY = PHONE.key();
  public static final String STATE_KEY = STATE.key();
  public static final String STREET_ADDRESS_KEY = STREET_ADDRESS.key();
  public static final String ZIP_KEY = ZIP.key();
  // Relationships
  public static final String CREDIT_CARD_KEY = CREDIT_CARD.key();
  public static final String RENTALS_KEY = RENTALS.key();

  private static Logger LOG = Logger.getLogger(_Customer.class);

  public Customer localInstanceIn(EOEditingContext editingContext) {
    Customer localInstance = (Customer)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String city() {
    return (String) storedValueForKey(_Customer.CITY_KEY);
  }

  public void setCity(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating city from " + city() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.CITY_KEY);
  }

  public String firstName() {
    return (String) storedValueForKey(_Customer.FIRST_NAME_KEY);
  }

  public void setFirstName(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating firstName from " + firstName() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.FIRST_NAME_KEY);
  }

  public String lastName() {
    return (String) storedValueForKey(_Customer.LAST_NAME_KEY);
  }

  public void setLastName(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating lastName from " + lastName() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.LAST_NAME_KEY);
  }

  public NSTimestamp memberSince() {
    return (NSTimestamp) storedValueForKey(_Customer.MEMBER_SINCE_KEY);
  }

  public void setMemberSince(NSTimestamp value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating memberSince from " + memberSince() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.MEMBER_SINCE_KEY);
  }

  public String phone() {
    return (String) storedValueForKey(_Customer.PHONE_KEY);
  }

  public void setPhone(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating phone from " + phone() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.PHONE_KEY);
  }

  public String state() {
    return (String) storedValueForKey(_Customer.STATE_KEY);
  }

  public void setState(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating state from " + state() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.STATE_KEY);
  }

  public String streetAddress() {
    return (String) storedValueForKey(_Customer.STREET_ADDRESS_KEY);
  }

  public void setStreetAddress(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating streetAddress from " + streetAddress() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.STREET_ADDRESS_KEY);
  }

  public String zip() {
    return (String) storedValueForKey(_Customer.ZIP_KEY);
  }

  public void setZip(String value) {
    if (_Customer.LOG.isDebugEnabled()) {
    	_Customer.LOG.debug( "updating zip from " + zip() + " to " + value);
    }
    takeStoredValueForKey(value, _Customer.ZIP_KEY);
  }

  public webobjectsexamples.businesslogic.rentals.common.CreditCard creditCard() {
    return (webobjectsexamples.businesslogic.rentals.common.CreditCard)storedValueForKey(_Customer.CREDIT_CARD_KEY);
  }
  
  public void setCreditCard(webobjectsexamples.businesslogic.rentals.common.CreditCard value) {
    takeStoredValueForKey(value, _Customer.CREDIT_CARD_KEY);
  }

  public void setCreditCardRelationship(webobjectsexamples.businesslogic.rentals.common.CreditCard value) {
    if (_Customer.LOG.isDebugEnabled()) {
      _Customer.LOG.debug("updating creditCard from " + creditCard() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCreditCard(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.rentals.common.CreditCard oldValue = creditCard();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Customer.CREDIT_CARD_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Customer.CREDIT_CARD_KEY);
    }
  }
  
  public NSArray<webobjectsexamples.businesslogic.rentals.common.Rental> rentals() {
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.Rental>)storedValueForKey(_Customer.RENTALS_KEY);
  }

  public NSArray<webobjectsexamples.businesslogic.rentals.common.Rental> rentals(EOQualifier qualifier) {
    return rentals(qualifier, null, false);
  }

  public NSArray<webobjectsexamples.businesslogic.rentals.common.Rental> rentals(EOQualifier qualifier, boolean fetch) {
    return rentals(qualifier, null, fetch);
  }

  public NSArray<webobjectsexamples.businesslogic.rentals.common.Rental> rentals(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<webobjectsexamples.businesslogic.rentals.common.Rental> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.rentals.common.Rental.CUSTOMER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = webobjectsexamples.businesslogic.rentals.common.Rental.fetchRentals(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = rentals();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.rentals.common.Rental>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.rentals.common.Rental>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToRentals(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    includeObjectIntoPropertyWithKey(object, _Customer.RENTALS_KEY);
  }

  public void removeFromRentals(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    excludeObjectFromPropertyWithKey(object, _Customer.RENTALS_KEY);
  }

  public void addToRentalsRelationship(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    if (_Customer.LOG.isDebugEnabled()) {
      _Customer.LOG.debug("adding " + object + " to rentals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToRentals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Customer.RENTALS_KEY);
    }
  }

  public void removeFromRentalsRelationship(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    if (_Customer.LOG.isDebugEnabled()) {
      _Customer.LOG.debug("removing " + object + " from rentals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromRentals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Customer.RENTALS_KEY);
    }
  }

  public webobjectsexamples.businesslogic.rentals.common.Rental createRentalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.rentals.common.Rental.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Customer.RENTALS_KEY);
    return (webobjectsexamples.businesslogic.rentals.common.Rental) eo;
  }

  public void deleteRentalsRelationship(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Customer.RENTALS_KEY);
  }

  public void deleteAllRentalsRelationships() {
    Enumeration<webobjectsexamples.businesslogic.rentals.common.Rental> objects = rentals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRentalsRelationship(objects.nextElement());
    }
  }


  public static Customer createCustomer(EOEditingContext editingContext, String city
, String firstName
, String lastName
) {
    Customer eo = (Customer) EOUtilities.createAndInsertInstance(editingContext, _Customer.ENTITY_NAME);    
		eo.setCity(city);
		eo.setFirstName(firstName);
		eo.setLastName(lastName);
    return eo;
  }

  public static ERXFetchSpecification<Customer> fetchSpec() {
    return new ERXFetchSpecification<Customer>(_Customer.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Customer> fetchAllCustomers(EOEditingContext editingContext) {
    return _Customer.fetchAllCustomers(editingContext, null);
  }

  public static NSArray<Customer> fetchAllCustomers(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Customer.fetchCustomers(editingContext, null, sortOrderings);
  }

  public static NSArray<Customer> fetchCustomers(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Customer> fetchSpec = new ERXFetchSpecification<Customer>(_Customer.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Customer> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Customer fetchCustomer(EOEditingContext editingContext, String keyName, Object value) {
    return _Customer.fetchCustomer(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Customer fetchCustomer(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Customer> eoObjects = _Customer.fetchCustomers(editingContext, qualifier, null);
    Customer eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Customer that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Customer fetchRequiredCustomer(EOEditingContext editingContext, String keyName, Object value) {
    return _Customer.fetchRequiredCustomer(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Customer fetchRequiredCustomer(EOEditingContext editingContext, EOQualifier qualifier) {
    Customer eoObject = _Customer.fetchCustomer(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Customer that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Customer localInstanceIn(EOEditingContext editingContext, Customer eo) {
    Customer localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
