package er.reporting;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSKeyValueCoding;

/** This class exists so {@link NSKeyValueCoding} can access protected instance variables and methods.*/
public class KeyValueCodingProtectedAccessor extends NSKeyValueCoding.ValueAccessor {
    private static final Logger log = LoggerFactory.getLogger(KeyValueCodingProtectedAccessor.class);

    public KeyValueCodingProtectedAccessor() { super(); }

    @Override
    public Object fieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
        //log.warn("FieldValue, field: {} object: {}", field, object);
        return field.get(object);
    }

    @Override
    public void setFieldValue(Object object, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        //log.warn("SetFieldValue, field: {} value: {} object: {}", field, value, object);
        field.set(object, value);
    }

    @Override
    public Object methodValue(Object object, Method method) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        //log.warn("MethodValue, method: {} object: {}", method, object);
        return method.invoke(object, (Object[])null);
    }

    @Override
    public void setMethodValue(Object object, Method method, Object value) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        //log.warn("SetMethodValue, method: {} value: {} object: {}", method, value, object);
        method.invoke(object, new Object[] {value});
    }
}
