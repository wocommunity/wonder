/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import org.apache.log4j.Category;

public class ERXValidationException extends NSValidation.ValidationException implements NSKeyValueCoding {

    /** logging support */
    public static final Category cat = Category.getInstance(ERXValidationException.class);

    // Validation Exception Types
    
    public static final String NullPropertyException = "NullPropertyException";
    public static final String InvalidNumberException = "InvalidNumberException";
    public static final String MandatoryToOneRelationshipException = "MandatoryToOneRelationshipException";
    public static final String MandatoryRelationshipException = "MandatoryRelationshipException";
    public static final String ObjectRemovalException = "ObjectRemovalException";
    public static final String ObjectsRemovalException = "ObjectsRemovalException";
    public static final String CustomMethodException = "CustomMethodException";

    /**
     * Default constructor that builds a validation exception
     * without the failed value specified. If you want to have
     * validation templates that refer to the value that didn't
     * pass validation use the four argument constructor. Usually
     * for creating custom validation exceptions the 
     * {@link ERXValidationFactory} should be used.
     *
     * @param type of the exception, should be one of the constaints
     *		defined in this class.
     * @param object that is throwing the exception
     * @param key property key that failed validation
     */
    public ERXValidationException(String type, Object object, String key) {
        this(type, object, key, null);
    }

    /**
     * Default constructor that builds a validation exception
     * based on the type, object, key and failed value.Usually
     * for creating custom validation exceptions the
     * {@link ERXValidationFactory} should be used.
     *
     * @param type of the exception, should be one of the constaints
     *		defined in this class.
     * @param object that is throwing the exception
     * @param key property key that failed validation
     * @param value that failed validation
     */    
    public ERXValidationException(String type, Object object, String key, Object value) {
        super(type, object, key);
        setType(type);
        setValue(value);
    }    

    /** caches the validation message */
    protected String message;
    
    /** holds the method if one is provided */
    protected String method;
    
    /** holds the type of the exception */
    protected String type;
    
    /** holds the value that failed validation */
    protected Object value;
    
    /** holds the target language if provided */
    protected String targetLanguage;
    
    /** caches any set additionalExceptions */
    protected NSArray additionalExceptions;
    
    /** holds a reference to the context of the ecxception */
    protected volatile NSKeyValueCoding _context;
    
    /** holds a reference to the exception delegate */
    protected volatile Object delegate;
    
    /**
     * Gets the message for this exception.
     * @return the correctly formatted validation exception.
     */
    public String getMessage() {
        if (message == null)
            message = ERXValidationFactory.defaultFactory().messageForException(this);
        return message;
    }

    /**
     * Implementation of key value implementation.
     * Uses the default implementation.
     * @param key to look up
     * @return result of the lookup on the object
     */
    public Object valueForKey(String key) {
        return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }

