/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXValidationException.java created by max on Wed 21-Mar-2001 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import org.apache.log4j.Category;

public class ERXValidationException extends NSValidation.ValidationException implements NSKeyValueCoding {

    public static final Category cat = Category.getInstance("er.validation.ERXValidationException");

    // Validation Exception Types
    public static final String NullPropertyException = "NullPropertyException";
    public static final String InvalidNumberException = "InvalidNumberException";
    public static final String MandatoryRelationshipException = "MandatoryRelationshipException";
    public static final String ObjectRemovalException = "ObjectRemovalException";
    public static final String CustomMethodException = "CustomMethodException";

    // Additional Validation Keys
    public static final String ValidationTypeUserInfoKey = "ValidationTypeUserInfoKey";
    public static final String ValidatedValueUserInfoKey = "ValidatedValueUserInfoKey";
    public static final String ValidatedMethodUserInfoKey = "ValidatedMethodUserInfoKey";
    public static final String TargetLanguageUserInfoKey = "TargetLanguageUserInfoKey";

    public ERXValidationException(String type, NSDictionary userInfo) {
        super(type, userInfo);
        setType(type);
    }
    
    public ERXValidationException(EOEnterpriseObject object, String method) {
        super(CustomMethodException);
        customExceptionForMethod(object, method);
    }

    public ERXValidationException(EOEnterpriseObject object, String property, Object value, String type) {
        super(type);
        exceptionForObject(object, property, value, type);
    }    

    protected String _message;
    public String getMessage() {
        if (_message == null) {
            _message = ERXValidationFactory.defaultFactory().messageForException(this);
        }
        return _message;
    }
    
    public void customExceptionForMethod(EOEnterpriseObject object, String method) {
        setEoObject(object);
        setMethod(method);
        setType(CustomMethodException);
    }

    public void exceptionForObject(EOEnterpriseObject object, String property, Object value, String type) {
        if (object != null)
            setEoObject(object);
        if (property != null)
            setPropertyKey(property);
        setValue((value != null ? value : NSKeyValueCoding.NullValue));
        setType(type);
    }

    public boolean isCustomMethodException() { return type() == CustomMethodException; }

    //FIXME: (ak) this is probably a stupid way to work around super.userInfo() returning NSDictionary, but it should work for now...please someone check if userInfo is really required to be mutable
    protected NSMutableDictionary _mutableUserInfo;
    protected NSMutableDictionary _userInfo() {
        if(_mutableUserInfo == null) {
            Object info = super.userInfo();
            if(info == null) {
                _mutableUserInfo = new NSMutableDictionary();
            } else if(info.getClass() == NSDictionary.class) {
                _mutableUserInfo = new NSMutableDictionary((NSDictionary)info);
            } else if(info instanceof NSMutableDictionary) {
                _mutableUserInfo = (NSMutableDictionary)info;
            }
        }
        return _mutableUserInfo;
    }
    
    // Done to make template parsing easier.
    public Object valueForKey(String key) {
        Object value = null;
        if (key.equals("object"))
            value = object();
        else if (key.equals("propertyKey"))
            value = propertyKey();
        else if (key.equals("method"))
            value = method();
        else if (key.equals("type"))
            value = type();
        else if (key.equals("value"))
            value = value();
        return value != null ? value : objectForKey(key);
    }

    public void takeValueForKey(Object value, String key) { setObjectForKey(value, key); }
    
    public Object objectForKey( String aKey ) { return _userInfo().objectForKey( aKey ); }
    public void setObjectForKey( Object anObject, String aKey ) { _userInfo().setObjectForKey( anObject, aKey ); }

    public String method() { return (String) objectForKey( ValidatedMethodUserInfoKey ); }
    public void setMethod(String aMethod) { setObjectForKey(aMethod, ValidatedMethodUserInfoKey ); }
    
    public EOEnterpriseObject eoObject() { return (EOEnterpriseObject)object(); }
    public void setEoObject( EOEnterpriseObject anObject ) { setObjectForKey( anObject, ValidatedObjectUserInfoKey ); }
    
    public String propertyKey() { return key(); }
    public void setPropertyKey( String aProperty ) { setObjectForKey( aProperty, ValidatedKeyUserInfoKey ); }

    public String type() { return (String) objectForKey( ValidationTypeUserInfoKey ); }
    public void setType(String aType) { setObjectForKey(aType, ValidationTypeUserInfoKey ); }
    
    public Object value() { return objectForKey( ValidatedValueUserInfoKey ); }
    public void setValue(Object aValue) { setObjectForKey(aValue, ValidatedValueUserInfoKey); }

    public String targetLanguage() { return (String)objectForKey( TargetLanguageUserInfoKey ); }
    public void setTargetLanguage(String aValue) { setObjectForKey(aValue, TargetLanguageUserInfoKey); }

    public NSMutableArray additionalExceptionsMutable() { return (NSMutableArray) objectForKey(AdditionalExceptionsKey); }

    private volatile Object _delegate;
    public Object delegate() { return _delegate != null ? _delegate : ERXValidationFactory.defaultDelegate(); }
    public void setDelegate(Object obj) { _delegate = obj; }
}