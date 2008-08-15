// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Role.java instead.
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
public abstract class _Role extends er.extensions.eof.ERXGenericRecord {
	public static final String ENTITY_NAME = "Role";

	// Attributes

	// Relationships
	public static final String EMPLOYEES_KEY = "employees";
	public static final ERXKey<er.erxtest.model.Employee> EMPLOYEES = new ERXKey<er.erxtest.model.Employee>(EMPLOYEES_KEY);

  private static Logger LOG = Logger.getLogger(_Role.class);

  public Role localInstanceIn(EOEditingContext editingContext) {
    Role localInstance = (Role)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSArray<er.erxtest.model.Employee> employees() {
    return (NSArray<er.erxtest.model.Employee>)storedValueForKey("employees");
  }

  public NSArray<er.erxtest.model.Employee> employees(EOQualifier qualifier) {
    return employees(qualifier, null);
  }

  public NSArray<er.erxtest.model.Employee> employees(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.erxtest.model.Employee> results;
      results = employees();
      if (qualifier != null) {
        results = (NSArray<er.erxtest.model.Employee>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.erxtest.model.Employee>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToEmployees(er.erxtest.model.Employee object) {
    includeObjectIntoPropertyWithKey(object, "employees");
  }

  public void removeFromEmployees(er.erxtest.model.Employee object) {
    excludeObjectFromPropertyWithKey(object, "employees");
  }

  public void addToEmployeesRelationship(er.erxtest.model.Employee object) {
    if (_Role.LOG.isDebugEnabled()) {
      _Role.LOG.debug("adding " + object + " to employees relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToEmployees(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "employees");
    }
  }

  public void removeFromEmployeesRelationship(er.erxtest.model.Employee object) {
    if (_Role.LOG.isDebugEnabled()) {
      _Role.LOG.debug("removing " + object + " from employees relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromEmployees(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "employees");
    }
  }

  public er.erxtest.model.Employee createEmployeesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Employee");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "employees");
    return (er.erxtest.model.Employee) eo;
  }

  public void deleteEmployeesRelationship(er.erxtest.model.Employee object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "employees");
    editingContext().deleteObject(object);
  }

  public void deleteAllEmployeesRelationships() {
    Enumeration objects = employees().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteEmployeesRelationship((er.erxtest.model.Employee)objects.nextElement());
    }
  }


  public static Role createRole(EOEditingContext editingContext) {
    Role eo = (Role) EOUtilities.createAndInsertInstance(editingContext, _Role.ENTITY_NAME);    
    return eo;
  }

  public static NSArray<Role> fetchAllRoles(EOEditingContext editingContext) {
    return _Role.fetchAllRoles(editingContext, null);
  }

  public static NSArray<Role> fetchAllRoles(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Role.fetchRoles(editingContext, null, sortOrderings);
  }

  public static NSArray<Role> fetchRoles(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Role.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Role> eoObjects = (NSArray<Role>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Role fetchRole(EOEditingContext editingContext, String keyName, Object value) {
    return _Role.fetchRole(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Role fetchRole(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Role> eoObjects = _Role.fetchRoles(editingContext, qualifier, null);
    Role eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Role)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Role that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Role fetchRequiredRole(EOEditingContext editingContext, String keyName, Object value) {
    return _Role.fetchRequiredRole(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Role fetchRequiredRole(EOEditingContext editingContext, EOQualifier qualifier) {
    Role eoObject = _Role.fetchRole(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Role that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Role localInstanceIn(EOEditingContext editingContext, Role eo) {
    Role localInstance = (eo == null) ? null : (Role)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
