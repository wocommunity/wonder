// DO NOT EDIT.  Make changes to Unit.java instead.
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
public abstract class _Unit extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Unit";

  // Attribute Keys
  public static final ERXKey<NSTimestamp> DATE_ACQUIRED = new ERXKey<NSTimestamp>("dateAcquired");
  public static final ERXKey<String> NOTES = new ERXKey<String>("notes");
  public static final ERXKey<Integer> UNIT_ID = new ERXKey<Integer>("unitID");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Rental> RENTALS = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Rental>("rentals");
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Video> VIDEO = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Video>("video");

  // Attributes
  public static final String DATE_ACQUIRED_KEY = DATE_ACQUIRED.key();
  public static final String NOTES_KEY = NOTES.key();
  public static final String UNIT_ID_KEY = UNIT_ID.key();
  // Relationships
  public static final String RENTALS_KEY = RENTALS.key();
  public static final String VIDEO_KEY = VIDEO.key();

  private static Logger LOG = Logger.getLogger(_Unit.class);

  public Unit localInstanceIn(EOEditingContext editingContext) {
    Unit localInstance = (Unit)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSTimestamp dateAcquired() {
    return (NSTimestamp) storedValueForKey(_Unit.DATE_ACQUIRED_KEY);
  }

  public void setDateAcquired(NSTimestamp value) {
    if (_Unit.LOG.isDebugEnabled()) {
    	_Unit.LOG.debug( "updating dateAcquired from " + dateAcquired() + " to " + value);
    }
    takeStoredValueForKey(value, _Unit.DATE_ACQUIRED_KEY);
  }

  public String notes() {
    return (String) storedValueForKey(_Unit.NOTES_KEY);
  }

  public void setNotes(String value) {
    if (_Unit.LOG.isDebugEnabled()) {
    	_Unit.LOG.debug( "updating notes from " + notes() + " to " + value);
    }
    takeStoredValueForKey(value, _Unit.NOTES_KEY);
  }

  public Integer unitID() {
    return (Integer) storedValueForKey(_Unit.UNIT_ID_KEY);
  }

  public void setUnitID(Integer value) {
    if (_Unit.LOG.isDebugEnabled()) {
    	_Unit.LOG.debug( "updating unitID from " + unitID() + " to " + value);
    }
    takeStoredValueForKey(value, _Unit.UNIT_ID_KEY);
  }

  public webobjectsexamples.businesslogic.rentals.common.Video video() {
    return (webobjectsexamples.businesslogic.rentals.common.Video)storedValueForKey(_Unit.VIDEO_KEY);
  }
  
  public void setVideo(webobjectsexamples.businesslogic.rentals.common.Video value) {
    takeStoredValueForKey(value, _Unit.VIDEO_KEY);
  }

  public void setVideoRelationship(webobjectsexamples.businesslogic.rentals.common.Video value) {
    if (_Unit.LOG.isDebugEnabled()) {
      _Unit.LOG.debug("updating video from " + video() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setVideo(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.rentals.common.Video oldValue = video();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Unit.VIDEO_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Unit.VIDEO_KEY);
    }
  }
  
  public NSArray<webobjectsexamples.businesslogic.rentals.common.Rental> rentals() {
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.Rental>)storedValueForKey(_Unit.RENTALS_KEY);
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
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.rentals.common.Rental.UNIT_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
    includeObjectIntoPropertyWithKey(object, _Unit.RENTALS_KEY);
  }

  public void removeFromRentals(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    excludeObjectFromPropertyWithKey(object, _Unit.RENTALS_KEY);
  }

  public void addToRentalsRelationship(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    if (_Unit.LOG.isDebugEnabled()) {
      _Unit.LOG.debug("adding " + object + " to rentals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToRentals(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Unit.RENTALS_KEY);
    }
  }

  public void removeFromRentalsRelationship(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    if (_Unit.LOG.isDebugEnabled()) {
      _Unit.LOG.debug("removing " + object + " from rentals relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromRentals(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Unit.RENTALS_KEY);
    }
  }

  public webobjectsexamples.businesslogic.rentals.common.Rental createRentalsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.rentals.common.Rental.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Unit.RENTALS_KEY);
    return (webobjectsexamples.businesslogic.rentals.common.Rental) eo;
  }

  public void deleteRentalsRelationship(webobjectsexamples.businesslogic.rentals.common.Rental object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Unit.RENTALS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllRentalsRelationships() {
    Enumeration<webobjectsexamples.businesslogic.rentals.common.Rental> objects = rentals().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRentalsRelationship(objects.nextElement());
    }
  }


  public static Unit createUnit(EOEditingContext editingContext, NSTimestamp dateAcquired
, Integer unitID
, webobjectsexamples.businesslogic.rentals.common.Video video) {
    Unit eo = (Unit) EOUtilities.createAndInsertInstance(editingContext, _Unit.ENTITY_NAME);    
		eo.setDateAcquired(dateAcquired);
		eo.setUnitID(unitID);
    eo.setVideoRelationship(video);
    return eo;
  }

  public static ERXFetchSpecification<Unit> fetchSpec() {
    return new ERXFetchSpecification<Unit>(_Unit.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Unit> fetchAllUnits(EOEditingContext editingContext) {
    return _Unit.fetchAllUnits(editingContext, null);
  }

  public static NSArray<Unit> fetchAllUnits(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Unit.fetchUnits(editingContext, null, sortOrderings);
  }

  public static NSArray<Unit> fetchUnits(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Unit> fetchSpec = new ERXFetchSpecification<Unit>(_Unit.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Unit> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Unit fetchUnit(EOEditingContext editingContext, String keyName, Object value) {
    return _Unit.fetchUnit(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Unit fetchUnit(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Unit> eoObjects = _Unit.fetchUnits(editingContext, qualifier, null);
    Unit eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Unit that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Unit fetchRequiredUnit(EOEditingContext editingContext, String keyName, Object value) {
    return _Unit.fetchRequiredUnit(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Unit fetchRequiredUnit(EOEditingContext editingContext, EOQualifier qualifier) {
    Unit eoObject = _Unit.fetchUnit(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Unit that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Unit localInstanceIn(EOEditingContext editingContext, Unit eo) {
    Unit localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
  public static NSArray<webobjectsexamples.businesslogic.rentals.common.Unit> fetchPrefetchVideoMovie(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("prefetchVideoMovie", _Unit.ENTITY_NAME);
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.Unit>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<webobjectsexamples.businesslogic.rentals.common.Unit> fetchPrefetchVideoMovie(EOEditingContext editingContext)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("prefetchVideoMovie", _Unit.ENTITY_NAME);
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.Unit>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
}
