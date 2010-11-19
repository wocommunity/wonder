package ns.foundation;


import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Fully implemented except for NSKeyValueCodingAdditions
 */
public class NSDictionary<K, V> extends AbstractMap<K, V> implements Map<K, V>, NSKeyValueCoding, NSKeyValueCodingAdditions, _NSFoundationCollection,
    Cloneable, Serializable {

  private static final long serialVersionUID = -179272320896439111L;

  @SuppressWarnings("rawtypes")
  public static final NSDictionary EmptyDictionary = new NSDictionary();

  public static final boolean CheckForNull = true;
  public static final boolean IgnoreNull = true;
  protected static final String NULL_NOT_ALLOWED = "Attempt to insert null into an NSDictionary.";
  protected Set<Map.Entry<K, V>> _entrySetCache;

  private Map<K, V> _backingStore;

  public NSDictionary() {
    _initializeWithCapacity(0);
  }

  protected NSDictionary(int capacity) {
    _initializeWithCapacity(capacity);
  }

  public NSDictionary(Map<K, V> map) {
    _initializeWithMap(map, NullHandling.CheckAndFail);
  }

  public NSDictionary(Map<K, V> map, boolean ignoreNull) {
    _initializeWithMap(map, ignoreNull ? NullHandling.NoCheck : NullHandling.CheckAndFail);
  }

  
  @SuppressWarnings("unchecked")
  public NSDictionary(NSArray<V> objects, NSArray<K> keys) {
    this((V[]) (objects != null ? objects.toArray() : null), (K[]) (keys != null ? keys.toArray() : null));
  }

  public NSDictionary(NSDictionary<K, V> otherDictionary) {
    this((Map<K, V>) otherDictionary);
  }

  public NSDictionary(V[] objects, K[] keys) {
    _initFromKeyValues(objects, keys, NullHandling.CheckAndFail);
  }

  public NSDictionary(V object, K key) {
    if (key == null || object == null)
      throw new IllegalArgumentException("Object or key may not be null");

    _initializeWithCapacity(1).put(key, object);
  }

  protected Map<K, V> _initializeWithCapacity(int capacity) {
    Map<K, V> map = new HashMap<K, V>(capacity);
    _setMap(Collections.unmodifiableMap(map));
    return map;
  }

  protected void _initializeWithMap(Map<K, V> map, NullHandling nullHandling) {
    Map<K, V> store = _initializeWithCapacity(map.size());
    if (map instanceof NSDictionary<?, ?> || nullHandling == NullHandling.NoCheck)
      store.putAll(map);
    else {
      for (Map.Entry<K, V> entry : map.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) {
          if (nullHandling == NullHandling.CheckAndFail)
            throw new IllegalArgumentException("Key or value may not be null");
          continue;
        }
        store.put(entry.getKey(), entry.getValue());
      }
    }
  }

  protected void _initFromKeyValues(V[] objects, K[] keys, NullHandling nullHandling) {
    if (objects == null && keys == null) {
      _initializeWithCapacity(0);
      return;
    }

    if (keys == null || objects == null)
      throw new IllegalArgumentException("Both objects and keys cannot be null");

    if (objects.length != keys.length) {
      throw new IllegalArgumentException("Attempt to create an " + getClass().getName() + " with a different number of objects and keys.");
    }
    Map<K, V> store = _initializeWithCapacity(objects.length);
    if (nullHandling != NullHandling.NoCheck) {
      for (int i = 0; i < objects.length; i++) {
        if (objects[i] == null || keys[i] == null) {
          if (nullHandling == NullHandling.CheckAndFail)
            throw new IllegalArgumentException("Attempt to insert a null into an  " + getClass().getName() + ".");
          continue;
        }
        store.put(keys[i], objects[i]);
      }
    } else {
      for (int i = 0; i < objects.length; i++) {
        store.put(keys[i], objects[i]);
      }
    }
  }

  protected static <K, V, T extends NSDictionary<K, V>> T _initializeDictionaryWithMap(T dict, Map<K, V> map, NullHandling nullHandling) {
    if (map == null)
      throw new IllegalArgumentException("map may not be null");

    if (!(map instanceof NSDictionary<?, ?>) && nullHandling != NullHandling.NoCheck && map.size() > 0) {
      try {
        if (map.containsValue(null) || map.containsKey(null)) {
          if (nullHandling == NullHandling.CheckAndFail)
            throw new IllegalArgumentException(NULL_NOT_ALLOWED);
          dict._initializeWithMap(map, nullHandling);
          return dict;
        }
      } catch (NullPointerException e) {
        // Must not support nulls either
      }
    }

    dict._setMap(map);
    return dict;
  }

  protected Map<K, V> mapNoCopy() {
    return _backingStore;
  }

  protected Object[] keysNoCopy() {
    return mapNoCopy().keySet().toArray();
  }

  protected Object[] objectsNoCopy() {
    return mapNoCopy().values().toArray();
  }
  
  protected Map<K, V> _setMap(Map<K, V> map) {
    return _backingStore = map;
  }

  public static <K, V> NSDictionary<K, V> asDictionary(Map<K, V> map) {
    return asDictionary(map, NullHandling.CheckAndFail);
  }

  public static <K, V> NSDictionary<K, V> asDictionary(Map<K, V> map, NullHandling nullHandling) {
    if (map == null || map.size() == 0)
      return emptyDictionary();
    if (map.getClass() == NSDictionary.class)
      return (NSDictionary<K, V>) map;
    return _initializeDictionaryWithMap(new NSDictionary<K, V>(), Collections.unmodifiableMap(map), nullHandling);
  }

  public static <K, V> NSMutableDictionary<K, V> asMutableDictionary(Map<K, V> map) {
    return asMutableDictionary(map, NullHandling.CheckAndFail);
  }

  public static <K, V> NSMutableDictionary<K, V> asMutableDictionary(Map<K, V> map, NullHandling nullHandling) {
    if (map == null || map.size() == 0)
      return new NSMutableDictionary<K, V>();
    if (map instanceof NSMutableDictionary<?, ?>)
      return (NSMutableDictionary<K, V>) map;
    return _initializeDictionaryWithMap(new NSMutableDictionary<K, V>(), map, nullHandling);
  }

  public NSArray<K> allKeys() {
    return new NSArray<K>(keySet());
  }

  public NSArray<K> allKeysForObject(Object object) {
    if (object == null)
      return NSArray.emptyArray();

    NSMutableArray<K> result = new NSMutableArray<K>();

    for (Map.Entry<K, V> entry : entrySet()) {
      if (object.equals(entry.getValue())) {
        result.add(entry.getKey());
      }
    }

    return result;
  }

  public NSArray<V> allValues() {
    return new NSArray<V>(values());
  }

  public int count() {
    return mapNoCopy().size();
  }

  @SuppressWarnings("unchecked")
  public static <K, V> NSDictionary<K, V> emptyDictionary() {
    return EmptyDictionary;
  }

  public HashMap<K, V> hashMap() {
    return new HashMap<K, V>(mapNoCopy());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this || obj == mapNoCopy())
      return true;
    if (obj instanceof NSDictionary<?, ?> && mapNoCopy() == ((NSDictionary<?, ?>) obj).mapNoCopy())
      return true;
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return _shallowHashCode() ^ count();
  }

  @Override
  public int _shallowHashCode() {
    return NSDictionary.class.hashCode();
  }

  public boolean isEqualToDictionary(NSDictionary<?, ?> otherDictionary) {
    return equals(otherDictionary);
  }

  public NSDictionary<K, V> immutableClone() {
    return this;
  }

  public NSMutableDictionary<K, V> mutableClone() {
    return new NSMutableDictionary<K, V>(mapNoCopy());
  }

  public Enumeration<K> keyEnumerator() {
    return allKeys().objectEnumerator();
  }

  public Enumeration<V> objectEnumerator() {
    return allValues().objectEnumerator();
  }

  public V objectForKey(Object key) {
    return mapNoCopy().get(key);
  }

  public NSArray<V> objectsForKeys(NSArray<K> keys, V notFoundMarker) {
    if (keys != null) {
      NSMutableArray<V> array = new NSMutableArray<V>(keys.size());
      for (K key : keys) {
        V object = objectForKey(key);
        if (object != null) {
          array.addObject(object);
          continue;
        }
        if (notFoundMarker != null) {
          array.addObject(notFoundMarker);
        }
      }
      return array;
    }
    return NSArray.emptyArray();
  }

  @Override
  public Object valueForKey(String key) {
    Object value = objectForKey(key);
    if ((value == null) && (key != null)) {
      if (key.equals("allValues"))
        return allValues();
      if (key.equals("allKeys"))
        return allKeys();
      if (key.equals("count"))
        return count();
    }

    return value;
  }

  @Override
  public void takeValueForKey(Object value, String key) {
    throw new IllegalStateException(super.getClass().getName() + " is immutable.");
  }

  @Override
  public Object valueForKeyPath(String keyPath) {
    Object flattenedKeyPresent = objectForKey(keyPath);
    if (flattenedKeyPresent != null)
      return flattenedKeyPresent;

    return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
  }

  @Override
  public void takeValueForKeyPath(Object value, String keyPath) {
    NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
  }

  private static final String UNSUPPORTED = " is not a supported operation in com.webobjects.foundation.NSDictionary";

  @Override
  public void clear() {
    throw new UnsupportedOperationException("clear" + UNSUPPORTED);
  }

  @Override
  public NSDictionary<K, V> clone() {
    return this;
  }

  @Override
  public V remove(Object key) {
    throw new UnsupportedOperationException("remove" + UNSUPPORTED);
  }

  @Override
  public boolean containsKey(Object key) {
    return mapNoCopy().containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return mapNoCopy().containsValue(value);
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    if (_entrySetCache == null) {
      _entrySetCache = mapNoCopy().entrySet();
    }

    return _entrySetCache;
  }

  @Override
  public V get(Object key) {
    return objectForKey(key);
  }

  @Override
  public boolean isEmpty() {
    return mapNoCopy().isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return mapNoCopy().keySet();
  }

  @Override
  public int size() {
    return count();
  }

  @Override
  public Collection<V> values() {
    return mapNoCopy().values();
  }

}
