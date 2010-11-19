package ns.foundation;

import java.util.EnumSet;



public interface NSKeyValueObserving extends NSKeyValueCodingAdditions {
  
  public enum Options {
    New,
    Old,
    Initial,
    Prior;
    
    public static final EnumSet<Options> NewAndOld = EnumSet.of(New, Old);
  }
  
  public enum Changes {
    Setting,
    Insertion,
    Removal,
    Replacement
  }
  
  public enum SetMutation {
    Union,
    Minus,
    Intersect,
    Set
  }
  
  public class KeyValueChange {
    public EnumSet<Changes> kind;
    public Object newValue;
    public Object oldValue;
    public NSSet<Integer> indexes;
    public Boolean isPrior;
    
    public KeyValueChange() {
    }
    
    public KeyValueChange(Changes... changes) {
      kind = EnumSet.of(changes[0], changes);
    }

    public KeyValueChange(KeyValueChange changes) {
      this.kind = changes.kind;
      this.newValue = changes.newValue;
      this.oldValue = changes.oldValue;
      this.indexes = changes.indexes;
    }

    public KeyValueChange(EnumSet<Changes> change, NSSet<Integer> indexes) {
      this.kind = change;
      this.indexes = indexes;
    }
  }
    
  public static class DefaultImplementation {

    public static boolean automaticallyNotifiesObserversForKey(NSObservable targetObject, String key) {
      return true;
    }
    
    public static void addObserverForKeyPath(NSObservable targetObject, NSObserver observer, String keyPath, EnumSet<Options> options, Object context) {
      if (observer == null || keyPath == null || keyPath.length() == 0) {
        return;
      }
      
      KeyValueObservingProxy.proxyForObject(targetObject).addObserverForKeyPath(observer, keyPath, options, context);
    }

    public static void didChangeValueForKey(NSObservable targetObject, String key) { 
      if (key == null || key.length() == 0) {
        return;
      }
      
      KeyValueObservingProxy.proxyForObject(targetObject).sendNotificationsForKey(key, null, false);
    }

    public static void didChangeValuesAtIndexForKey(NSObservable targetObject, EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
      if (key == null) {
        return;
      }

      KeyValueObservingProxy.proxyForObject(targetObject).sendNotificationsForKey(key, null, false);
    }

    public static NSSet<String> keyPathsForValuesAffectingValueForKey(NSObservable targetObject, String key) {
      return NSSet.emptySet();
    }

    public static void removeObserverForKeyPath(NSObservable targetObject, NSObserver observer, String keyPath) {
      if (observer == null || keyPath == null || keyPath.length() == 0) {
        return;
      }
      
      KeyValueObservingProxy.proxyForObject(targetObject).removeObserverForKeyPath(observer, keyPath);
    }

    public static void willChangeValueForKey(NSObservable targetObject, String key) {
      if (key == null || key.length() == 0)
        return;
      
      KeyValueChange changeOptions = new KeyValueChange(Changes.Setting);
      KeyValueObservingProxy.proxyForObject(targetObject).sendNotificationsForKey(key, changeOptions, true);
    }

    public static void willChangeValuesAtIndexForKey(final NSObservable targetObject, final EnumSet<Changes> change, final NSSet<Integer> indexes, final String key) {
      if (key == null || key.length() == 0)
        return;

      KeyValueChange changeOptions = new KeyValueChange(change, indexes);
      KeyValueObservingProxy.proxyForObject(targetObject).sendNotificationsForKey(key, changeOptions, true);
    }

    public static void observeValueForKeyPath(NSObserver observer, String keyPath, NSObservable targetObject, KeyValueChange changes, Object context) {
    }
  }
  
  public static class Utility {

    public static NSObservable observable(Object object) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      if (object instanceof NSObservable) {
        return (NSObservable)object;
      }
      return KeyValueObservingProxy.proxyForObject(object);
    }
    
