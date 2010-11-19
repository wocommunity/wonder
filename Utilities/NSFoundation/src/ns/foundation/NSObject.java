package ns.foundation;

import java.util.EnumSet;

public class NSObject implements _NSObject {

  public NSObject self() {
    return this;
  }
  
  public final void set(String key, Object value) {
    takeValueForKey(value, key);
  }
  
  public final void get(String key) {
    valueForKey(key);
  }
  
  @Override
  public void takeValueForKey(Object value, String key) {
    NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
  }

  @Override
  public void takeValueForKeyPath(Object value, String keyPath) {
    NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);    
  }

  @Override
  public Object valueForKey(String key) {
    return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
  }

  @Override
  public Object valueForKeyPath(String keyPath) {
    return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
  }

  @Override
  public void observeValueForKeyPath(String keyPath, NSObservable targetObject, KeyValueChange changes, Object context) {
    // Do nothing
  }

  @Override
  public void addObserverForKeyPath(NSObserver observer, String keyPath, EnumSet<Options> options, Object context) {
    NSKeyValueObserving.DefaultImplementation.addObserverForKeyPath(this, observer, keyPath, options, context);
  }
  
  @Override
  public boolean automaticallyNotifiesObserversForKey(String key) {
    return true;
  }

  @Override
  public void didChangeValueForKey(String key) {
    NSKeyValueObserving.DefaultImplementation.didChangeValueForKey(this, key);
  }

  @Override
  public void didChangeValuesAtIndexForKey(EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
    NSKeyValueObserving.DefaultImplementation.didChangeValuesAtIndexForKey(this, change, indexes, key);
  }

  @Override
  public NSSet<String> keyPathsForValuesAffectingValueForKey(String key) {
    return NSKeyValueObserving.DefaultImplementation.keyPathsForValuesAffectingValueForKey(this, key);
  }

  @Override
  public void removeObserverForKeyPath(NSObserver observer, String keyPath) {
    NSKeyValueObserving.DefaultImplementation.removeObserverForKeyPath(this, observer, keyPath);
  }

  @Override
  public void willChangeValueForKey(String key) {
    NSKeyValueObserving.DefaultImplementation.willChangeValueForKey(this, key);
  }

  @Override
  public void willChangeValuesAtIndexForKey(EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
    NSKeyValueObserving.DefaultImplementation.willChangeValuesAtIndexForKey(this, change, indexes, key);
  }

  public Object handleQueryWithUnboundKey(String paramString) {
    return null;
  }

  @Override
  public boolean respondsToSelector(NSSelector<?> selector) {
    return NSSelector.respondsToSelector(this, selector);
  }
}
