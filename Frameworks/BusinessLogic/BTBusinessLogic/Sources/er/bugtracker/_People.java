// _People.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to People.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXGenericRecord;

public abstract class _People extends ERXGenericRecord {

    public _People() {
        super();
    }

    public static abstract class _PeopleClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray activeUsers(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "People", "activeUsers", null);
        }

        public NSArray canLoginAsAdminWithUsernamePassword(EOEditingContext ec, Object username, Object password) {
            NSMutableDictionary _dict = new NSMutableDictionary(3);
            
            if(username != null) _dict.setObjectForKey( username, "username");
            if(password != null) _dict.setObjectForKey( password, "password");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "People", "canLoginAsAdmin", _dict);
        }

        public NSArray loginWithUsernamePassword(EOEditingContext ec, Object username, Object password) {
            NSMutableDictionary _dict = new NSMutableDictionary(3);
            
            if(username != null) _dict.setObjectForKey( username, "username");
            if(password != null) _dict.setObjectForKey( password, "password");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "People", "login", _dict);
        }

    }


    public String login() {
        return (String)storedValueForKey("login");
    }
    public void setLogin(String aValue) {
        takeStoredValueForKey(aValue, "login");
    }

    public String name() {
        return (String)storedValueForKey("name");
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, "name");
    }

    public String password() {
        return (String)storedValueForKey("password");
    }
    public void setPassword(String aValue) {
        takeStoredValueForKey(aValue, "password");
    }

    public Number isAdmin() {
        return (Number)storedValueForKey("isAdmin");
    }
    public void setIsAdmin(Number aValue) {
        takeStoredValueForKey(aValue, "isAdmin");
    }

    public String email() {
        return (String)storedValueForKey("email");
    }
    public void setEmail(String aValue) {
        takeStoredValueForKey(aValue, "email");
    }

    public Number isEngineering() {
        return (Number)storedValueForKey("isEngineering");
    }
    public void setIsEngineering(Number aValue) {
        takeStoredValueForKey(aValue, "isEngineering");
    }

    public Number isCustomerService() {
        return (Number)storedValueForKey("isCustomerService");
    }
    public void setIsCustomerService(Number aValue) {
        takeStoredValueForKey(aValue, "isCustomerService");
    }

    public Number isActive() {
        return (Number)storedValueForKey("isActive");
    }
    public void setIsActive(Number aValue) {
        takeStoredValueForKey(aValue, "isActive");
    }

    public NSArray bugs() {
        return (NSArray)storedValueForKey("bugs");
    }
    public void setBugs(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "bugs");
    }
    public void addToBugs(Bug object) {
        NSMutableArray array = (NSMutableArray)bugs();

        willChange();
        array.addObject(object);
    }
    public void removeFromBugs(Bug object) {
        NSMutableArray array = (NSMutableArray)bugs();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfBugs(Bug object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "bugs");
    }
    public void removeFromBothSidesOfBugs(Bug object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "bugs");
    }


    public NSArray requirements() {
        return (NSArray)storedValueForKey("requirements");
    }
    public void setRequirements(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "requirements");
    }
    public void addToRequirements(Requirement object) {
        NSMutableArray array = (NSMutableArray)requirements();

        willChange();
        array.addObject(object);
    }
    public void removeFromRequirements(Requirement object) {
        NSMutableArray array = (NSMutableArray)requirements();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfRequirements(Requirement object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirements");
    }
    public void removeFromBothSidesOfRequirements(Requirement object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirements");
    }


    public NSArray preferences() {
        return (NSArray)storedValueForKey("preferences");
    }
    public void setPreferences(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "preferences");
    }
    public void addToPreferences(er.corebusinesslogic.ERCPreference object) {
        NSMutableArray array = (NSMutableArray)preferences();

        willChange();
        array.addObject(object);
    }
    public void removeFromPreferences(er.corebusinesslogic.ERCPreference object) {
        NSMutableArray array = (NSMutableArray)preferences();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfPreferences(er.corebusinesslogic.ERCPreference object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "preferences");
    }
    public void removeFromBothSidesOfPreferences(er.corebusinesslogic.ERCPreference object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "preferences");
    }

}
