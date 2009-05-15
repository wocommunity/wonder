// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Paycheck.java instead.
package er.erxtest.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Paycheck extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Paycheck";

  // Attribute Keys
  public static final ERXKey<java.math.BigDecimal> AMOUNT = new ERXKey<java.math.BigDecimal>("amount");
  public static final ERXKey<Boolean> CASHED = new ERXKey<Boolean>("cashed");
  public static final ERXKey<NSTimestamp> PAYMENT_DATE = new ERXKey<NSTimestamp>("paymentDate");
  // Relationship Keys
  public static final ERXKey<er.erxtest.model.Employee> EMPLOYEE = new ERXKey<er.erxtest.model.Employee>("employee");

  // Attributes
  public static final String AMOUNT_KEY = AMOUNT.key();
  public static final String CASHED_KEY = CASHED.key();
  public static final String PAYMENT_DATE_KEY = PAYMENT_DATE.key();
  // Relationships
  public static final String EMPLOYEE_KEY = EMPLOYEE.key();

  private static Logger LOG = Logger.getLogger(_Paycheck.class);

  public Paycheck localInstanceIn(EOEditingContext editingContext) {
    Paycheck localInstance = (Paycheck)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public java.math.BigDecimal amount() {
    return (java.math.BigDecimal) storedValueForKey("amount");
  }

  public void setAmount(java.math.BigDecimal value) {
    if (_Paycheck.LOG.isDebugEnabled()) {
    	_Paycheck.LOG.debug( "updating amount from " + amount() + " to " + value);
    }
    takeStoredValueForKey(value, "amount");
  }

  public Boolean cashed() {
    return (Boolean) storedValueForKey("cashed");
  }

  public void setCashed(Boolean value) {
    if (_Paycheck.LOG.isDebugEnabled()) {
    	_Paycheck.LOG.debug( "updating cashed from " + cashed() + " to " + value);
    }
    takeStoredValueForKey(value, "cashed");
  }

  public NSTimestamp paymentDate() {
    return (NSTimestamp) storedValueForKey("paymentDate");
  }

  public void setPaymentDate(NSTimestamp value) {
    if (_Paycheck.LOG.isDebugEnabled()) {
    	_Paycheck.LOG.debug( "updating paymentDate from " + paymentDate() + " to " + value);
    }
    takeStoredValueForKey(value, "paymentDate");
  }

  public er.erxtest.model.Employee employee() {
    return (er.erxtest.model.Employee)storedValueForKey("employee");
  }
  
  public void setEmployee(er.erxtest.model.Employee value) {
    takeStoredValueForKey(value, "employee");
  }

  public void setEmployeeRelationship(er.erxtest.model.Employee value) {
    if (_Paycheck.LOG.isDebugEnabled()) {
      _Paycheck.LOG.debug("updating employee from " + employee() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setEmployee(value);
    }
    else if (value == null) {
    	er.erxtest.model.Employee oldValue = employee();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "employee");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "employee");
    }
  }
  

  public static Paycheck createPaycheck(EOEditingContext editingContext, java.math.BigDecimal amount
, Boolean cashed
, NSTimestamp paymentDate
, er.erxtest.model.Employee employee) {
    Paycheck eo = (Paycheck) EOUtilities.createAndInsertInstance(editingContext, _Paycheck.ENTITY_NAME);    
		eo.setAmount(amount);
		eo.setCashed(cashed);
		eo.setPaymentDate(paymentDate);
    eo.setEmployeeRelationship(employee);
    return eo;
  }

  public static NSArray<Paycheck> fetchAllPaychecks(EOEditingContext editingContext) {
    return _Paycheck.fetchAllPaychecks(editingContext, null);
  }

  public static NSArray<Paycheck> fetchAllPaychecks(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Paycheck.fetchPaychecks(editingContext, null, sortOrderings);
  }

  public static NSArray<Paycheck> fetchPaychecks(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Paycheck.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Paycheck> eoObjects = (NSArray<Paycheck>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Paycheck fetchPaycheck(EOEditingContext editingContext, String keyName, Object value) {
    return _Paycheck.fetchPaycheck(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Paycheck fetchPaycheck(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Paycheck> eoObjects = _Paycheck.fetchPaychecks(editingContext, qualifier, null);
    Paycheck eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Paycheck)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Paycheck that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Paycheck fetchRequiredPaycheck(EOEditingContext editingContext, String keyName, Object value) {
    return _Paycheck.fetchRequiredPaycheck(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Paycheck fetchRequiredPaycheck(EOEditingContext editingContext, EOQualifier qualifier) {
    Paycheck eoObject = _Paycheck.fetchPaycheck(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Paycheck that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Paycheck localInstanceIn(EOEditingContext editingContext, Paycheck eo) {
    Paycheck localInstance = (eo == null) ? null : (Paycheck)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
