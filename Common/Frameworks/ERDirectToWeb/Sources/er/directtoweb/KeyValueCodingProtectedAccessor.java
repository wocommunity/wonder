/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.lang.reflect.*;

import com.webobjects.foundation.*;

import er.extensions.*;

public class KeyValueCodingProtectedAccessor extends NSKeyValueCoding.ValueAccessor {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(KeyValueCodingProtectedAccessor.class);

    public KeyValueCodingProtectedAccessor() { super(); }

    public Object fieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
        //log.warn("FieldValue, field: " + field.toString() + " object: " + object.toString());
        return field.get(object);
    }

    public void setFieldValue(Object object, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        //log.warn("SetFieldValue, field: " + field.toString() + " value: " + value + " object: " + object.toString());
        field.set(object, value);
    }

    public Object methodValue(Object object, Method method) throws IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
            //log.warn("MethodValue, method: " + method.toString() + " object: " + object.toString());
            return method.invoke(object, null);
        }

    public void setMethodValue(Object object, Method method, Object value) throws IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
            //log.warn("SetMethodValue, method: " + method.toString() + " value: " + value + " object: " + object.toString());
            method.invoke(object, new Object[] {value});
        }
}
