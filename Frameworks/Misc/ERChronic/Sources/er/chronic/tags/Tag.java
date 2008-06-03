package er.chronic.tags;

import java.util.Calendar;

/**
 * Tokens are tagged with subclassed instances of this class when
 * they match specific criteria
 */
public class Tag<T> {
  private T _type;
  private Calendar _now;

  public Tag(T type) {
    _type = type;
  }

  public Calendar getNow() {
    return _now;
  }

  public void setType(T type) {
    _type = type;
  }

  public T getType() {
    return _type;
  }

  public void setStart(Calendar s) {
    _now = s;
  }
}
