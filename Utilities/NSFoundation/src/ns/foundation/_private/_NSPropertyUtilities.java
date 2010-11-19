package ns.foundation._private;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import ns.foundation.NSForwardException;

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
}