    public static void addObserverForKeyPath(Object object, NSObserver observer, String keyPath, EnumSet<Options> options, Object context) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      observable(object).addObserverForKeyPath(observer, keyPath, options, context);
    }

    public static boolean automaticallyNotifiesObserversForKey(Object object, String key) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      return observable(object).automaticallyNotifiesObserversForKey(key);
    }

    public static void didChangeValueForKey(Object object, String key) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      observable(object).didChangeValueForKey(key);
    }

    public static void didChangeValuesAtIndexForKey(Object object, EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      observable(object).didChangeValuesAtIndexForKey(change, indexes, key);
    }

    public static NSSet<String> keyPathsForValuesAffectingValueForKey(Object object, String key) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      return observable(object).keyPathsForValuesAffectingValueForKey(key);
    }

    public static void removeObserverForKeyPath(Object object, NSObserver observer, String keyPath) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      observable(object).removeObserverForKeyPath(observer, keyPath);
    }

    public static void willChangeValueForKey(Object object, String key) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      observable(object).willChangeValueForKey(key);
    }

    public static void willChangeValuesAtIndexForKey(Object object, EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      observable(object).willChangeValuesAtIndexForKey(change, indexes, key);
    }

    public static void observeValueForKeyPath(Object object, String keyPath, NSObservable targetObject, KeyValueChange changes, Object context) {
      if (object == null) {
        throw new IllegalArgumentException("Object cannot be null");
      }
      if (object instanceof NSObserver) {
        ((NSObserver)object).observeValueForKeyPath(keyPath, targetObject, changes, context);
      }
      throw new IllegalArgumentException("Object must implement NSObserver");
    }    
  }
  
  public static class KeyValueObservingProxy implements NSObservable {
    private static final NSMutableDictionary<Object, KeyValueObservingProxy> _proxyCache = new NSMutableDictionary<Object, KeyValueObservingProxy>();
    private static final NSMutableDictionary<Class<? extends Object>, NSMutableDictionary<String, NSMutableSet<String>>> _dependentKeys = new NSMutableDictionary<Class<? extends Object>, NSMutableDictionary<String, NSMutableSet<String>>>();
    private final Object _targetObject;
    private NSMutableDictionary<String, KeyValueChange> _changesForKey = new NSMutableDictionary<String, KeyValueChange>();
    private NSMutableDictionary<String, NSMutableDictionary<NSObserver, ObserverInfo>> _observersForKey = new NSMutableDictionary<String, NSMutableDictionary<NSObserver,ObserverInfo>>();
    int _changeCount = 0;
    
    public static KeyValueObservingProxy proxyForObject(Object object) {
      KeyValueObservingProxy proxy = _proxyCache.objectForKey(object);
      
      if (proxy != null) {
        return proxy;
      }
      
      proxy = new KeyValueObservingProxy(object);
      _proxyCache.setObjectForKey(proxy, object);
      return proxy;
    }
    
    private KeyValueObservingProxy(Object object) {
      _targetObject = object;
    }

    private NSObservable observable() {
      if (_targetObject instanceof NSObservable) {
        return (NSObservable)_targetObject;
      }
      return this;
    }
    
    @Override
    public void addObserverForKeyPath(NSObserver observer, String keyPath, EnumSet<Options> options, Object context) {
      if (observer == null)
        return;
      
      KeyValueForwardingObserver forwarder = null;
      if (keyPath.contains(".")) {
        forwarder = new KeyValueForwardingObserver(keyPath, observable(), observer, options, context);
      } else {
        addDependentKeysForKey(keyPath);
      }
      
      NSMutableDictionary<NSObserver, ObserverInfo> observers = _observersForKey.objectForKey(keyPath);
      if (observers == null) {
        observers = new NSMutableDictionary<NSObserver, ObserverInfo>();
        _observersForKey.setObjectForKey(observers, keyPath);
        NSKeyValueCoding.DefaultImplementation._addKVOAdditionsForKey(_targetObject, keyPath);
      }
      observers.setObjectForKey(new ObserverInfo(observer, options, context, forwarder), observer);

      if (options.contains(Options.Initial)) {
        Object _newValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(_targetObject, keyPath);
        if (_newValue == null)
          _newValue = NSKeyValueCoding.NullValue;
        
        final Object value = _newValue;
        KeyValueChange changes = new KeyValueChange() {{ this.newValue = value; }};

        observer.observeValueForKeyPath(keyPath, observable(), changes, context);
      } 
    }

    private void addDependentKeysForKey(String key) {
      NSSet<String> composedOfKeys = NSKeyValueObserving.Utility.keyPathsForValuesAffectingValueForKey(_targetObject,key);
      
      NSMutableDictionary<String, NSMutableSet<String>> dependentKeysForClass = _dependentKeys.objectForKey(_targetObject.getClass());      
      if (dependentKeysForClass == null) {
        dependentKeysForClass = new NSMutableDictionary<String, NSMutableSet<String>>();
        _dependentKeys.setObjectForKey(dependentKeysForClass, _targetObject.getClass());
      }
      
      for (String componentKey : composedOfKeys) {
        NSMutableSet<String> keysComposedOfKey = dependentKeysForClass.objectForKey(componentKey);
        if (keysComposedOfKey == null) {
          keysComposedOfKey = new NSMutableSet<String>();
          dependentKeysForClass.setObjectForKey(keysComposedOfKey, componentKey);
        }
        
        keysComposedOfKey.addObject(key);
        addDependentKeysForKey(componentKey);
      }

    }
    
    @Override
    public void removeObserverForKeyPath(NSObserver observer, String keyPath) {
      NSMutableDictionary<NSObserver, ObserverInfo> observers = _observersForKey.objectForKey(keyPath);
      if (keyPath.contains(".")) {
        KeyValueForwardingObserver forwarder = observers.objectForKey(observer).forwarder;
        forwarder.destroy();
      }
      
      observers.removeObjectForKey(observer);
      if (observers.isEmpty()) {
        _observersForKey.removeObjectForKey(keyPath);
        NSKeyValueCoding.DefaultImplementation._removeKVOAdditionsForKey(_targetObject, keyPath);
      }
      if (_observersForKey.isEmpty())
        _proxyCache.removeObjectForKey(_targetObject);
    }
    
    @SuppressWarnings("unchecked")
    public void sendNotificationsForKey(String key, KeyValueChange changeOptions, boolean isBefore) {
      KeyValueChange changes = _changesForKey.objectForKey(key);

      if (isBefore) {
        changes = new KeyValueChange(changeOptions);
        
        NSSet<Integer> indexes = changes.indexes;
        if (indexes != null) {
          EnumSet<Changes> type = changes.kind;
          if (type.contains(Changes.Replacement) || type.contains(Changes.Removal)) {
            NSMutableArray<Object> oldValues = new NSMutableArray<Object>((NSArray<Object>)NSKeyValueCodingAdditions.Utility.valueForKeyPath(_targetObject, key));
            changes.oldValue = oldValues;
          }
        } else {
          Object oldValue = NSKeyValueCoding.Utility.valueForKey(_targetObject, key);
          
          if (oldValue == null)
            oldValue = NSKeyValueCoding.NullValue;
          
          changes.oldValue = oldValue;
        }
        
        changes.isPrior = true;
        _changesForKey.setObjectForKey(changes, key);
      } else {
        changes.isPrior = null;
        NSSet<Integer> indexes = changes.indexes;
        
        if (indexes != null) {
          EnumSet<Changes> type = changes.kind;
          if (type.contains(Changes.Replacement) || type.contains(Changes.Insertion)) {
            NSMutableArray<Object> newValues = new NSMutableArray<Object>((NSArray<Object>)NSKeyValueCodingAdditions.Utility.valueForKeyPath(_targetObject, key));
            changes.newValue = newValues;
          }
        } else {
          Object newValue = NSKeyValueCoding.Utility.valueForKey(_targetObject, key);
          if (newValue == null)
            newValue = NSKeyValueCoding.NullValue;
          changes.newValue = newValue;
        }
        //FIXME - Is this safe? Sink the change notification if nothing actually changed.
        if (changes.oldValue == changes.newValue)
          return;
      }
      
      
      NSArray<ObserverInfo> observers;        
      if (_observersForKey.containsKey(key)) {
        observers = _observersForKey.objectForKey(key).allValues();
      } else {
        observers = new NSArray<ObserverInfo>();
      }

      int count = observers.count();
      while (count-- > 0) {
        ObserverInfo observerInfo = observers.objectAtIndex(count);
        if (isBefore && (observerInfo.options.contains(Options.Prior))) {
          observerInfo.observer.observeValueForKeyPath(key, observable(), changes, observerInfo.context);
        } else if (!isBefore) {
          observerInfo.observer.observeValueForKeyPath(key, observable(), changes, observerInfo.context);          
        }        
      }
      
      NSSet<String> keysComposedOfKey = null;
      if (_dependentKeys.containsKey(_targetObject.getClass()))
          keysComposedOfKey = _dependentKeys.objectForKey(_targetObject.getClass()).objectForKey(key);
      if (keysComposedOfKey == null || keysComposedOfKey.isEmpty())
        return;
      
      for (String dependentKey : keysComposedOfKey) {
        sendNotificationsForKey(dependentKey, changeOptions, isBefore);
      }
    }

    @Override
    public boolean automaticallyNotifiesObserversForKey(String key) {
      return true;
    }

    @Override
    public void didChangeValueForKey(String key) {
      if (--_changeCount == 0)
        NSKeyValueObserving.DefaultImplementation.didChangeValueForKey(this, key);
    }

    @Override
    public void didChangeValuesAtIndexForKey(EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
      NSKeyValueObserving.DefaultImplementation.didChangeValuesAtIndexForKey(this, change, indexes, key);
    }

    @Override
    public NSSet<String> keyPathsForValuesAffectingValueForKey(String key) {
      return NSSet.emptySet();
    }

    @Override
    public void willChangeValueForKey(String key) {
      _changeCount++;
      NSKeyValueObserving.DefaultImplementation.willChangeValueForKey(this, key);
    }

    @Override
    public void willChangeValuesAtIndexForKey(EnumSet<Changes> change, NSSet<Integer> indexes, String key) {
      NSKeyValueObserving.DefaultImplementation.willChangeValuesAtIndexForKey(this, change, indexes, key);
    }

    @Override
    public void takeValueForKeyPath(Object value, String keyPath) {
      NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(_targetObject, value, keyPath);
    }

    @Override
    public Object valueForKeyPath(String keyPath) {
      return NSKeyValueCodingAdditions.Utility.valueForKeyPath(_targetObject, keyPath);
    }

    @Override
    public void takeValueForKey(Object value, String key) {
      NSKeyValueCoding.Utility.takeValueForKey(_targetObject, value, key);
    }

    @Override
    public Object valueForKey(String key) {
      return NSKeyValueCoding.Utility.valueForKey(_targetObject, key);
    }
  }
    
  public static class ObserverInfo {
    public final NSObserver observer;
    public final EnumSet<Options> options;
    public final Object context;
    public final KeyValueForwardingObserver forwarder;
    
    public ObserverInfo(NSObserver observer, EnumSet<Options> options, Object context, KeyValueForwardingObserver forwarder) {
      this.observer = observer;
      this.options = options;
      this.context = context;
      this.forwarder = forwarder;
    }
  }
  
  public static class KeyValueForwardingObserver implements NSObserver {
    private final NSObservable _targetObject;
    private final NSObserver _observer;
    private final EnumSet<Options> _options;
    
    private final String _firstPart;
    private final String _secondPart;
    
    private NSObservable _value;
    
    public KeyValueForwardingObserver(String keyPath, NSObservable object, NSObserver observer, EnumSet<Options> options, Object context) {
      _observer = observer;
      _targetObject = object;
      _options = options;

      if (!keyPath.contains("."))
        throw new IllegalArgumentException("Created KeyValueForwardingObserver without a compound key path: " + keyPath);
      
      int index = keyPath.indexOf('.');
      _firstPart = keyPath.substring(0, index);
      _secondPart = keyPath.substring(index+1);
      
      _targetObject.addObserverForKeyPath(this, keyPath, options, context);
      
      _value = (NSObservable) _targetObject.valueForKey(_firstPart);
      if (_value != null) {
        _value.addObserverForKeyPath(this, keyPath, options, context);
      }
    }

    @Override
    public void observeValueForKeyPath(String keyPath, NSObservable targetObject, KeyValueChange changes, Object context) {
      if (targetObject == _targetObject) {
        _observer.observeValueForKeyPath(_firstPart, targetObject, changes, context);
        
        Object newValue = _targetObject.valueForKeyPath(_secondPart);
        if (_value != null && _value != newValue)
          _value.removeObserverForKeyPath(this, _secondPart);
        
        _value = (NSObservable) newValue;
        if (_value != null)
          _value.addObserverForKeyPath(this, _secondPart, _options, context);
      } else {
        _observer.observeValueForKeyPath(_firstPart+"."+keyPath, targetObject, changes, context);
      }
    }

    private void destroy() {
      if (_value != null)
        _value.removeObserverForKeyPath(this, _secondPart);
      _targetObject.removeObserverForKeyPath(this, _firstPart);

      _value = null;
    }
  }

  public interface _NSKeyValueObserving {
  }

}
