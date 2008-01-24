/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.extensions.ERXLocalizer;
import er.extensions.ERXUtilities;
import er.extensions.ERXValueUtilities;

/**
 * Abstact super class of most assignments found in 
 * the ERDirectToWeb framework. This class provides 
 * default implementations for localization support
 * and dynamic method lookup for firing rules.
 */
// ENHANCEME: Should have a default static implementation of decoding an assignment from an unarchiver
//		that way all of the subclasses don't have to implement decodeWithKeyValueUnarchiver
// ENHANCEME: Also should have a weak hash map implementation for caching created assignments.
public abstract class ERDAssignment extends Assignment implements ERDComputingAssignmentInterface {

    /** logging supprt */
    public final static Logger log = Logger.getLogger("er.directtoweb.rules.ERDAssignment");

    /** Cached context class array */
    // MOVEME: ERDConstants
    public final static Class[] D2WContextClassArray = new Class[] { D2WContext.class };

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDAssignment(EOKeyValueUnarchiver u) { super(u); }
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDAssignment(String key, Object value) { super(key,value); }

    protected boolean booleanContextValueForKey(D2WContext c, String key, boolean defaultValue) {
        return ERXValueUtilities.booleanValueWithDefault(c.valueForKey(key), defaultValue);
    }


    protected static void logDeprecatedMessage(Class oldClass, Class newClass) {
        log.error(oldClass.getName() + " is deprecated, please fix your rules to use " + newClass.getName() + " instead");
    }
    
    /**
     * Gets the localizer for a given context.
     * The default implementation just returns
     * the localizer for the current session of
     * the given context. This method belongs to
     * {@link ERDLocaizableInterface}.
     * @param c current context
     * @return localizer for the preferred language
     *		of the session. 
     */
    public ERXLocalizer localizerForContext(D2WContext c) {
        return ERXLocalizer.currentLocalizer();
    }

    /**
     * Returns a localized value for a given key in a given
     * context if localization is enabled. This implementation
     * calls <code>localizedStringForKeyWithDefault</code> on the
     * localizer for the given context. This method belongs to 
     * {@link ERDLocalizableInterface}.
     * @param key to be looked up on the context
     * @param c current context
     * @return localized version of the given key returning the key 
     * 		as the default if a localized version isn't found.
     */
    public Object localizedValueForKeyWithDefaultInContext(String key, D2WContext c) {
        if (key != null && ERXLocalizer.isLocalizationEnabled()) {
            return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(key);
        } else {
            return key;
        }
    }
    /**
     * Returns a localized value for a given key in a given
     * context if localization is enabled. This implementation
     * calls <code>valueForKeyPath</code> on the
     * localizer for the given context. This method belongs to 
     * {@link ERDLocaizableInterface}.
     * @param key to be looked up on the context
     * @param c current context
     * @return localized version of the given key if localization .
     */
    public Object localizedValueForKeyInContext(String key, D2WContext c) {
        if(key != null && ERXLocalizer.isLocalizationEnabled()) {
            return ERXLocalizer.currentLocalizer().valueForKeyPath(key);
        } else {
            return key;
        }
    }
    
    public Object localizedTemplateStringForKeyInContext(String key, D2WContext c) {
        if( key != null && ERXLocalizer.isLocalizationEnabled()) {
            return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject(key, c);
        } else {
            return key;
        }
    }
    
    /**
     * There are basically two choices to lookup the method to
     * be called when an assignment is fired. The first is to
     * use the keypath that is being requested to lookup the
     * method, i.e. if the context is being asked for the key:
     * displayNameForProperty then that method will be called 
     * on the particular assignment. The second method is to 
     * use the value of the assignment as the method to be called.
     * Using the value of the assignment allows the passing of a 
     * parameter to your assignment method, this gives the flexibility 
     * to have several methods for the same key path.
     * @param c current context
     * @return the name of the method to be called, by default the 
     * 		key path of the assignmnet is returned.
     */
    public String keyForMethodLookup(D2WContext c) {
        return keyPath();
    }

    /**
     * Method called to fire an assignment. This method
     * has been enhanced to dynamicly lookup the real 
     * method to call based on the return value of 
     * <code>keyForMethodLookup</code>. The default
     * implementation will lookup the method based on the
     * key path of the assignment. If you are building a 
     * generic assignment like a BooleanAssignment you 
     * should override this method seeing as you wouldn't
     * care what the key path of the assignment is. If you 
     * would like to provide a different methodology for the
     * method to be fired override the method keyForMethodLookup.
     * @param c current D2W context
     * @return result of firing the assignment.
     */
    public Object fire(D2WContext c) {
        Object result = null;
        try {
            // ENHANCEME: This method lookup should be staticly cached, something along the
            //		lines of className-keyForMethod.
            Method m = getClass().getMethod(keyForMethodLookup(c), D2WContextClassArray);
            result = m.invoke(this, new Object[] { c });
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException occurred in ERAssignment: " + e.toString() 
            + " keyForMethodLookup(): " + keyForMethodLookup(c) + " target exception: " 
            + e.getTargetException()+ " assignment was " + this + "\n\n" + "Target exception backtrace: "
            + ERXUtilities.stackTrace(e.getTargetException()));
        } catch (Exception e) {
            log.error("Exception occurred in ERDAssignment of class: " + this.getClass().getName() 
            + ": " + e.toString() + " keyForMethodLookup(): " + keyForMethodLookup(c) + " assignment was " + this);
        }
        return result;
    }
}
