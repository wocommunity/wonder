package er.excel;
import java.lang.reflect.*;

import com.webobjects.foundation.*;

public class KeyValueCodingProtectedAccessor
extends NSKeyValueCoding.ValueAccessor
{
    public Object fieldValue(Object object, Field field)
    throws IllegalArgumentException, IllegalAccessException {
        return field.get(object);
    }

    public void setFieldValue(Object object, Field field, Object object0)
    throws IllegalArgumentException, IllegalAccessException {
        field.set(object, object0);
    }

    public Object methodValue(Object object, Method method)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        return method.invoke(object, null);
    }

    public void setMethodValue
    (Object object, Method method, Object object1)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        method.invoke(object, new Object[] { object1 });
    }

    public String toString() {
        return "KeyValueCodingProtectedAccessor";
    }
}

