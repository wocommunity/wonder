package ns.foundation;

import ns.foundation.NSKeyValueObserving.KeyValueChange;

public interface NSObserver {

  public void observeValueForKeyPath(String keyPath, NSObservable targetObject, KeyValueChange changes, Object context);

}