// DO NOT EDIT.  Make changes to Rental.java instead.
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
public abstract class _Rental extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Rental";

  // Attribute Keys
  public static final ERXKey<NSTimestamp> DATE_OUT = new ERXKey<NSTimestamp>("dateOut");
  public static final ERXKey<NSTimestamp> DATE_RETURNED = new ERXKey<NSTimestamp>("dateReturned");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Customer> CUSTOMER = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Customer>("customer");
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Fee> FEES = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Fee>("fees");
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Unit> UNIT = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Unit>("unit");

  // Attributes
  public static final String DATE_OUT_KEY = DATE_OUT.key();
  public static final String DATE_RETURNED_KEY = DATE_RETURNED.key();
  // Relationships
  public static final String CUSTOMER_KEY = CUSTOMER.key();
  public static final String FEES_KEY = FEES.key();
  public static final String UNIT_KEY = UNIT.key();

  private static Logger LOG = Logger.getLogger(_Rental.class);

  public Rental localInstanceIn(EOEditingContext editingContext) {
    Rental localInstance = (Rental)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSTimestamp dateOut() {
    return (NSTimestamp) storedValueForKey(_Rental.DATE_OUT_KEY);
  }

  public void setDateOut(NSTimestamp value) {
    if (_Rental.LOG.isDebugEnabled()) {
    	_Rental.LOG.debug( "updating dateOut from " + dateOut() + " to " + value);
    }
    takeStoredValueForKey(value, _Rental.DATE_OUT_KEY);
  }

  public NSTimestamp dateReturned() {
    return (NSTimestamp) storedValueForKey(_Rental.DATE_RETURNED_KEY);
  }

  public void setDateReturned(NSTimestamp value) {
    if (_Rental.LOG.isDebugEnabled()) {
    	_Rental.LOG.debug( "updating dateReturned from " + dateReturned() + " to " + value);
    }
    takeStoredValueForKey(value, _Rental.DATE_RETURNED_KEY);
  }

  public webobjectsexamples.businesslogic.rentals.common.Customer customer() {
    return (webobjectsexamples.businesslogic.rentals.common.Customer)storedValueForKey(_Rental.CUSTOMER_KEY);
  }
  
  public void setCustomer(webobjectsexamples.businesslogic.rentals.common.Customer value) {
    takeStoredValueForKey(value, _Rental.CUSTOMER_KEY);
  }

  public void setCustomerRelationship(webobjectsexamples.businesslogic.rentals.common.Customer value) {
    if (_Rental.LOG.isDebugEnabled()) {
      _Rental.LOG.debug("updating customer from " + customer() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCustomer(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.rentals.common.Customer oldValue = customer();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Rental.CUSTOMER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Rental.CUSTOMER_KEY);
    }
  }
  
  public webobjectsexamples.businesslogic.rentals.common.Unit unit() {
    return (webobjectsexamples.businesslogic.rentals.common.Unit)storedValueForKey(_Rental.UNIT_KEY);
  }
  
  public void setUnit(webobjectsexamples.businesslogic.rentals.common.Unit value) {
    takeStoredValueForKey(value, _Rental.UNIT_KEY);
  }

  public void setUnitRelationship(webobjectsexamples.businesslogic.rentals.common.Unit value) {
    if (_Rental.LOG.isDebugEnabled()) {
      _Rental.LOG.debug("updating unit from " + unit() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setUnit(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.rentals.common.Unit oldValue = unit();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Rental.UNIT_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Rental.UNIT_KEY);
    }
  }
  
  public NSArray<webobjectsexamples.businesslogic.rentals.common.Fee> fees() {
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.Fee>)storedValueForKey(_Rental.FEES_KEY);
  }

  public NSArray<webobjectsexamples.businesslogic.rentals.common.Fee> fees(EOQualifier qualifier) {
    return fees(qualifier, null, false);
  }

  public NSArray<webobjectsexamples.businesslogic.rentals.common.Fee> fees(EOQualifier qualifier, boolean fetch) {
    return fees(qualifier, null, fetch);
  }

  public NSArray<webobjectsexamples.businesslogic.rentals.common.Fee> fees(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<webobjectsexamples.businesslogic.rentals.common.Fee> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.rentals.common.Fee.RENTAL_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = webobjectsexamples.businesslogic.rentals.common.Fee.fetchFees(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = fees();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.rentals.common.Fee>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.rentals.common.Fee>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToFees(webobjectsexamples.businesslogic.rentals.common.Fee object) {
    includeObjectIntoPropertyWithKey(object, _Rental.FEES_KEY);
  }

  public void removeFromFees(webobjectsexamples.businesslogic.rentals.common.Fee object) {
    excludeObjectFromPropertyWithKey(object, _Rental.FEES_KEY);
  }

  public void addToFeesRelationship(webobjectsexamples.businesslogic.rentals.common.Fee object) {
    if (_Rental.LOG.isDebugEnabled()) {
      _Rental.LOG.debug("adding " + object + " to fees relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToFees(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Rental.FEES_KEY);
    }
  }

  public void removeFromFeesRelationship(webobjectsexamples.businesslogic.rentals.common.Fee object) {
    if (_Rental.LOG.isDebugEnabled()) {
      _Rental.LOG.debug("removing " + object + " from fees relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromFees(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Rental.FEES_KEY);
    }
  }

  public webobjectsexamples.businesslogic.rentals.common.Fee createFeesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.rentals.common.Fee.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Rental.FEES_KEY);
    return (webobjectsexamples.businesslogic.rentals.common.Fee) eo;
  }

  public void deleteFeesRelationship(webobjectsexamples.businesslogic.rentals.common.Fee object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Rental.FEES_KEY);
  }

  public void deleteAllFeesRelationships() {
    Enumeration<webobjectsexamples.businesslogic.rentals.common.Fee> objects = fees().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteFeesRelationship(objects.nextElement());
    }
  }


  public static Rental createRental(EOEditingContext editingContext, NSTimestamp dateOut
, webobjectsexamples.businesslogic.rentals.common.Customer customer, webobjectsexamples.businesslogic.rentals.common.Unit unit) {
    Rental eo = (Rental) EOUtilities.createAndInsertInstance(editingContext, _Rental.ENTITY_NAME);    
		eo.setDateOut(dateOut);
    eo.setCustomerRelationship(customer);
    eo.setUnitRelationship(unit);
    return eo;
  }

  public static ERXFetchSpecification<Rental> fetchSpec() {
    return new ERXFetchSpecification<Rental>(_Rental.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Rental> fetchAllRentals(EOEditingContext editingContext) {
    return _Rental.fetchAllRentals(editingContext, null);
  }

  public static NSArray<Rental> fetchAllRentals(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Rental.fetchRentals(editingContext, null, sortOrderings);
  }

  public static NSArray<Rental> fetchRentals(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Rental> fetchSpec = new ERXFetchSpecification<Rental>(_Rental.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Rental> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Rental fetchRental(EOEditingContext editingContext, String keyName, Object value) {
    return _Rental.fetchRental(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Rental fetchRental(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Rental> eoObjects = _Rental.fetchRentals(editingContext, qualifier, null);
    Rental eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Rental that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Rental fetchRequiredRental(EOEditingContext editingContext, String keyName, Object value) {
    return _Rental.fetchRequiredRental(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Rental fetchRequiredRental(EOEditingContext editingContext, EOQualifier qualifier) {
    Rental eoObject = _Rental.fetchRental(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Rental that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Rental localInstanceIn(EOEditingContext editingContext, Rental eo) {
    Rental localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
