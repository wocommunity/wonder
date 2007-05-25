// _People.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to People.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _People extends ERXGenericRecord {

    public interface Key  {
        public static final String PASSWORD = "password";
        public static final String NAME = "name";
        public static final String LOGIN = "login";
        public static final String IS_ENGINEERING = "isEngineering";
        public static final String IS_CUSTOMER_SERVICE = "isCustomerService";
        public static final String IS_ADMIN = "isAdmin";
        public static final String IS_ACTIVE = "isActive";
        public static final String EMAIL = "email";  
    }

    public static abstract class _PeopleClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray objectsForActiveUsers(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("activeUsers", "People");

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForCanLoginAsAdmin(EOEditingContext context, String passwordBinding, String usernameBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("canLoginAsAdmin", "People");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (passwordBinding != null)
                bindings.setObjectForKey(passwordBinding, "password");
            if (usernameBinding != null)
                bindings.setObjectForKey(usernameBinding, "username");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForLogin(EOEditingContext context, String passwordBinding, String usernameBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("login", "People");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (passwordBinding != null)
                bindings.setObjectForKey(passwordBinding, "password");
            if (usernameBinding != null)
                bindings.setObjectForKey(usernameBinding, "username");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String email() {
        return (String)storedValueForKey(Key.EMAIL);
    }
    public void setEmail(String aValue) {
        takeStoredValueForKey(aValue, Key.EMAIL);
    }

    public boolean isActive() {
        return ((Boolean)storedValueForKey(Key.IS_ACTIVE)).booleanValue();
    }
    public void setIsActive(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_ACTIVE);
    }

    public boolean isAdmin() {
        return ((Boolean)storedValueForKey(Key.IS_ADMIN)).booleanValue();
    }
    public void setIsAdmin(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_ADMIN);
    }

    public boolean isCustomerService() {
        return ((Boolean)storedValueForKey(Key.IS_CUSTOMER_SERVICE)).booleanValue();
    }
    public void setIsCustomerService(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_CUSTOMER_SERVICE);
    }

    public boolean isEngineering() {
        return ((Boolean)storedValueForKey(Key.IS_ENGINEERING)).booleanValue();
    }
    public void setIsEngineering(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_ENGINEERING);
    }

    public String login() {
        return (String)storedValueForKey(Key.LOGIN);
    }
    public void setLogin(String aValue) {
        takeStoredValueForKey(aValue, Key.LOGIN);
    }

    public String name() {
        return (String)storedValueForKey(Key.NAME);
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, Key.NAME);
    }

    public String password() {
        return (String)storedValueForKey(Key.PASSWORD);
    }
    public void setPassword(String aValue) {
        takeStoredValueForKey(aValue, Key.PASSWORD);
    }
}
