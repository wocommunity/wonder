package er.caching;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.danga.memcached.MemCachedClient;

@SuppressWarnings("hiding")
public class ERCachingMap<String, V extends Object> implements Map<String, V> {
    
    private MemCachedClient _client;

    private synchronized MemCachedClient client() {
        if (_client == null) {
            _client = new MemCachedClient();
            _client.setCompressEnable(false);
            _client.setCompressThreshold(0);
        }
        return _client;
    }

    public boolean containsKey(Object key) {
        return client().keyExists(key.toString());
    }

    public V get(Object key) {
        return (V) client().get(key.toString());
    }

    public boolean isEmpty() {
        return false;
    }

    public V put(String key, V value) {
        return put(key, value, 0L);
    }

    public V put(String key, V value, long timeout) {
        V old = null; //get(arg0);
        client().set(key.toString(), value, timeout);
        return old;
    }

    public V remove(Object key) {
        V old = get(key);
        client().delete(key.toString());
        return old;
    }

    public void putAll(Map<? extends String, ? extends V> arg0) {
        for (Iterator<? extends String> iterator = arg0.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            put(key, arg0.get(key));
        }
    }

    public boolean containsValue(Object arg0) {
        throw new UnsupportedOperationException("Not supported in memcached");
    }

    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not supported in memcached");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported in memcached");
    }

    public Set<Entry<String, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported in memcached");
    }

    public int size() {
        throw new UnsupportedOperationException("Not supported in memcached");
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException("Not supported in memcached");
    }
}