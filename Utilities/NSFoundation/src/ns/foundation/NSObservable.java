package ns.foundation;

import java.util.EnumSet;


public interface NSObservable extends NSKeyValueObserving {
  public boolean automaticallyNotifiesObserversForKey(String key);

  public void addObserverForKeyPath(NSObserver observer, String keyPath, EnumSet<Options> options, Object context);
  
  public void didChangeValuesAtIndexForKey(EnumSet<Changes> change, NSSet<Integer> indexes, String key);

  public void didChangeValueForKey(String key);

  public void removeObserverForKeyPath(NSObserver observer, String keyPath);

  public void willChangeValuesAtIndexForKey(EnumSet<Changes> change, NSSet<Integer> indexes, String key);

  public void willChangeValueForKey(String key);

  public NSSet<String> keyPathsForValuesAffectingValueForKey(String key);

}
