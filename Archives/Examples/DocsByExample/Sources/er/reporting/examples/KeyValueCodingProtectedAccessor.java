/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.reporting.examples;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Category;

import com.webobjects.foundation.NSKeyValueCoding;

public class KeyValueCodingProtectedAccessor extends NSKeyValueCoding.ValueAccessor {

    public static final Category cat = Category.getInstance(KeyValueCodingProtectedAccessor.class);
    
    public KeyValueCodingProtectedAccessor() { super(); }

    public Object fieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
        //cat.warn("FieldValue, field: " + field.toString() + " object: " + object.toString());
        return field.get(object);
    }

    public void setFieldValue(Object object, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        //cat.warn("SetFieldValue, field: " + field.toString() + " value: " + value + " object: " + object.toString());
        field.set(object, value);
    }

    public Object methodValue(Object object, Method method) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        //cat.warn("MethodValue, method: " + method.toString() + " object: " + object.toString());
        return method.invoke(object, (Object[])null);
    }

    public void setMethodValue(Object object, Method method, Object value) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        //cat.warn("SetMethodValue, method: " + method.toString() + " value: " + value + " object: " + object.toString());
        method.invoke(object, new Object[] {value});
    }
}
