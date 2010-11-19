package ns.foundation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import ns.foundation._private._NSMethod;


public class NSSelector<T> implements Serializable {
  protected static final Class<?>[] _NoClassArray = new Class[0];
  protected static final Object[] _NoObjectArray = new Object[0];

  protected String _name;
  protected Class<?>[] _types;
  private transient Class<?> _cachedClass;
  private transient _NSMethod _cachedMethod;
  private transient NSMutableDictionary<String, _NSMethod> _classToMethodMapTable = new NSMutableDictionary<String, _NSMethod>(8);

  public static <T> T _safeInvokeSelector(NSSelector<T> selector, Object receiver, Object... parameters) {
    try {
      return selector.invoke(receiver, parameters != null ? parameters : _NoObjectArray);
    } catch (InvocationTargetException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Either the receiver or the method is not public: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  protected NSSelector() {

  }

  public NSSelector(String name) {
    this(name, (Class[]) null);
  }

  public String name() {
    return _name;
  }

  public NSSelector(String name, Class<?>... parameterTypes) {
    if (name == null) {
      throw new IllegalArgumentException("Selector name cannot be null");
    }

    _name = name;
    if (parameterTypes != null) {
      _types = new Class[parameterTypes.length];
      System.arraycopy(parameterTypes, 0, _types, 0, parameterTypes.length);
    } else {
      _types = _NoClassArray;
    }
  }

  @SuppressWarnings("unchecked")
  public T invoke(Object target, Object... parameters) throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    _NSMethod method = methodOnObject(target);
    return (T) method.invoke(target, parameters != null ? parameters : new Object[0]);
  }

  public T invoke(Object target) throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return invoke(target, _NoObjectArray);
  }

  public static Object invoke(String name, Class<?> parameterTypes[], Object target, Object... parameters) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    NSSelector<?> selector = new NSSelector<Object>(name, parameterTypes);
    return selector.invoke(target, parameters);
  }

  public static Object invoke(String name, Class<?> parameterType, Object target, Object argument) throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException {
    if (parameterType == null) {
      throw new IllegalArgumentException("Parameter type cannot be null");
    }
    NSSelector<?> selector = new NSSelector<Object>(name, parameterType);
    return selector.invoke(target, argument);
  }

  public static Object invoke(String name, Class<?> parameterType1, Class<?> parameterType2, Object target, Object argument1, Object argument2)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    if (parameterType1 == null || parameterType2 == null) {
      throw new IllegalArgumentException("Neither parameter type can be null");
    }
    NSSelector<?> selector = new NSSelector<Object>(name, parameterType1, parameterType2);
    return selector.invoke(target, argument1, argument2);
  }

  public Class<?>[] parameterTypes() {
    if (_types.length == 0) {
      return _types;
    }

    Class<?>[] types = new Class[_types.length];
    System.arraycopy(_types, 0, types, 0, _types.length);
    return types;
  }

  private synchronized _NSMethod _methodOnObject(Object targetObject) {
    Class<?> targetClass = targetObject.getClass();
    if (targetClass == _cachedClass) {
      return _cachedMethod;
    }

    String className = targetClass.getName();
    Object value = _classToMethodMapTable.objectForKey(className);
    if (value == null) {
      try {
        value = _NSReflectionUtilities.methodOnObject(targetObject, _name, _types);
      } catch (NoSuchMethodException exception) {
        exception.printStackTrace();
      }

      if (value == null)
        value = NSKeyValueCoding.NullValue;

      _classToMethodMapTable.takeValueForKey(value, className);

    }

    _NSMethod method = null;
    if (value != NSKeyValueCoding.NullValue) {
      method = (_NSMethod) value;
    }

    _cachedClass = targetClass;
    _cachedMethod = method;
    return method;
  }

  private _NSMethod methodOnObject(Object targetObject) throws NoSuchMethodException {
    if (targetObject == null)
      throw new IllegalArgumentException("Target object cannot be null");

    _NSMethod method = _methodOnObject(targetObject);
    Class<?> targetClass = targetObject.getClass();
    if (method == null)
      throw new NoSuchMethodException("Class " + targetClass.getName() + " does not implement method " + _NSReflectionUtilities._methodSignature(_name, _types));

    return method;
  }

  public static boolean respondsToSelector(Object object, NSSelector<?> selector) {
    return selector._methodOnObject(object) != null;
  }

}