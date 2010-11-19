package ns.foundation.overriddenaccess;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ns.foundation.NSKeyValueCoding.ValueAccessor;

public class KeyValueCodingProtectedAccessor extends ValueAccessor {
  @Override
  public Object fieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
    Object result = field.get(object);
    if (result == null) {
      return 42;
    }
    return result;
  }

  @Override
  public void setFieldValue(Object object, Field field, Object object0) throws IllegalArgumentException, IllegalAccessException {
    field.set(object, object0 == null ? 24 : object0);
  }

  @Override
  public Object methodValue(Object object, Method method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Object result = method.invoke(object, (Object[]) null);
    if (result == null) {
      return 42;
    }
    return result;
  }

  @Override
  public void setMethodValue(Object object, Method method, Object object1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    method.invoke(object, new Object[] { object1 == null ? 24 : object1 });
  }
}
