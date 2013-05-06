// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Partial_EmployeePerson.java instead.
/** Partial template to fix relationships */
package er.example.erxpartials.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Partial_EmployeePerson extends er.extensions.partials.ERXPartial<er.example.erxpartials.model.Person> {
  public static final String ENTITY_NAME = "Person";

  // Attribute Keys
  public static final ERXKey<String> EMPLOYEE_NUMBER = new ERXKey<String>("employeeNumber");
  public static final ERXKey<java.math.BigDecimal> SALARY = new ERXKey<java.math.BigDecimal>("salary");
  // Relationship Keys
  public static final ERXKey<er.example.erxpartials.model.Department> DEPARTMENT = new ERXKey<er.example.erxpartials.model.Department>("department");
  public static final ERXKey<er.example.erxpartials.model.EmployeeType> EMPLOYEE_TYPE = new ERXKey<er.example.erxpartials.model.EmployeeType>("employeeType");

  // Attributes
  public static final String EMPLOYEE_NUMBER_KEY = EMPLOYEE_NUMBER.key();
  public static final String SALARY_KEY = SALARY.key();
  // Relationships
  public static final String DEPARTMENT_KEY = DEPARTMENT.key();
  public static final String EMPLOYEE_TYPE_KEY = EMPLOYEE_TYPE.key();

	public static NSArray<String> _partialAttributes = null;
	public static NSArray<String> _partialRelationships = null;
	
	public static NSArray<String> partialAttributes() {
		if ( _partialAttributes == null ) {
			synchronized(ENTITY_NAME) {
				NSMutableArray<String> partialList = new NSMutableArray<String>();
				partialList.addObject( EMPLOYEE_NUMBER_KEY );
				partialList.addObject( SALARY_KEY );
				_partialAttributes = partialList.immutableClone();
			}
		}
		return _partialAttributes;
	}

	public static NSArray<String> partialRelationships() {
		if ( _partialRelationships == null ) {
			synchronized(ENTITY_NAME) {
				NSMutableArray<String> partialList = new NSMutableArray<String>();
				partialList.addObject( DEPARTMENT_KEY );
				partialList.addObject( EMPLOYEE_TYPE_KEY );
				_partialRelationships = partialList.immutableClone();
			}
		}
		return _partialRelationships;
	}

  private static Logger LOG = Logger.getLogger(_Partial_EmployeePerson.class);

  public String employeeNumber() {
    return (String) storedValueForKey("employeeNumber");
  }

  public void setEmployeeNumber(String value) {
    if (_Partial_EmployeePerson.LOG.isDebugEnabled()) {
    	_Partial_EmployeePerson.LOG.debug( "updating employeeNumber from " + employeeNumber() + " to " + value);
    }
    takeStoredValueForKey(value, "employeeNumber");
  }

  public java.math.BigDecimal salary() {
    return (java.math.BigDecimal) storedValueForKey("salary");
  }

  public void setSalary(java.math.BigDecimal value) {
    if (_Partial_EmployeePerson.LOG.isDebugEnabled()) {
    	_Partial_EmployeePerson.LOG.debug( "updating salary from " + salary() + " to " + value);
    }
    takeStoredValueForKey(value, "salary");
  }

  public er.example.erxpartials.model.Department department() {
    return (er.example.erxpartials.model.Department)storedValueForKey("department");
  }
  
  public void setDepartment(er.example.erxpartials.model.Department value) {
    takeStoredValueForKey(value, "department");
  }

  public void setDepartmentRelationship(er.example.erxpartials.model.Department value) {
    if (_Partial_EmployeePerson.LOG.isDebugEnabled()) {
      _Partial_EmployeePerson.LOG.debug("updating department from " + department() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setDepartment(value);
    }
    else if (value == null) {
    	er.example.erxpartials.model.Department oldValue = department();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "department");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "department");
    }
  }
  
  public er.example.erxpartials.model.EmployeeType employeeType() {
    return (er.example.erxpartials.model.EmployeeType)storedValueForKey("employeeType");
  }
  
  public void setEmployeeType(er.example.erxpartials.model.EmployeeType value) {
    takeStoredValueForKey(value, "employeeType");
  }

  public void setEmployeeTypeRelationship(er.example.erxpartials.model.EmployeeType value) {
    if (_Partial_EmployeePerson.LOG.isDebugEnabled()) {
      _Partial_EmployeePerson.LOG.debug("updating employeeType from " + employeeType() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setEmployeeType(value);
    }
    else if (value == null) {
    	er.example.erxpartials.model.EmployeeType oldValue = employeeType();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "employeeType");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "employeeType");
    }
  }
  

  public Partial_EmployeePerson initPartial_EmployeePerson(EOEditingContext editingContext) {
    Partial_EmployeePerson eo = (Partial_EmployeePerson)this;    
    return eo;
  }
}
