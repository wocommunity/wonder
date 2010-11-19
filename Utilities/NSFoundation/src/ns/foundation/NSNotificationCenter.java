package ns.foundation;

import java.util.Collection;
import java.util.HashSet;

public class NSNotificationCenter {
  private static final NSNotificationCenter _defaultCenter = new NSNotificationCenter();

  private final NSMutableDictionary<String, NotificationRegistry> _namedRegistries = new NSMutableDictionary<String, NotificationRegistry>();
  private final NotificationRegistry _unnamedRegistry = new NotificationRegistry();

  private static void postNotification(NSNotificationCenter notificationCenter, NSNotification notification) {
    notificationCenter._unnamedRegistry.postNotification(notification);
    notificationCenter._namedRegistries.objectForKey(notification.name()).postNotification(notification);
  }
  
  public static NSNotificationCenter defaultCenter() {
    return _defaultCenter;
  }

  private void _checkSelector(NSSelector<?> selector) {
    Class<?>[] parameterTypes = selector.parameterTypes();
    boolean invalid = false;
    if (parameterTypes == null || parameterTypes.length != 1) {
      invalid = true;
    } else {
      Class<?> arg = parameterTypes[0];
      if (!_NSReflectionUtilities.classIsAssignableFrom(arg, NSNotification.class)) {
        invalid = true;
      }
    }
    if (invalid) {
      throw new IllegalArgumentException("NSNotificationCenter addObserver() requires a selector taking a single NSNotification argument");
    }
  }
  
  public synchronized void addObserver(NSSelectable observer, NSSelector<?> selector, String name, Object object) {
    if (observer == null || selector == null) {
      throw new IllegalArgumentException("NSNotificationCenter addObserver() requires non null observer and selector parameters");
    } 
    _checkSelector(selector);
    
    NotificationRegistry registry;
    NotificationObserver _observer = new NotificationObserver(observer, selector);
    if (name == null) {
      registry = _unnamedRegistry;
    } else if ((registry = _namedRegistries.objectForKey(name)) == null) {
      registry = new NotificationRegistry();
      _namedRegistries.setObjectForKey(registry, name);
    }
    
    registry.addObserver(_observer, object);
  }

  public void postNotification(NSNotification notification) {
    if (notification == null) {
      throw new IllegalArgumentException("Notification cannot be null");
    }
    
    NSNotificationCenter.postNotification(this, notification);
  }

  public void postNotification(String notificationName, Object notificationObject) {
    postNotification(new NSNotification(notificationName, notificationObject, null));
  }

  public void postNotification(String notificationName, Object notificationObject, NSDictionary<String, Object> userInfo) {
    postNotification(new NSNotification(notificationName, notificationObject, userInfo));
  }

  public void removeObserver(Object observer) {
    removeObserver(observer, null, null);
  }

  public synchronized void removeObserver(Object observer, String name, Object object) {
    if (observer == null && name == null && object == null) {
      throw new IllegalArgumentException("Can't remove entry with null observer, null name, and null object from NSNotificationCenter");
    }
    
    if (name == null) {
      for (String key : _namedRegistries.keySet()) {
        _namedRegistries.objectForKey(key).removeObserver(observer, object);
      }
      
      _unnamedRegistry.removeObserver(observer, object);
    } else {
      _namedRegistries.objectForKey(name).removeObserver(observer, object);
    }
  }

  @Override
  public String toString() {
    return super.toString();
  }

  private static class NotificationRegistry {
    private NSMutableDictionary<Integer, NSMutableArray<NotificationObserver>> _objectObservers = new NSMutableDictionary<Integer, NSMutableArray<NotificationObserver>>();
    private boolean _observerRemoval = false;
    private NSArray<NotificationObserver> _postingObservers;
    
    public void addObserver(NotificationObserver observer, Object _object) {
      Object object = _object;
      if (object == null) {
        object = NSKeyValueCoding.NullValue;
      }
      
      NSMutableArray<NotificationObserver> observers = _objectObservers.objectForKey(object.hashCode());
      if (observers == null) {
        observers = new NSMutableArray<NotificationObserver>();
        _objectObservers.setObjectForKey(observers, object.hashCode());
      }
      
      if (observers == _postingObservers) {
        _postingObservers = observers.immutableClone();
      }
      
      observers.add(observer);
    }
    
    public void removeObserver(Object observer, Object object) {
      NSMutableArray<Integer> removedKeys = new NSMutableArray<Integer>();
      
      Collection<Integer> keys;
      if (object == null) {
        keys = _objectObservers.keySet();
      } else { 
        keys = new HashSet<Integer>(object.hashCode());
      }
        
      for(Integer key : keys) {
        NSArray<NotificationObserver> observers = _objectObservers.objectForKey(key);
        if (observers != null) {
          int index = observers.count();
          while(index-- > 0) {
            if (observers.objectAtIndex(index).observer() == observer) {
              _observerRemoval = true;
              if (observers == _postingObservers) {
                _postingObservers = observers.immutableClone();
              }

              observers.remove(index);
            }
          }
        }

        if (observers == null || observers.count() == 0) {
          removedKeys.add(key);
        }
      }
      
      for (Integer key : removedKeys) {
        _objectObservers.removeObjectForKey(key);
      }
    }
    
    public void postNotification(NSNotification notification) {
      Object object = notification.object();
      
      Collection<Object> keys = new HashSet<Object>();
      keys.add(NSKeyValueCoding.NullValue.hashCode());
      if (object != null) {
        keys.add(object.hashCode());
      }
      
      for (Object key : keys) {
        _postingObservers = _objectObservers.objectForKey(key);
        if (_postingObservers == null) {
          continue;
        }

        NSArray<NotificationObserver> observers = _postingObservers;
        
        _observerRemoval = false;
        int index = observers.count();
        while (index-- > 0) {
          NotificationObserver observer = _postingObservers.get(index);
          if (!_observerRemoval || observers.indexOfIdenticalObject(observer) != NSArray.NotFound) {
            observer.postNotification(notification);
          }
        }
      }
      _postingObservers = null;
    }
  }

  private static class NotificationObserver {
    private final NSSelectable _observer;
    private final NSSelector<?> _selector;
    
    public NotificationObserver(NSSelectable observer, NSSelector<?> selector) {
      _observer = observer;
      _selector = selector;
    }
    
    public NSSelectable observer() {
      return _observer;
    }
    
    public void postNotification(NSNotification notification) {
      NSSelector._safeInvokeSelector(_selector, _observer, notification);
    }
  }
}