package er.reporting;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSKeyValueCoding;

/** This class exists so {@link NSKeyValueCoding} can access protected instance variables and methods.*/
public class KeyValueCodingProtectedAccessor extends NSKeyValueCoding.ValueAccessor {

    public static final Logger cat = Logger.getLogger(KeyValueCodingProtectedAccessor.class);
    
    public KeyValueCodingProtectedAccessor() { super(); }

    @Override
    public Object fieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
        //cat.warn("FieldValue, field: " + field.toString() + " object: " + object.toString());
        return field.get(object);
    }

    @Override
    public void setFieldValue(Object object, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        //cat.warn("SetFieldValue, field: " + field.toString() + " value: " + value + " object: " + object.toString());
        field.set(object, value);
    }

    @Override
    public Object methodValue(Object object, Method method) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        //cat.warn("MethodValue, method: " + method.toString() + " object: " + object.toString());
        return method.invoke(object, (Object[])null);
    }

    @Override
    public void setMethodValue(Object object, Method method, Object value) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        //cat.warn("SetMethodValue, method: " + method.toString() + " value: " + value + " object: " + object.toString());
        method.invoke(object, new Object[] {value});
    }
}
