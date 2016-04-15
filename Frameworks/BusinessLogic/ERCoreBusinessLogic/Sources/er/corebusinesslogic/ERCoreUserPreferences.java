/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import java.util.Enumeration;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;

import er.extensions.batching.ERXBatchNavigationBar;
import er.extensions.components.ERXSortOrder;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRetainer;
import er.extensions.foundation.ERXValueUtilities;

/**
 *
 * @property er.corebusinesslogic.ERCoreUserPreferences.handlerClassName
 */
public class ERCoreUserPreferences implements NSKeyValueCoding {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------    
    
    private static final Logger log = LoggerFactory.getLogger(ERCoreUserPreferences.class);

    /** EOEncoding key */
    private final static String VALUE="_V";

    /** Notification that is posted when preferences change */
    public final static String PreferenceDidChangeNotification = "PreferenceChangedNotification";

    //	===========================================================================
    //	Class Variable(s)
    //	---------------------------------------------------------------------------    

    /** caches the singleton user preference object */
    private static ERCoreUserPreferences _userPreferences;

    //	===========================================================================
    //	Class Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Gets the singleton instance for interacting with
     * the user preference system.
     * @return single instance of the user preferences
     */
    public static ERCoreUserPreferences userPreferences() {
        if (_userPreferences == null) {
        	_userPreferences = new ERCoreUserPreferences();
        }
        return _userPreferences;
    }
    
    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------    

    /**
     * Registers notification handlers for user preference notifications. These
     * are mainly used within the context of D2W pages.
     */
    public void registerHandlers() {
        log.debug("Registering preference handlers");
        Object handler = null;
        String handlerClassName = ERXProperties.stringForKey("er.corebusinesslogic.ERCoreUserPreferences.handlerClassName");
        if (handlerClassName != null) {
          try {
            handler = Class.forName(handlerClassName).newInstance();
          }
          catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
          }
        }
        if (handler == null) {
        	handler = new _UserPreferenceHandler();
        }
        ERXRetainer.retain(handler);        
    }
    
    protected NSArray preferences(EOEditingContext ec) {
        ERCoreUserInterface user = (ERCoreUserInterface)ERCoreBusinessLogic.actor(ec);
        return user!=null ? user.preferences() : NSArray.EmptyArray;
    }

    protected EOEnterpriseObject preferenceRecordForKey(String key, EOEditingContext ec) {
        EOEnterpriseObject result=null;
        if (key != null) {
            log.debug("Preference value for key = {}", key);
            for (Enumeration e = preferences(ec).objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject pref = (EOEnterpriseObject)e.nextElement();
                String prefKey = (String)pref.valueForKey("key");
                log.debug("prefKey '{}'", prefKey);
                if (prefKey != null && prefKey.equals(key)) {
                    result = pref;
                    break;
                }
            }
        }
        return result;
    }

    protected String encodedValue(Object value) {
        EOKeyValueArchiver archiver = new EOKeyValueArchiver();
        archiver.encodeObject(value,VALUE);
        String encodedValue = NSPropertyListSerialization.stringFromPropertyList(archiver.dictionary());
        return encodedValue;
    }

    protected Object decodedValue(String encodedValue) {
        NSDictionary d = (NSDictionary )NSPropertyListSerialization.propertyListFromString(encodedValue);
        EOKeyValueUnarchiver u = new EOKeyValueUnarchiver(d);
        return u.decodeObjectForKey(VALUE);
    }    
    
    //	===========================================================================
    //	Implementation of NSKeyValueCoding
    //	---------------------------------------------------------------------------    
    
    // FIXME -- unarchiving - archiving probably could use optimization
    public Object valueForKey(String key) {
        Object result=null;
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EOEnterpriseObject pref = preferenceRecordForKey(key, ec);
            if (pref != null) {
                String encodedValue = (String)pref.valueForKey("value");
                if(encodedValue !=null) {
                    result = decodedValue(encodedValue);
                }
            }
        } catch(RuntimeException ex) {
        	log.error("Error while getting preference {}", key, ex);
        } finally {
            ec.unlock();
        }
        ec.dispose();
        log.debug("Prefs vfk {} = {}", key, result);
        return result;
    }

    public void takeValueForKey(Object value, String key) {
        // we first make sure there is no cruft left
        // !! locking is turned off on the value attribute of UserPreference
        // so that if a user opens two sessions they don't get locking failures
        // this is OK for display style prefs (how many items, how they are sorted)
        // but might not be for more behavior-style prefs!!
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EOEnterpriseObject pref = preferenceRecordForKey(key, ec);
            ERCoreUserInterface u = (ERCoreUserInterface)ERCoreBusinessLogic.actor(ec);
            if (pref != null) {
                if (value != null) {
                    String encodedValue = encodedValue(value);
                    if (ObjectUtils.notEqual(encodedValue, pref.valueForKey("value"))) {
                        log.debug("Updating preference {}: {}={}", u, key, encodedValue);
                        pref.takeValueForKey(encodedValue,"value");
                    }
                } else {
                    log.debug("Removing preference {}: {}", u, key);
                    ec.deleteObject(pref);
                }
            } else if (value!=null) {
                pref = ERXEOControlUtilities.createAndInsertObject(ec, "ERCPreference");
                u.newPreference(pref);
                // done this way to not force you to sub-class our User entity
                pref.takeValueForKey(ERXEOControlUtilities.primaryKeyObjectForObject((EOEnterpriseObject)u),"userID");
                pref.takeValueForKey(key,"key");
                pref.takeValueForKey(encodedValue(value),"value");
                if (log.isDebugEnabled())
                    log.debug("Creating preference {}: {} - {} -- {}", u, key, value, encodedValue(value));
            }
            if (ec.hasChanges()) {
                ec.saveChanges();
            }
        } catch(RuntimeException ex) {
        	log.error("Error while setting preference {}", key, ex);
        } finally {
            ec.unlock();
        }
        ec.dispose();
        NSNotificationCenter.defaultCenter().postNotification(PreferenceDidChangeNotification,
                                                              new NSDictionary(value, key));
    }

    public boolean booleanValueForKey(String key) {
        return booleanValueForKeyWithDefault(key, false);
    }

    public boolean booleanValueForKeyWithDefault(String key, boolean def) {
        return ERXValueUtilities.booleanValueWithDefault(valueForKey(key), def);
    }

    public static class _UserPreferenceHandler {
        public _UserPreferenceHandler() {
            NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("handleBatchSizeChange", ERXConstant.NotificationClassArray), ERXBatchNavigationBar.BatchSizeChanged, null);
            NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("handleSortOrderingChange", ERXConstant.NotificationClassArray), ERXSortOrder.SortOrderingChanged, null);
        }
    	
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
}
