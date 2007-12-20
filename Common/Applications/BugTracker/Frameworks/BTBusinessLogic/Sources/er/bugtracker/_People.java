// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to People.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _People extends ERXGenericRecord {

	public static final String ENTITY_NAME = "People";

    public interface Key {
	// Attributes
	   public static final String EMAIL = "email";
	   public static final String IS_ACTIVE = "isActive";
	   public static final String IS_ADMIN = "isAdmin";
	   public static final String IS_CUSTOMER_SERVICE = "isCustomerService";
	   public static final String IS_ENGINEERING = "isEngineering";
	   public static final String LOGIN = "login";
	   public static final String NAME = "name";
	   public static final String PASSWORD = "password";

	// Relationships
    }

    public static class _PeopleClazz extends ERXGenericRecord.ERXGenericRecordClazz<People> {
        /* more clazz methods here */
    }

  public String email() {
    return (String) storedValueForKey(Key.EMAIL);
  }
  public void setEmail(String value) {
    takeStoredValueForKey(value, Key.EMAIL);
  }

  public Boolean isActive() {
    return (Boolean) storedValueForKey(Key.IS_ACTIVE);
  }
  public void setIsActive(Boolean value) {
    takeStoredValueForKey(value, Key.IS_ACTIVE);
  }

  public Boolean isAdmin() {
    return (Boolean) storedValueForKey(Key.IS_ADMIN);
  }
  public void setIsAdmin(Boolean value) {
    takeStoredValueForKey(value, Key.IS_ADMIN);
  }

  public Boolean isCustomerService() {
    return (Boolean) storedValueForKey(Key.IS_CUSTOMER_SERVICE);
  }
  public void setIsCustomerService(Boolean value) {
    takeStoredValueForKey(value, Key.IS_CUSTOMER_SERVICE);
  }

  public Boolean isEngineering() {
    return (Boolean) storedValueForKey(Key.IS_ENGINEERING);
  }
  public void setIsEngineering(Boolean value) {
    takeStoredValueForKey(value, Key.IS_ENGINEERING);
  }

  public String login() {
    return (String) storedValueForKey(Key.LOGIN);
  }
  public void setLogin(String value) {
    takeStoredValueForKey(value, Key.LOGIN);
  }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public String password() {
    return (String) storedValueForKey(Key.PASSWORD);
  }
  public void setPassword(String value) {
    takeStoredValueForKey(value, Key.PASSWORD);
  }

}
