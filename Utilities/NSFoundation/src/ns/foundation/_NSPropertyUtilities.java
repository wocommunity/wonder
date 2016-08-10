package ns.foundation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public abstract class _NSPropertyUtilities {
  private static CtClass CT_NUMBER_CLASS;
  private static ClassPool ctPool;
  
  static {
    ctPool = ClassPool.getDefault();
    try {
      CT_NUMBER_CLASS = ctPool.getCtClass("java.lang.Number");
    } catch (NotFoundException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
  }
  
  public static boolean _isCtClassANumber(CtClass type) {
    try {
      for (CtClass intf : type.getInterfaces()) {
        if (intf == CT_NUMBER_CLASS) {
          return true;
        }
      }
    } catch (NotFoundException e) {
      e.printStackTrace();
    }
    return false;
  }
  
  public static boolean _isCtClassABoolean(CtClass type) {
    return type == CtClass.booleanType || type.getName().equals("java.lang.Boolean");
  }
  
  public static <T> T convertObjectIntoCompatibleValue(Object value, Class<T> newValueType) {
    return _convertObjectIntoCompatibleValue(value, newValueType); 
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T _convertObjectIntoCompatibleValue(Object value, Class<T> newValueType) {
    if (value == null || newValueType == value.getClass())
      return (T)value;
    if (newValueType == String.class)
      return (T)value.toString();
    if (_NSUtilities._isClassANumber(newValueType)) {
      if (value instanceof Number)
        return (T) _NSUtilities.convertNumberIntoCompatibleValue((Number)value, (Class<Number>)newValueType);
      if (value instanceof Boolean)
        return (T) _NSUtilities.convertNumberIntoCompatibleValue(_NSUtilities.convertBooleanIntoNumberValue((Boolean)value), (Class<Number>)newValueType);
      return (T) _NSUtilities.convertNumberIntoCompatibleValue(Double.valueOf(value.toString()), (Class<Number>)newValueType);
    }
    if (_NSUtilities._isClassABoolean(newValueType)) {
      if (value instanceof Number)
        return (T) _NSUtilities.convertNumberIntoBooleanValue((Number)value);
      return (T) Boolean.valueOf(value.toString());
    }
    return (T) value;
  }
}
