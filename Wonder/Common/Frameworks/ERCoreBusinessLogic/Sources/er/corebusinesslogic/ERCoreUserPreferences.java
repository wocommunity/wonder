/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import java.util.Enumeration;

public class ERCoreUserPreferences implements NSKeyValueCoding {

    /** Logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCoreUserPreferences.class);

    private final static String VALUE="_V";

    public final static String PreferenceDidChangeNotification = "PreferenceChangedNotification";

    private static ERCoreUserPreferences _userPreferences;
    public static ERCoreUserPreferences userPreferences() {
        if (_userPreferences == null)
            _userPreferences = new ERCoreUserPreferences();
        return _userPreferences;
    }

    /////////////////////////////////////////// Instance Methods /////////////////////////////////////////////
    // Here is where we register the handlers.
    public void registerHandlers() {
        log.debug("Registering preference handlers");
        _UserPreferenceHandler handler = new _UserPreferenceHandler();
        ERXRetainer.retain(handler);
        NSNotificationCenter.defaultCenter().addObserver(handler,
                                                         new NSSelector("handleBatchSizeChange", ERXConstant.NotificationClassArray),
                                                         ERXBatchNavigationBar.BatchSizeChanged,
                                                         null);
        NSNotificationCenter.defaultCenter().addObserver(handler,
                                                         new NSSelector("handleSortOrderingChange", ERXConstant.NotificationClassArray),
                                                         ERXSortOrder.SortOrderingChanged,
                                                         null);
        
    }
    
    public NSArray preferences() {
        ERCoreUserInterface user = (ERCoreUserInterface)ERCoreBusinessLogic.actor(preferencesEditingContext());
        return user!=null ? user.preferences() : ERXConstant.EmptyArray;
    }

    // FIXME: this EC will end up collecting a *lot* of stuff over the course of its life
    // a bunch of users and their prefs which have long since departed.
    private EOEditingContext _preferencesEditingContext;
    private EOEditingContext preferencesEditingContext() {
        if (_preferencesEditingContext==null)
            _preferencesEditingContext=ERXExtensions.newEditingContext();
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

    // FIXME -- unarchiving - archiving probably could use optimization
    public Object valueForKey(String key) {
        Object result=null;
        EOEnterpriseObject pref=preferenceRecordForKey(key);
        if (pref!=null) {
            String encodedValue=(String)pref.valueForKey("value");
            NSDictionary d=(NSDictionary )NSPropertyListSerialization.propertyListFromString(encodedValue);
            EOKeyValueUnarchiver u=new EOKeyValueUnarchiver(d);
            result=u.decodeObjectForKey(VALUE);
        }
        if (log.isDebugEnabled()) log.debug("Prefs vfk "+key+" = "+result);
        return result;
    }

    public String encodedValue(Object value) {
        EOKeyValueArchiver archiver=new EOKeyValueArchiver();
        archiver.encodeObject(value,VALUE);
        String encodedValue=NSPropertyListSerialization.stringFromPropertyList(archiver.dictionary());
        return encodedValue;
    }

    public void takeValueForKey(Object value, String key) {
        Object result=null;
        // we first make sure there is no cruft left
        // !! locking is turned off on the value attribute of UserPreference
        // so that if a user opens two sessions they don't get locking failures
        // this is OK for display style prefs (how many items, how they are sorted)
        // but might not be for more behavior-style prefs!!
        preferencesEditingContext().revert();
        EOEnterpriseObject pref=preferenceRecordForKey(key);
        ERCoreUserInterface u=(ERCoreUserInterface)ERCoreBusinessLogic.actor(preferencesEditingContext());
        if (pref!=null) {
            if (value!=null) {
                String encodedValue=encodedValue(value);
                if (ERXExtensions.safeDifferent(encodedValue,pref.valueForKey("value"))) {
                    if (log.isDebugEnabled())
                        log.debug("Updating preference "+u+": "+key+"="+encodedValue);
                    pref.takeValueForKey(encodedValue,"value");
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("Removing preference "+u+": "+key);
                pref.editingContext().deleteObject(pref);
            }
        } else if (value!=null) {
            pref=ERXUtilities.createEO("ERCPreference",
                                      preferencesEditingContext());
            u.newPreference(pref);
            // done this way to not force you to sub-class our User entity
            pref.takeValueForKey(ERXExtensions.rawPrimaryKeyForObject((EOEnterpriseObject)u),"userID");
            pref.takeValueForKey(key,"key");
            pref.takeValueForKey(encodedValue(value),"value");
            if (log.isDebugEnabled())
                log.debug("Creating preference "+u+": "+key+" - "+value+" -- "+encodedValue(value));
        }
        preferencesEditingContext().saveChanges();
        NSNotificationCenter.defaultCenter().postNotification(PreferenceDidChangeNotification, new NSDictionary(value,key));
    }

    public static class _UserPreferenceHandler {

        public void handleBatchSizeChange(NSNotification n) { handleChange("batchSize", n); }
        public void handleSortOrderingChange(NSNotification n) { handleChange("sortOrdering", n); }

        public void handleChange(String prefName, NSNotification n) {
            if (ERCoreBusinessLogic.actor() != null) {
                NSKeyValueCoding context=(NSKeyValueCoding)n.userInfo().objectForKey("d2wContext");
                if (context!=null && context.valueForKey("pageConfiguration") != null) {
                    userPreferences().takeValueForKey(n.object(),
                                                      prefName+"."+(String)context.valueForKey("pageConfiguration"));
                }
            }
        }
    }
    /*
    public static class _SortOrderingHandler {
        // this class handles the changes -- the other direction (from BL to UI) is handled in rules
        public void handleSortOrderingChange(NSNotification n) {
            if (ERCoreBusinessLogic.actor()!=null) {
                NSKeyValueCoding context=(NSKeyValueCoding)n.userInfo().objectForKey("d2wContext");
                WODisplayGroup dg=(WODisplayGroup)n.object();
                if (log.isDebugEnabled()) log.debug("handleSortOrderingChange "+context);
                if (context!=null) {
                    ERCoreUserPreferences.userPreferences().takeValueForKey(dg.sortOrderings(),
                                                                          ERXExtensions.userPreferencesKeyFromContext("sortOrdering", context));
                }
            }
        }
    }

    public static class _BatchSizeHandler {
        // this class handles the changes -- the other direction (from BL to UI) is handled in rules
        public void handleBatchSizeChange(NSNotification n) {
            if (ERCoreBusinessLogic.actor() != null) {
                NSKeyValueCoding context=(NSKeyValueCoding)n.userInfo().objectForKey("d2wContext");
                if (log.isDebugEnabled()) log.debug("handleBatchSizeChange "+context);
                if (context!=null) {
                    ERCoreUserPreferences.userPreferences().takeValueForKey(n.object(),
                                                                          ERXExtensions.userPreferencesKeyFromContext("batchSize", context));
                }
            }
        }
    }
     */
}
