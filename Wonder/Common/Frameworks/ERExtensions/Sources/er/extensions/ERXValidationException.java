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
     * Default constructor.
     * @param type of the exception
     * @param object that is throwing the exception
     * @param key property key that failed validation
     */
    public ERXValidationException(String type, Object object, String key) {
        super(type, object, key);
        setType(type);
    }

    public ERXValidationException(String type, Object object, String key, Object value) {
        this(type, object, key);
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

    protected volatile Object delegate;
    public Object delegate() { return delegate != null ? delegate : ERXValidationFactory.defaultDelegate(); }
    public void setDelegate(Object obj) { delegate = obj; }

    public NSKeyValueCoding context() {
        if (_context == null)
            _context = ERXValidationFactory.defaultFactory().contextForException(this);
        return _context;
    }
    public void setContext(NSKeyValueCoding obj) { _context = obj; }

    public void setAdditionalExceptions(NSArray exceptions) {
        additionalExceptions = exceptions;
    }

    public NSArray additionalExceptions() {
        if (additionalExceptions == null) {
            additionalExceptions = super.additionalExceptions();
        }
        return additionalExceptions;
    }

    public String displayNameForProperty() {
        return propertyKey() != null ? localizedDisplayNameForKey(propertyKey()) : null;
    }

    public String displayNameForEntity() {
        return eoObject() != null ? localizedDisplayNameForKey(eoObject().entityName()) : null;
    }
    
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
    
    public String toString() { return "<" + getClass().getName() + " object: " + object() + "; propertyKey: "+ propertyKey() + "; type: " + type() + ">";}

}