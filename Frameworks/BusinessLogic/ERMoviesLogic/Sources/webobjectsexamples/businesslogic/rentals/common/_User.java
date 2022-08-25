// DO NOT EDIT.  Make changes to User.java instead.
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
public abstract class _User extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "User";

  // Attribute Keys
  public static final ERXKey<Integer> ACCESS_LEVEL = new ERXKey<Integer>("accessLevel");
  public static final ERXKey<String> PASSWORD = new ERXKey<String>("password");
  public static final ERXKey<String> USERNAME = new ERXKey<String>("username");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.rentals.common.Customer> CUSTOMER = new ERXKey<webobjectsexamples.businesslogic.rentals.common.Customer>("customer");

  // Attributes
  public static final String ACCESS_LEVEL_KEY = ACCESS_LEVEL.key();
  public static final String PASSWORD_KEY = PASSWORD.key();
  public static final String USERNAME_KEY = USERNAME.key();
  // Relationships
  public static final String CUSTOMER_KEY = CUSTOMER.key();

  private static Logger LOG = LoggerFactory.getLogger(_User.class);

  public User localInstanceIn(EOEditingContext editingContext) {
    User localInstance = (User)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Integer accessLevel() {
    return (Integer) storedValueForKey(_User.ACCESS_LEVEL_KEY);
  }

  public void setAccessLevel(Integer value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating accessLevel from " + accessLevel() + " to " + value);
    }
    takeStoredValueForKey(value, _User.ACCESS_LEVEL_KEY);
  }

  public String password() {
    return (String) storedValueForKey(_User.PASSWORD_KEY);
  }

  public void setPassword(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating password from " + password() + " to " + value);
    }
    takeStoredValueForKey(value, _User.PASSWORD_KEY);
  }

  public String username() {
    return (String) storedValueForKey(_User.USERNAME_KEY);
  }

  public void setUsername(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating username from " + username() + " to " + value);
    }
    takeStoredValueForKey(value, _User.USERNAME_KEY);
  }

  public webobjectsexamples.businesslogic.rentals.common.Customer customer() {
    return (webobjectsexamples.businesslogic.rentals.common.Customer)storedValueForKey(_User.CUSTOMER_KEY);
  }
  
  public void setCustomer(webobjectsexamples.businesslogic.rentals.common.Customer value) {
    takeStoredValueForKey(value, _User.CUSTOMER_KEY);
  }

  public void setCustomerRelationship(webobjectsexamples.businesslogic.rentals.common.Customer value) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("updating customer from " + customer() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCustomer(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.rentals.common.Customer oldValue = customer();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _User.CUSTOMER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _User.CUSTOMER_KEY);
    }
  }
  

  public static User createUser(EOEditingContext editingContext, Integer accessLevel
, String password
, String username
) {
    User eo = (User) EOUtilities.createAndInsertInstance(editingContext, _User.ENTITY_NAME);    
		eo.setAccessLevel(accessLevel);
		eo.setPassword(password);
		eo.setUsername(username);
    return eo;
  }

  public static ERXFetchSpecification<User> fetchSpec() {
    return new ERXFetchSpecification<User>(_User.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<User> fetchAllUsers(EOEditingContext editingContext) {
    return _User.fetchAllUsers(editingContext, null);
  }

  public static NSArray<User> fetchAllUsers(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _User.fetchUsers(editingContext, null, sortOrderings);
  }

  public static NSArray<User> fetchUsers(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<User> fetchSpec = new ERXFetchSpecification<User>(_User.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<User> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static User fetchUser(EOEditingContext editingContext, String keyName, Object value) {
    return _User.fetchUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static User fetchUser(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<User> eoObjects = _User.fetchUsers(editingContext, qualifier, null);
    User eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one User that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static User fetchRequiredUser(EOEditingContext editingContext, String keyName, Object value) {
    return _User.fetchRequiredUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static User fetchRequiredUser(EOEditingContext editingContext, EOQualifier qualifier) {
    User eoObject = _User.fetchUser(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no User that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static User localInstanceIn(EOEditingContext editingContext, User eo) {
    User localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
  public static NSArray<webobjectsexamples.businesslogic.rentals.common.User> fetchLogin(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("login", _User.ENTITY_NAME);
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.User>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<webobjectsexamples.businesslogic.rentals.common.User> fetchLogin(EOEditingContext editingContext,
	String passwordBinding,
	String userBinding)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("login", _User.ENTITY_NAME);
    NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
    bindings.takeValueForKey(passwordBinding, "password");
    bindings.takeValueForKey(userBinding, "user");
	fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.rentals.common.User>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
}
