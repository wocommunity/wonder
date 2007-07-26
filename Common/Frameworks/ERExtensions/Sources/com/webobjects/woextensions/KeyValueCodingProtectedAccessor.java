/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package com.webobjects.woextensions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.webobjects.foundation.NSKeyValueCoding;

public class KeyValueCodingProtectedAccessor extends NSKeyValueCoding.ValueAccessor {
    
    public KeyValueCodingProtectedAccessor() { super(); }

    public Object fieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
        return field.get(object);
    }

    public void setFieldValue(Object object, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, value);
    }

    public Object methodValue(Object object, Method method) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        return method.invoke(object, (Object[])null);
    }

    public void setMethodValue(Object object, Method method, Object value) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        method.invoke(object, new Object[] {value});
    }
}
