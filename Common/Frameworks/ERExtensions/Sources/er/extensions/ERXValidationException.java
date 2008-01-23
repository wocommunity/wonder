/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSValidation;

/**
 * ERXValidationExceptions extends the regular
 * {@link com.webobjects.foundation.NSValidation.ValidationException NSValidation.ValidationException}
 * to add template based resolution of the validation exception. See more
 * information about resolving templates in the {@link ERXValidationFactory ERXValidationFactory}.
 */
public class ERXValidationException extends NSValidation.ValidationException implements NSKeyValueCoding {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXValidationException.class);

    // Validation Exception Types
    /** corresponds to a model thrown 'null property' exception */
    public static final String NullPropertyException = "NullPropertyException";
    
    /** corresponds to a number formatter exception */
    public static final String InvalidNumberException = "InvalidNumberException";

    /** corresponds to a model thrown 'mandatory toOne relationship' exception */
    public static final String MandatoryToOneRelationshipException = "MandatoryToOneRelationshipException";

    /** corresponds to a model thrown 'manditory toMany relationship' exception */
    public static final String MandatoryToManyRelationshipException = "MandatoryToManyRelationshipException";

    /** corresponds to a model thrown 'object removal' exception */    
    public static final String ObjectRemovalException = "ObjectRemovalException";

    /** corresponds to a model thrown 'objects removal' exception */
    public static final String ObjectsRemovalException = "ObjectsRemovalException";

    /** corresponds to a model thrown 'maximum length of attribute exceeded' exception */
    public static final String ExceedsMaximumLengthException = "ExceedsMaximumLengthException";

    /** corresponds to a model thrown 'Error converting value of class' exception */
    public static final String ValueConversionException = "ValueConversionException";

    /** corresponds to a custom method exception */
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

    /** holds the object that failed validation */
    protected Object object;

    /** holds the target language if provided */
    protected String targetLanguage;
    
    /** caches any set additionalExceptions */
    protected NSArray additionalExceptions;
    
    /** holds a reference to the context of the exception */
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
     * 
     */
    protected String _getMessage() {
        if(message == null) {
            return type;
        }
        return message;
    }
    
    /**
     * Implementation of key value coding.
     * Uses the default implementation.
     * @param key to look up
     * @return result of the lookup on the object
     */
    public Object valueForKey(String key) {
    	try {
    		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    	} catch(NSKeyValueCoding.UnknownKeyException ex) {
    		// AK: when we try to fix the bug in ERDirectToWeb templates that specify "context." explicitly 
    		// by setting up ourselves as the context, we could still run into keys we can't resolve 
    		// (like "indefiniteArticle") and we just return null for that. Of course we should fix the templates instead
    		// and then ask the context *before* we ask ourself, so the displayPropertyKey of the context gets
    		// precedence over our version
    		if(context() != null && context() != this) {
    			return context().valueForKey(key);
    		}
    		throw ex;
    	}
    }

    /**
     * Implementation of the key value coding.
     * Uses the default implementation.
     * @param obj value to be set on this exception
     * @param key to be set
     */
    public void takeValueForKey(Object obj, String key) {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, key);
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
     * Returns method name. The method name is only set if the validation
     * exception is a custom validation exception.
     * @return custom method name.
     */
    public String method() { return method; }

    /**
     * Sets the custom method name that threw the
     * validation exception.
     * @param aMethod name to be set.
     */
    public void setMethod(String aMethod) { method = aMethod; }

    /**
     * Cover method that casts the <code>object</code> of
     * the validation exception to an EOEnterpriseObject.
     * @return object cast as an enterprise object.
     */
    public EOEnterpriseObject eoObject() { return (EOEnterpriseObject)object(); }


    /**
     * Overrides super implementation to allow for setable object value.
     * @return object object for this exception.
     */
    public Object object() {
        if(object == null)
        	object = super.object();
        return object;
    }

    /**
     * Cover method for returning the <code>key</code> of
     * the validation exception under the name propertyKey.
     * @return the key of the validation exception
     */
    public String propertyKey() { return key(); }

    /**
     * Cover method for getting the attribute corresponding
     * to the <b>propertyKey</b> and <b>entity</b> off of
     * the objet.
     * @return EOAttribute corresponding to the propertyKey
     *		and entity.
     */
    public EOAttribute attribute() {
        EOAttribute attribute = null;
        if (eoObject() != null) {
            EOEntity entity = EOUtilities.entityForObject(eoObject().editingContext(), eoObject());
            attribute = entity != null ? entity.attributeNamed(propertyKey()) : null;
        }
        return attribute;
    }
    
    /**
     * Cover method to return the type of the validation
     * exception. The corresponds to one of the constant
     * strings defined in this class.
     * @return the type of this validation exception.
     */
    public String type() { return type; }

    /**
     * Sets the validation type of this exception.
     * Should correspond to one of the type constants
     * defined in this class. All of the model thrown
     * validation exceptions should have the correct
     * type already set.
     * @param aType name to set on this validation
     *		exception.
     */
    public void setType(String aType) { type = aType; }

    /**
     * Returns the value that failed validation.
     * @return failed validation value.
     */
    public Object value() { return value; }

    /**
     * Sets the value that failed validation.
     * @param aValue that failed validation
     */
    public void setValue(Object aValue) { value = aValue; }

    /**
     * Sets the object that failed validation.
     * @param aValue object that failed validation
     */
    public void setObject(Object aValue) { object = aValue; }
    
    /**
     * Returns the target language to display the validation message in.
     * If a target language is not set then the current default
     * language for the current thread is used.
     * @return target language if one is set.
     */
    public String targetLanguage() { return targetLanguage; }

    /**
     * Sets the target language to use when rendering the validation
     * message. Only set a target language if you want to override
     * the current language of the thread.
     * @param aValue name fo the language to render the validation
     *		exception in.
     */
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
     * When this also returns null, then the exception will be used as its context.
     * This is needed because of some of the templates in ERDirectToWeb which use
     * <code>context.propertyKey</code> and will display <b>?</b> if none is given. 
     * <p>
     * @return current context for the validation exception.
     */
    // CHECKME: Now with WO 5 this doesn't need to implement the NSKeyValueCoding interface
    public NSKeyValueCoding context() {
        if (_context == null)
            _context = ERXValidationFactory.defaultFactory().contextForException(this);
        if (_context == null)
        	return this;
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
     * occurred. The reason this method is needed is because
     * we wanted to have the ability to set the additional
     * exceptions.
     * @return array of additional exceptions
     */
    public NSArray additionalExceptions() {
        if (additionalExceptions == null) {
            additionalExceptions = super.additionalExceptions();
            if (additionalExceptions == null)
                return NSArray.EmptyArray;
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
            displayName = localizer.localizedStringForKeyWithDefault(displayName);
        }
        return displayName;
    }

    /**
     * Compares this exception to anything else.
     * @return description of the validation exception
     */
    public boolean equals(Object anotherObject) {
        if(anotherObject != null && anotherObject instanceof ERXValidationException) {
            ERXValidationException ex = (ERXValidationException)anotherObject;
            return ERXExtensions.safeEquals(type(), ex.type()) && ERXExtensions.safeEquals(key(), ex.key()) && ERXExtensions.safeEquals(object(), ex.object())
                && ERXExtensions.safeEquals(value(), ex.value()) && ERXExtensions.safeEquals(additionalExceptions(), ex.additionalExceptions());
        }
        return super.equals(anotherObject);
    }
    
    
    /**
     * Returns the formatted description of the validation exception
     * without calling <code>getMessage</code>.
     * @return description of the validation exception
     */
    public String toString() {
        return "<" + getClass().getName() + " object: " + object() + "; propertyKey: "
        + propertyKey() + "; type: " + type() + "; additionalExceptions: " + additionalExceptions() + ">";
    }
}