    /**
     * Implementation of the key value implementation
     * Uses the default implementation.
     * @param obj value to be set on this exception
     * @param key to be set
     */
    public void takeValueForKey(Object obj, String key) {
        com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, key);
    }

    /**
     * Convience method to determine if this exception
     * was a custom thrown exception instead of a model
     * thrown exception. A custom exception would be
     * an exception that you throw in your validateFoo
     * method if a particular constraint is not valid.
     * @return if this exception is a custom thrown exception.
     */
    public boolean isCustomMethodException() { return type() == CustomMethodException; }

    /**
     *  
     */
    public String method() { return method; }
    
    public void setMethod(String aMethod) { method = aMethod; }
    
    public EOEnterpriseObject eoObject() { return (EOEnterpriseObject)object(); }
    
    public String propertyKey() { return key(); }

    public String type() { return type; }
    public void setType(String aType) { type = aType; }

    public Object value() { return value; }
    public void setValue(Object aValue) { value = aValue; }

    public String targetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String aValue) {  targetLanguage = aValue; }

    /**
     * Gets the current delegate for this validation exception.
     * If one is not set then the default delegate for the
     * {@link ERXValidationFactory ERXValidationFactory} is returned.
     * @return delegate for this validation exception.
     */
    public Object delegate() { return delegate != null ? delegate : ERXValidationFactory.defaultDelegate(); }

    /**
     * Sets the delegate for the current validation exception.
     * The delegate can intervine to provide a different template
     * for the validation exception or resolve the template in a
     * different manner.
     * @param obj delegate to be used for this validation exception.
     */
    public void setDelegate(Object obj) { delegate = obj; }

    /**
     * The current context of the validation exception. Context
     * objects are mainly used for resolving keys in validation
     * templates. When validation exceptions are thrown in D2W
     * pages the current {@link com.webobjects.directtoweb.D2WContext D2WContext}
     * is set as the current context on the exceptions. If a context
     * is not set then the <code>contextForException</code> is called
     * off of the default {@link ERXValidationFactory ERXValidationFactory}.
     * <p>
     * @return current context for the validation exception.
     */
    // CHECKME: Now with WO 5 this doesn't nee to implement the NSKeyValueCoding interface
    public NSKeyValueCoding context() {
        if (_context == null)
            _context = ERXValidationFactory.defaultFactory().contextForException(this);
        return _context;
    }

    /**
     * Sets the context that can be used to resolve key bindings
     * in validation templates.
     * @param context of the current exception
     */
    public void setContext(NSKeyValueCoding context) { _context = context; }

    /**
     * Sets the array of additional exceptions that has
     * occurried.
     * @param exceptions array of additional exceptions
     */
    public void setAdditionalExceptions(NSArray exceptions) {
        additionalExceptions = exceptions;
    }

    /**
     * Cover method to return any additional exceptions that
     * occurried. The reason this method is needed is because
     * we wanted to have the ability to set the additional
     * exceptions.
     * @return array of additional exceptions
     */
    public NSArray additionalExceptions() {
        if (additionalExceptions == null) {
            additionalExceptions = super.additionalExceptions();
            if (additionalExceptions == null)
                additionalExceptions = NSArray.EmptyArray;
        }
        return additionalExceptions;
    }

    /**
     * Generates a displayable and localized version of the
     * current propertyKey (also called key).
     * @return localized displayable version of the current
     *		propertyKey.
     */
    public String displayNameForProperty() {
        return propertyKey() != null ? localizedDisplayNameForKey(propertyKey()) : null;
    }

    /**
     * Generates a displayable and localized version of the
     * current object's entity name.
     * @return localized displayable version of an object's
     *	       entity name.
     */
    public String displayNameForEntity() {
        return eoObject() != null ? localizedDisplayNameForKey(eoObject().entityName()) : null;
    }

    /**
     * Creates a localized display name for a given key
     * using the class description's <code>displayNameForKey</code>
     * and then using a localizer for the current "targetLanguage" or
     * the current localizer to translate the string.
     * @param key to be translated
     * @return localized and display version of the given key.
     */
    protected String localizedDisplayNameForKey(String key) {
        String displayName = key;
        if (eoObject() != null) {
            displayName = eoObject().classDescription().displayNameForKey(key);
        }
        ERXLocalizer localizer = null;
        if (targetLanguage() != null) {
            localizer = ERXLocalizer.localizerForLanguage(targetLanguage());
        } else if (ERXLocalizer.currentLocalizer() != null) {
            localizer = ERXLocalizer.currentLocalizer();
        }
        if (localizer != null) {
            displayName = localizer.localizedStringForKey(displayName);
        }
        return displayName;
    }

    /**
     * Formatted description of the validation exception
     * without calling <code>getMessage</code>.
     * @return description of the validation exception
     */
    public String toString() {
        return "<" + getClass().getName() + " object: " + object() + "; propertyKey: "
        + propertyKey() + "; type: " + type() + ">";
    }
}