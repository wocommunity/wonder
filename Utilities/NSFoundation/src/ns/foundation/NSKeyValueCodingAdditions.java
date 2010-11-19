package ns.foundation;

public interface NSKeyValueCodingAdditions extends NSKeyValueCoding {

  public static final String KeyPathSeparator = ".";
  public static final char _KeyPathSeparatorChar = '.';

  public void takeValueForKeyPath(Object value, String keyPath);

  public Object valueForKeyPath(String keyPath);

  public static abstract class DefaultImplementation {
    public static Object valueForKeyPath(Object object, String keyPath) {
      if (keyPath == null) {
        return null;
      }
      int index = keyPath.indexOf(_KeyPathSeparatorChar);
      if (index < 0) {
        return NSKeyValueCoding.Utility.valueForKey(object, keyPath);
      }

      String key = keyPath.substring(0, index);
      Object value = NSKeyValueCoding.Utility.valueForKey(object, key);
      return ((value == null) ? null : NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, keyPath.substring(index + 1)));
    }

    public static void takeValueForKeyPath(Object object, Object value, String keyPath) {
      if (keyPath == null) {
        throw new IllegalArgumentException("Key path cannot be null");
      }

      int index = keyPath.indexOf(_KeyPathSeparatorChar);
      if (index < 0) {
        NSKeyValueCoding.Utility.takeValueForKey(object, value, keyPath);
      } else {
        String key = keyPath.substring(0, index);
        Object targetObject = NSKeyValueCoding.Utility.valueForKey(object, key);
        if (targetObject != null)
          NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(targetObject, value, keyPath.substring(index + 1));
      }
    }
  }

  public static abstract class Utility {
    public static Object valueForKeyPath(Object object, String keyPath) {
      if (object == null)
        throw new IllegalArgumentException("Object cannot be null");
      if (object instanceof NSKeyValueCodingAdditions)
        return ((NSKeyValueCodingAdditions) object).valueForKeyPath(keyPath);

      return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(object, keyPath);
    }

    public static void takeValueForKeyPath(Object object, Object value, String keyPath) {
      if (object == null)
        throw new IllegalArgumentException("Object cannot be null");
      if (object instanceof NSKeyValueCodingAdditions)
        ((NSKeyValueCodingAdditions) object).takeValueForKeyPath(value, keyPath);
      else
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(object, value, keyPath);
    }
  }
}
