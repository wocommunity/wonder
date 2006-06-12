/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.wrox;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXBatchNavigationBar;
import er.extensions.ERXConstant;
import er.extensions.ERXCrypto;
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
import er.extensions.ERXGenericRecord;
import er.extensions.ERXRetainer;
import er.extensions.ERXSortOrder;
import er.extensions.ERXUtilities;

public class User extends ERXGenericRecord {

    //////* logging support */////////////////
    public static final Logger log = Logger.getLogger(User.class);
    public static final Logger userPrefLog = Logger.getLogger("er.wrox.User.Preferences");

    ///////////////////////////////////////////// Static Methods ////////////////////////////////////////////////////
    // Full MT implementation
    private static NSMutableDictionary _actorsPerThread=new NSMutableDictionary();
    public synchronized static void setActor(User actor) {
        Object key=Thread.currentThread().getName();
        if (actor != null)
            _actorsPerThread.setObjectForKey(actor,key);
        else
            _actorsPerThread.removeObjectForKey(key);
    }

    public synchronized static User actor() {
        Object key=Thread.currentThread().getName();
        return (User)_actorsPerThread.objectForKey(key);
    }

    public synchronized static User actor(EOEditingContext ec) {
        User result = actor();
        if (result!=null && result.editingContext() != ec)
            result=(User)ERXUtilities.localInstanceOfObject(ec, result);
        return result;
    }

    // Preferences handling
    private static NSKeyValueCoding _userPreferences;
    public static NSKeyValueCoding userPreferences() {
        if (_userPreferences==null)
            _userPreferences=new _UserPreferences();
        return _userPreferences;
    }

    public static void registerUserPreferenceHandler() {
        _UserPreferenceHandler uph = new _UserPreferenceHandler();
        ERXRetainer.retain(uph);
        NSNotificationCenter.defaultCenter().addObserver(uph,
                                                         new NSSelector("handleSortOrderingChange", ERXConstant.NotificationClassArray),
                                                         ERXSortOrder.SortOrderingChanged,
                                                         null);
        NSNotificationCenter.defaultCenter().addObserver(uph,
                                                         new NSSelector("handleBatchSizeChange", ERXConstant.NotificationClassArray),
                                                         ERXBatchNavigationBar.BatchSizeChanged,
                                                         null);
    } 
    
    public static class _UserPreferences implements NSKeyValueCoding {

        private final static String VALUE="_V";

        public NSArray preferences() {
            User user = User.actor(preferencesEditingContext());
            return user != null ? user.preferences() : ERXConstant.EmptyArray;
        }

        private EOEditingContext _preferencesEditingContext;
        private EOEditingContext preferencesEditingContext() {
            if (_preferencesEditingContext==null)
                _preferencesEditingContext = ERXEC.newEditingContext();
            return _preferencesEditingContext;
        }

        private EOEnterpriseObject preferenceRecordForKey(String key) {
            EOEnterpriseObject result=null;
            if (key!=null) {
                for (Enumeration e=preferences().objectEnumerator(); e.hasMoreElements();) {
                    EOEnterpriseObject pref=(EOEnterpriseObject)e.nextElement();
                    String prefKey=(String)pref.valueForKey("key");
                    if (prefKey!=null && prefKey.equals(key)) {
                        result=pref;
                        break;
                    }
                }
            }
            return result;
        }

        public Object valueForKey(String key) {
            Object result=null;
            userPrefLog.debug("VFK, key: " + key);
            EOEnterpriseObject pref=preferenceRecordForKey(key);
            if (pref!=null) {
                String encodedValue=(String)pref.valueForKey("value");
                NSDictionary d=(NSDictionary)NSPropertyListSerialization.propertyListFromString(encodedValue);
                userPrefLog.debug("Decoded dictionary: " + d + " value: " + result);
                EOKeyValueUnarchiver u=new EOKeyValueUnarchiver(d);
                result=u.decodeObjectForKey(VALUE);
            }
            return result;
        }

        public String encodedValue(Object value) {
            EOKeyValueArchiver archiver=new EOKeyValueArchiver();
            archiver.encodeObject(value,VALUE);
            String encodedValue=NSPropertyListSerialization.stringFromPropertyList(archiver.dictionary());
            if (userPrefLog.isDebugEnabled())
                userPrefLog.debug("Encoded value: " + value + " into dic: "
                     + archiver.dictionary() + " value: " + encodedValue);
            return encodedValue;
        }

