package com.webobjects.eocontrol;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.webobjects.foundation.NSKeyValueCoding;

public class KeyValueCodingProtectedAccessor
    extends NSKeyValueCoding.ValueAccessor
{
    @Override
    public Object fieldValue(Object object, Field field)
	throws IllegalArgumentException, IllegalAccessException {
	return field.get(object);
    }
    
    @Override
    public void setFieldValue(Object object, Field field, Object object0)
	throws IllegalArgumentException, IllegalAccessException {
	field.set(object, object0);
    }
    
    @Override
    public Object methodValue(Object object, Method method)
	throws IllegalArgumentException, IllegalAccessException,
	       InvocationTargetException {
	return method.invoke(object,  (Object[])null);
    }
    
    @Override
    public void setMethodValue
	(Object object, Method method, Object object1)
	throws IllegalArgumentException, IllegalAccessException,
	       InvocationTargetException {
	method.invoke(object, new Object[] { object1 });
    }
    
    @Override
    public String toString() {
	return "KeyValueCodingProtectedAccessor";
    }
}

