package ns.foundation;

import java.math.BigDecimal;
import java.math.BigInteger;

public class _NSUtilities {
  private static _TypeConversionDelegate _delegate = null;

  public abstract static class _TypeConversionDelegate {
    public abstract <T> T convertObjectIntoCompatibleValue(Object value, Class<T> newValueType);
  }
  
  public static void setTypeConversionDelegate(_TypeConversionDelegate delegate) {
    _delegate = delegate;
  }
  
  public static boolean _isClassABoolean(Class<?> valueClass) {
    return ((valueClass == Boolean.class) || (valueClass == boolean.class));
  }

  public static boolean _isClassANumber(Class<?> valueClass) {
    
    return ((valueClass == Number.class) || (valueClass == int.class) || (valueClass == Integer.class) 
        || (valueClass == double.class) || (valueClass == Double.class)
        || (valueClass == long.class) || (valueClass == Long.class)  
        || (valueClass == float.class) || (valueClass == Float.class)
        || (valueClass == byte.class) || (valueClass == Byte.class)
        || (valueClass == short.class) || (valueClass == Short.class)
        || (valueClass == BigDecimal.class) || (valueClass == BigInteger.class));
  }

  public static boolean _isClassANumberOrABoolean(Class<?> valueClass) {
    return _isClassANumber(valueClass) || _isClassABoolean(valueClass);
  }

  public static <T> T convertObjectIntoCompatibleValue(Object value, Class<T> newValueType) {
    if (_delegate != null) {
      return _delegate.convertObjectIntoCompatibleValue(value, newValueType);
    }
    return _convertObjectIntoCompatibleValue(value, newValueType); 
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T _convertObjectIntoCompatibleValue(Object value, Class<T> newValueType) {
    if (value == null || newValueType == value.getClass())
      return (T)value;
    if (newValueType == String.class)
      return (T)value.toString();
    if (_isClassANumber(newValueType)) {
      if (value instanceof Number)
        return (T) convertNumberIntoCompatibleValue((Number)value, (Class<Number>)newValueType);
      if (value instanceof Boolean)
        return (T) convertNumberIntoCompatibleValue(convertBooleanIntoNumberValue((Boolean)value), (Class<Number>)newValueType);
      return (T) convertNumberIntoCompatibleValue(Double.valueOf(value.toString()), (Class<Number>)newValueType);
    }
    if (_isClassABoolean(newValueType)) {
      if (value instanceof Number)
        return (T) convertNumberIntoBooleanValue((Number)value);
      return (T) Boolean.valueOf(value.toString());
    }
    return (T) value;
  }
  
  public static Number convertBooleanIntoNumberValue(Boolean value) {
    if (value == null)
      return null;
    return value ? (short)1 : (short)0;
  }
  
  public static Boolean convertNumberIntoBooleanValue(Number value) {
    if (value == null)
      return null;
    
    int compare = 0;
    if (value instanceof BigDecimal)
      compare = ((BigDecimal)value).compareTo(BigDecimal.ZERO);
    else if (value instanceof BigInteger)
      compare = ((BigInteger)value).compareTo(BigInteger.ZERO);
    else
      compare = Integer.valueOf(((Number)value).intValue()).compareTo(Integer.valueOf(0));
    return 0 == compare ? false : true;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T convertNumberIntoCompatibleValue(Object value, Class<T> newValueType) {
    if (value == null)
      return null;

    if (newValueType.isInstance(value)) {
      return (T) value;
    }
    
    if (newValueType == Number.class)
      return (T) value;
    
    if (newValueType == BigInteger.class) {
      if (value instanceof BigDecimal)
        return (T) ((BigDecimal)value).toBigInteger();
      return (T) new BigDecimal(value.toString()).toBigInteger();
    }
    
    if (newValueType == BigDecimal.class) {
      return (T) new BigDecimal(value.toString());
    }
    
    if (newValueType == Integer.class || newValueType == int.class)
      return (T) Integer.valueOf(((Number)value).intValue());

    if (newValueType == Long.class || newValueType == long.class)
      return (T) Long.valueOf(((Number)value).longValue());

    if (newValueType == Short.class || newValueType == short.class)
      return (T) Short.valueOf(((Number)value).shortValue());

    if (newValueType == Byte.class || newValueType == byte.class)
      return (T) Byte.valueOf(((Number)value).byteValue());
    
    if (newValueType == Double.class || newValueType == double.class)
      return (T) Double.valueOf(((Number)value).doubleValue());

    if (newValueType == Float.class || newValueType == float.class)
      return (T) Float.valueOf(((Number)value).floatValue());

    return (T) value;
  }
  
  public static Class<?> classObjectForClass(Class<?> objectClass) {
    if(objectClass.isPrimitive()) {
      if(objectClass == Boolean.TYPE)
        return Boolean.class;
      if(objectClass == Short.TYPE)
        return Short.class;
      if(objectClass == Integer.TYPE)
        return Integer.class;
      if(objectClass == Long.TYPE)
        return Long.class;
      if(objectClass == Double.TYPE)
        return Double.class;
      if(objectClass == Float.TYPE)
        return Float.class;
      if(objectClass == Character.TYPE)
        return Character.class;
      if(objectClass == Void.TYPE)
        return Void.class;
    }
    return objectClass;
  }
}