        public void takeValueForKey(Object value, String key) {
            Object result=null;
            // we first make sure there is no cruft left
            // !! locking is turned off on the value attribute of UserPreference
            // so that if a user opens two sessions they don't get locking failures
            // this is OK for display style prefs (how many items, how they are sorted)
            // but might not be for more behavior-style prefs!!
            userPrefLog.debug("TVFK, key: " + key + " value: " + value);
            preferencesEditingContext().revert();
            EOEnterpriseObject pref=preferenceRecordForKey(key);
            User u=User.actor(preferencesEditingContext());
            if (pref!=null) {
                if (value!=null) {
                    String encodedValue=encodedValue(value);
                    log.debug("Encoded value string: " + encodedValue);
                    if (ERXExtensions.safeDifferent(encodedValue,pref.valueForKey("value"))) {
                        if (userPrefLog.isDebugEnabled())
                            userPrefLog.debug("Updating preference "+u+": "+key+"="+encodedValue);
                        pref.takeValueForKey(encodedValue,"value");
                    }
                } else {
                    if (userPrefLog.isDebugEnabled())
                        userPrefLog.debug("Removing preference "+u+": "+key);
                    pref.removeObjectFromBothSidesOfRelationshipWithKey(u,"user");
                }
            } else if (value!=null) {
                pref=ERXUtilities.createEOLinkedToEO("Preference",
                                                        preferencesEditingContext(),
                                                        "preferences",
                                                        u);
                pref.takeValueForKey(key,"key");
                pref.takeValueForKey(encodedValue(value),"value");
                if (userPrefLog.isDebugEnabled())
                    userPrefLog.debug("Creating preference "+u+": "+key + " pref: " + pref);
            }
            if (preferencesEditingContext().hasChanges())
                preferencesEditingContext().saveChanges();
        }
    }

    public static class _UserPreferenceHandler {
        public void handleBatchSizeChange(NSNotification n) { handleChange("batchSize", n); }
        public void handleSortOrderingChange(NSNotification n) { handleChange("sortOrdering", n); }

        public void handleChange(String prefName, NSNotification n) {
            if (User.actor() != null) {
                NSKeyValueCoding context=(NSKeyValueCoding)n.userInfo().objectForKey("d2wContext");
                if (context!=null && context.valueForKey("pageConfiguration") != null) {
                    userPreferences().takeValueForKey(n.object(),
                                                      prefName+"."+(String)context.valueForKey("pageConfiguration"));
                }
            }
        }
    }
    
    ///////////////////////////////////////////// Instance Methods ////////////////////////////////////////////////////
    public String fullName() { return firstName() + " " + lastName(); }
    public String primaryKey() { return  primaryKeyInTransaction(); }
    
    // Optimization for checking login names.
    public boolean _shouldValidateUsername=false;
    public String _validatedUsername=null;
    
    public void setUsername(String value) {
        if (ERXExtensions.safeDifferent(value, username())) {
            if (_validatedUsername==null ||
                ERXExtensions.safeDifferent(value, _validatedUsername)) {
                _shouldValidateUsername=true;
            }
            takeStoredValueForKey(value, "username");
            _validatedUsername=null;
        }
    }    
    ///////////////////////////////////////////// Validation Methods //////////////////////////////////////////////////
    public static Object validateForIllegalCharacters (String anyString, String propertyName) {
        if (anyString != null && (anyString.indexOf('*')!=-1 || anyString.indexOf('%')!=-1 || anyString.indexOf(' ')!=-1))
            throw new NSValidation.ValidationException("Please don't use *, % or ' ' characters in <b>"+propertyName+"</b>.");
        return null;
    }
    
    public String validateUsername(String newUsername) throws NSValidation.ValidationException {
        log.debug("Validating username: " + newUsername);
        validateForIllegalCharacters(newUsername,"usernames");
        if (_shouldValidateUsername || ERXExtensions.safeDifferent(newUsername, username())) {
            boolean found=false;
            if (!found) {
                NSDictionary usernameBindings=new NSDictionary(newUsername,"username");
                // This code is not foolproof, because there is a time lag between check and commit!
                // If the database supports unique constraints should probablly use one just in case.
                NSArray usersWithSameUsernameInDB=EOUtilities.objectsWithFetchSpecificationAndBindings(editingContext(),
                                                                                                       "User",
                                                                                                       "RawRowsCheckUsername",
                                                                                                       usernameBindings);
                found=usersWithSameUsernameInDB.count() > 1 ||
                    usersWithSameUsernameInDB.count()==1 &&
                    ERXExtensions.safeDifferent(primaryKey(),
                                               ((NSDictionary)usersWithSameUsernameInDB.objectAtIndex(0)).objectForKey("id").toString());
            }
            if (found)
                throw new NSValidation.ValidationException("Sorry, the username <b>'"+newUsername+"'</b> is already in use.");
            _shouldValidateUsername=false;
            _validatedUsername=newUsername;
        }
        return newUsername;
    }

    public String validatePassword(String newPassword) throws NSValidation.ValidationException {
        if (newPassword !=null && newPassword.equals(username()))
            throw new NSValidation.ValidationException("For your security, your <b>password</b> should not be the same as your <b>username</b>.");
        else if (newPassword == null || newPassword.length() < 4)
            throw new NSValidation.ValidationException("For your security, your <b>password</b> should be at least four characters.");
        return newPassword;
    }
    
    ///////////////////////////////////////////// Accessor Methods ////////////////////////////////////////////////////
    public String firstName() { return (String)storedValueForKey("firstName"); }
    public String lastName() { return (String)storedValueForKey("lastName"); }
    public String username() { return (String)storedValueForKey("username"); }
    public String password() { return encryptedPassword(); }
    public void setPassword(String value) { takeValueForKey(ERXCrypto.shaEncode(value), "encryptedPassword"); }

    public String encryptedPassword() { return (String)storedValueForKey("encryptedPassword"); }
    public NSArray preferences() { return (NSArray)storedValueForKey("preferences"); }
}
