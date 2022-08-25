// DO NOT EDIT.  Make changes to AppUser.java instead.
package webobjectsexamples.businesslogic.movies.common;

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
public abstract class _AppUser extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "AppUser";

  // Attribute Keys
  public static final ERXKey<String> USER_NAME = new ERXKey<String>("userName");
  // Relationship Keys

  // Attributes
  public static final String USER_NAME_KEY = USER_NAME.key();
  // Relationships

  private static Logger LOG = LoggerFactory.getLogger(_AppUser.class);

  public AppUser localInstanceIn(EOEditingContext editingContext) {
    AppUser localInstance = (AppUser)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String userName() {
    return (String) storedValueForKey(_AppUser.USER_NAME_KEY);
  }

  public void setUserName(String value) {
    if (_AppUser.LOG.isDebugEnabled()) {
    	_AppUser.LOG.debug( "updating userName from " + userName() + " to " + value);
    }
    takeStoredValueForKey(value, _AppUser.USER_NAME_KEY);
  }


  public static AppUser createAppUser(EOEditingContext editingContext, String userName
) {
    AppUser eo = (AppUser) EOUtilities.createAndInsertInstance(editingContext, _AppUser.ENTITY_NAME);    
		eo.setUserName(userName);
    return eo;
  }

  public static ERXFetchSpecification<AppUser> fetchSpec() {
    return new ERXFetchSpecification<AppUser>(_AppUser.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<AppUser> fetchAllAppUsers(EOEditingContext editingContext) {
    return _AppUser.fetchAllAppUsers(editingContext, null);
  }

  public static NSArray<AppUser> fetchAllAppUsers(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _AppUser.fetchAppUsers(editingContext, null, sortOrderings);
  }

  public static NSArray<AppUser> fetchAppUsers(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<AppUser> fetchSpec = new ERXFetchSpecification<AppUser>(_AppUser.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<AppUser> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static AppUser fetchAppUser(EOEditingContext editingContext, String keyName, Object value) {
    return _AppUser.fetchAppUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static AppUser fetchAppUser(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<AppUser> eoObjects = _AppUser.fetchAppUsers(editingContext, qualifier, null);
    AppUser eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one AppUser that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static AppUser fetchRequiredAppUser(EOEditingContext editingContext, String keyName, Object value) {
    return _AppUser.fetchRequiredAppUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static AppUser fetchRequiredAppUser(EOEditingContext editingContext, EOQualifier qualifier) {
    AppUser eoObject = _AppUser.fetchAppUser(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no AppUser that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static AppUser localInstanceIn(EOEditingContext editingContext, AppUser eo) {
    AppUser localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
