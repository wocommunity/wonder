package ns.foundation;

import java.io.Serializable;

public class NSNotification implements NSCoding, Serializable {
  private String _name;
  private Object _object;
  private NSDictionary<String, Object> _userInfo;

  @SuppressWarnings("unchecked")
  public NSNotification(String name, Object object, NSDictionary<String, Object> userInfo) {
    if (name == null) {
      throw new IllegalArgumentException("attempt to create notification without a name");
    }

    _name = name;
    _object = object;
    _userInfo = ((userInfo != null) ? userInfo : NSDictionary.EmptyDictionary);
  }

  public NSNotification(String name, Object object) {
    this(name, object, null);
  }

  public String name() {
    return _name;
  }

  public Object object() {
    return _object;
  }

  public NSDictionary<String, ?> userInfo() {
    return _userInfo;
  }

  @Override
  public String toString() {
    Object object = object();
    NSDictionary<String, ?> userInfo = userInfo();

    if ((object == null) && (userInfo == null)) {
      return "<" + getClass().toString() + "(name=" + name() + ")>";
    }
    if (object == null) {
      return "<" + getClass().toString() + "(name=" + name() + ", userInfo=" + userInfo + ")>";
    }
    if (userInfo == null) {
      return "<" + getClass().toString() + "(name=" + name() + ", object=" + object + ")>";
    }

    return "<" + getClass().toString() + "(name=" + name() + ", object=" + object + ", userInfo=" + userInfo + ")>";
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (other instanceof NSNotification) {
      NSNotification otherNotification = (NSNotification) other;
      if ((name().equals(otherNotification.name())) && (object() == otherNotification.object())) {
        NSDictionary<String, ?> userInfo = userInfo();
        NSDictionary<String, ?> otherUserInfo = otherNotification.userInfo();
        if (userInfo != otherUserInfo) {
          return (((userInfo == null) || (otherUserInfo == null)) ? false : userInfo.equals(otherUserInfo));
        }

        return true;
      }
    }
    return false;
  }

  @Override
  public Class<?> classForCoder() {
    return getClass();
  }

  @SuppressWarnings("unchecked")
  public static Object decodeObject(NSCoder coder) {
    String name = (String) coder.decodeObject();
    Object object = coder.decodeObject();
    NSDictionary<String, Object> userInfo = (NSDictionary<String, Object>) coder.decodeObject();

    return new NSNotification(name, object, userInfo);
  }

  @Override
  public void encodeWithCoder(NSCoder coder) {
    coder.encodeObject(name());
    coder.encodeObject(object());
    coder.encodeObject(userInfo());
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }
}