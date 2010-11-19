package ns.foundation;

import java.io.Serializable;

public interface _NSObject extends NSKeyValueCoding, NSKeyValueCodingAdditions, NSKeyValueObserving, NSObservable, NSObserver, Cloneable, Serializable {
  public boolean respondsToSelector(NSSelector<?> selector);
}
