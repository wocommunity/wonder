// DO NOT EDIT.  Make changes to FeeType.java instead.
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
public abstract class _FeeType extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "FeeType";

  // Attribute Keys
  public static final ERXKey<Integer> ENABLED = new ERXKey<Integer>("enabled");
  public static final ERXKey<String> FEE_TYPE = new ERXKey<String>("feeType");
  public static final ERXKey<Integer> ORDER_BY = new ERXKey<Integer>("orderBy");
  // Relationship Keys

  // Attributes
  public static final String ENABLED_KEY = ENABLED.key();
  public static final String FEE_TYPE_KEY = FEE_TYPE.key();
  public static final String ORDER_BY_KEY = ORDER_BY.key();
  // Relationships

  private static Logger LOG = Logger.getLogger(_FeeType.class);

  public FeeType localInstanceIn(EOEditingContext editingContext) {
    FeeType localInstance = (FeeType)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Integer enabled() {
    return (Integer) storedValueForKey(_FeeType.ENABLED_KEY);
  }

  public void setEnabled(Integer value) {
    if (_FeeType.LOG.isDebugEnabled()) {
    	_FeeType.LOG.debug( "updating enabled from " + enabled() + " to " + value);
    }
    takeStoredValueForKey(value, _FeeType.ENABLED_KEY);
  }

  public String feeType() {
    return (String) storedValueForKey(_FeeType.FEE_TYPE_KEY);
  }

  public void setFeeType(String value) {
    if (_FeeType.LOG.isDebugEnabled()) {
    	_FeeType.LOG.debug( "updating feeType from " + feeType() + " to " + value);
    }
    takeStoredValueForKey(value, _FeeType.FEE_TYPE_KEY);
  }

  public Integer orderBy() {
    return (Integer) storedValueForKey(_FeeType.ORDER_BY_KEY);
  }

  public void setOrderBy(Integer value) {
    if (_FeeType.LOG.isDebugEnabled()) {
    	_FeeType.LOG.debug( "updating orderBy from " + orderBy() + " to " + value);
    }
    takeStoredValueForKey(value, _FeeType.ORDER_BY_KEY);
  }


  public static FeeType createFeeType(EOEditingContext editingContext, Integer enabled
, String feeType
, Integer orderBy
) {
    FeeType eo = (FeeType) EOUtilities.createAndInsertInstance(editingContext, _FeeType.ENTITY_NAME);    
		eo.setEnabled(enabled);
		eo.setFeeType(feeType);
		eo.setOrderBy(orderBy);
    return eo;
  }

  public static ERXFetchSpecification<FeeType> fetchSpec() {
    return new ERXFetchSpecification<FeeType>(_FeeType.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<FeeType> fetchAllFeeTypes(EOEditingContext editingContext) {
    return _FeeType.fetchAllFeeTypes(editingContext, null);
  }

  public static NSArray<FeeType> fetchAllFeeTypes(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _FeeType.fetchFeeTypes(editingContext, null, sortOrderings);
  }

  public static NSArray<FeeType> fetchFeeTypes(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<FeeType> fetchSpec = new ERXFetchSpecification<FeeType>(_FeeType.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<FeeType> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static FeeType fetchFeeType(EOEditingContext editingContext, String keyName, Object value) {
    return _FeeType.fetchFeeType(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static FeeType fetchFeeType(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<FeeType> eoObjects = _FeeType.fetchFeeTypes(editingContext, qualifier, null);
    FeeType eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one FeeType that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static FeeType fetchRequiredFeeType(EOEditingContext editingContext, String keyName, Object value) {
    return _FeeType.fetchRequiredFeeType(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static FeeType fetchRequiredFeeType(EOEditingContext editingContext, EOQualifier qualifier) {
    FeeType eoObject = _FeeType.fetchFeeType(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no FeeType that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static FeeType localInstanceIn(EOEditingContext editingContext, FeeType eo) {
    FeeType localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
