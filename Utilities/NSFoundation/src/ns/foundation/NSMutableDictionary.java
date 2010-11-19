package ns.foundation;


import java.util.HashMap;
import java.util.Map;

/**
 * Fully implemented except for NSKeyValueCodingAdditions
 */
public class NSMutableDictionary<K, V> extends NSDictionary<K, V> {

  private static final long serialVersionUID = 8222038593983170469L;

  public NSMutableDictionary() {
    super();
  }

  public NSMutableDictionary(int capacity) {
    super(capacity);
  }

  public NSMutableDictionary(V object, K key) {
    super(object, key);
  }

  public NSMutableDictionary(V[] objects, K[] keys) {
    super(objects, keys);
  }

  public NSMutableDictionary(Map<K, V> map) {
    super(map);
  }

  public NSMutableDictionary(NSArray<V> objects, NSArray<K> keys) {
    super(objects, keys);
  }

  public NSMutableDictionary(NSDictionary<K, V> otherDictionary) {
    super(otherDictionary);
  }

  @Override
  protected Map<K, V> _initializeWithCapacity(int capacity) {
    return _setMap(new HashMap<K, V>(capacity));
  }
  
  public void addEntriesFromDictionary(NSDictionary<? extends K, ? extends V> otherDictionary) {
    putAll(otherDictionary);
  }

  @Override
  public int _shallowHashCode() {
    return NSMutableDictionary.class.hashCode();
  }

  @Override
  public NSDictionary<K, V> immutableClone() {
    return new NSDictionary<K, V>(this);
  }

  public void removeAllObjects() {
    mapNoCopy().clear();
  }

  public V removeObjectForKey(Object key) {
    if (key == null)
      throw new IllegalArgumentException("Attempt to remove null key from an " + getClass().getName() + ".");

    return mapNoCopy().remove(key);
  }

  public void removeObjectsForKeys(NSArray<?> keys) {
    if (keys == null)
      return;
    for (int i = 0; i < keys.size(); i++) {
      removeObjectForKey(keys.get(i));
    }
  }

  public void setDictionary(NSDictionary<? extends K, ? extends V> otherDictionary) {
    if (otherDictionary != this) {
      clear();
      putAll(otherDictionary);
    }
  }

  public void setObjectForKey(V object, K key) {
    if (object == null)
      throw new IllegalArgumentException("Attempt to insert null object into an " + getClass().getName() + ".");
    if (key == null)
      throw new IllegalArgumentException("Attempt to insert null key into an " + getClass().getName() + ".");

    mapNoCopy().put(key, object);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void takeValueForKey(Object value, String key) {
    if (value != null)
      put((K)key, (V)value);
    else
      remove(key);
  }
  
  @Override
  public void clear() {
    removeAllObjects();
  }

  @Override
  public NSMutableDictionary<K, V> clone() {
    return mutableClone();
  }

  @Override
  public V put(K key, V value) {
    if (key == null)
      throw new IllegalArgumentException("Attempt to insert null key into an " + getClass().getName() + ".");
    V oldValue = objectForKey(key);
    setObjectForKey(value, key);
    return oldValue;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    if (m.containsKey(null) || m.containsValue(null))
      throw new IllegalArgumentException("Key or value may not be null");

    mapNoCopy().putAll(m);
  }
  
  @Override
  public V remove(Object key) {
    return removeObjectForKey(key);
  }
}
