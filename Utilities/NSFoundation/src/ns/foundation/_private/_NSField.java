package ns.foundation._private;

public abstract class _NSField {
  public abstract Object get(Object object) throws IllegalArgumentException, IllegalAccessException;

  public abstract void set(Object object, Object value) throws IllegalArgumentException, IllegalAccessException;
}
