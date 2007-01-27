// _Paycheck.java
// DO NOT EDIT.  Make changes to Paycheck.java instead.

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _Paycheck extends EOGenericRecord {
	public static final String ENTITY_NAME = "Paycheck";

	public static final String AMOUNT_KEY = "amount";
	public static final String CASHED_KEY = "cashed";
	public static final String PAYMENT_DATE_KEY = "paymentDate";

	public static final String EMPLOYEE_KEY = "employee";

	private static Logger LOG = Logger.getLogger(_Paycheck.class);

	public _Paycheck() {
		super();
	}

	public Paycheck localInstanceOfPaycheck(EOEditingContext editingContext) {
		return (Paycheck)EOUtilities.localInstanceOfObject(editingContext, this);
	}


	public Number amount() {
		return (Number) storedValueForKey("amount");
	}

	public void setAmount(Number aValue) {
		if (_Paycheck.LOG.isDebugEnabled()) {
			_Paycheck.LOG.debug( "updating amount from "+amount()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "amount");
	}

	public java.lang.Boolean cashed() {
		return (java.lang.Boolean) storedValueForKey("cashed");
	}

	public void setCashed(java.lang.Boolean aValue) {
		if (_Paycheck.LOG.isDebugEnabled()) {
			_Paycheck.LOG.debug( "updating cashed from "+cashed()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "cashed");
	}

	public NSTimestamp paymentDate() {
		return (NSTimestamp) storedValueForKey("paymentDate");
	}

	public void setPaymentDate(NSTimestamp aValue) {
		if (_Paycheck.LOG.isDebugEnabled()) {
			_Paycheck.LOG.debug( "updating paymentDate from "+paymentDate()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "paymentDate");
	}

	public Employee employee() {
		return (Employee)storedValueForKey("employee");
	}

	public void setEmployeeRelationship(Employee aValue) {
		if (_Paycheck.LOG.isDebugEnabled()) {
			_Paycheck.LOG.debug("updating employee from " + employee() + " to " + aValue);
		}
		if (aValue == null) {
			Employee object = employee();
			if (object != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(object, "employee");
			}
		} else {
	    		addObjectToBothSidesOfRelationshipWithKey(aValue, "employee");
		}
	}

	public static Paycheck createPaycheck(EOEditingContext editingContext, Number amount, java.lang.Boolean cashed, NSTimestamp paymentDate, Employee employee) {
		Paycheck eoObject = (Paycheck)EOUtilities.createAndInsertInstance(editingContext, _Paycheck.ENTITY_NAME);
		eoObject.setAmount(amount);
		eoObject.setCashed(cashed);
		eoObject.setPaymentDate(paymentDate);
		eoObject.setEmployeeRelationship(employee);
		return eoObject;
	}

	public static NSArray fetchAllPaychecks(EOEditingContext editingContext) {
		return _Paycheck.fetchAllPaychecks(editingContext, null);
	}

	public static NSArray fetchAllPaychecks(EOEditingContext editingContext, NSArray sortOrderings) {
		return _Paycheck.fetchPaychecks(editingContext, null, sortOrderings);
	}

	public static NSArray fetchPaychecks(EOEditingContext editingContext, EOQualifier qualifier, NSArray sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Paycheck.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray eoObjects = editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	public static Paycheck fetchPaycheck(EOEditingContext editingContext, String keyName, Object value) {
		return _Paycheck.fetchPaycheck(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Paycheck fetchPaycheck(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray eoObjects = _Paycheck.fetchPaychecks(editingContext, qualifier, null);
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

	public static Paycheck localInstanceOfPaycheck(EOEditingContext editingContext, Paycheck eo) {
		return (eo == null) ? null : (Paycheck)EOUtilities.localInstanceOfObject(editingContext, eo);
	}

}
