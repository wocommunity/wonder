package ns.foundation;

public interface NSValidation {

  public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException;

  public Object validateValueForKey(Object value, String key) throws ValidationException;

  public static class ValidationException extends RuntimeException {

    public ValidationException(String message) {
      super(message);
    }
  }
}
