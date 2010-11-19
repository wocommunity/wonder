package ns.foundation._private;

import java.lang.reflect.InvocationTargetException;


public abstract class _NSMethod {
  public abstract Object invoke(Object object, Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
}
