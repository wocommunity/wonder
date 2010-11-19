package ns.foundation;

import java.lang.reflect.InvocationTargetException;

import ns.foundation._private._NSMethod;


public abstract class _NSReflectionUtilities {
  private static final _NSReflectionUtilities instance;

  static {
    instance = new DefaultImplementation();
  }

  public static _NSMethod methodOnObject(Object targetObject, String _name, Class<?>[] _types) throws NoSuchMethodException {
    return instance._methodOnObject(targetObject, _name, _types);
  }

  protected abstract _NSMethod _methodOnObject(Object targetObject, String _name, Class<?>[] _types) throws NoSuchMethodException;

  protected static String encodeType(Class<?> clazz) {
    if (clazz.isArray())
      return clazz.getName();
    if (clazz.isPrimitive()) {
      if (clazz == boolean.class)
        return "Z";
      if (clazz == byte.class)
        return "B";
      if (clazz == char.class)
        return "C";
      if (clazz == double.class)
        return "D";
      if (clazz == float.class)
        return "F";
      if (clazz == int.class)
        return "I";
      if (clazz == long.class)
        return "J";
      if (clazz == short.class)
        return "S";
    }
    return "L" + clazz.getName() + ";";
  }

  protected static String _methodSignature(String _name, Class<?>[] _types) {
    StringBuffer sb = new StringBuffer(_name);
    sb.append('(');
    String separator = "";
    for (Class<?> clazz : _types) {
      sb.append(separator);
      sb.append(encodeType(clazz));
      separator = ",";
    }
    sb.append(")");
    return sb.toString();
  }

  public static boolean classIsAssignableFrom(Class<?> clazz, Class<?> target) {
    if (clazz.isInterface() || target.isInterface()) {
      throw new IllegalArgumentException("Assignability of interfaces is not supported");
    }
    Class<?> arg = clazz;
    while (arg != null && arg != target) {
      arg = arg.getSuperclass();
    }

    return arg == target;
  }

  static class DefaultImplementation extends _NSReflectionUtilities {

    @Override
    protected _NSMethod _methodOnObject(Object targetObject, String _name, Class<?>[] _types) throws NoSuchMethodException {
      java.lang.reflect.Method method = null;
      Class<?> anObjectClass = targetObject.getClass();
      try {
        method = anObjectClass.getMethod(_name, (_types != null) ? _types : new Class[0]);
      } catch (NoSuchMethodException exception) {
        NSLog._conditionallyLogPrivateException(exception);
      } catch (SecurityException exception) {
        NSLog._conditionallyLogPrivateException(exception);
      }

      if (method != null) {
        int modifiers = method.getModifiers();
        if ((java.lang.reflect.Modifier.isPrivate(modifiers)) || (java.lang.reflect.Modifier.isStatic(modifiers))
            || (java.lang.reflect.Modifier.isAbstract(modifiers)))
          method = null;
      }

      if (method == null)
        return null;
      
      final java.lang.reflect.Method _method = method;
      return new _NSMethod() {

        @Override
        public Object invoke(Object obj, Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
          return _method.invoke(obj, args);
        }
        
      };
    }
  }
}
