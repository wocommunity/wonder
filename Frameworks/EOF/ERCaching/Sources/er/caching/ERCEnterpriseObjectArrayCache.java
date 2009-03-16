package er.caching;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEnterpriseObjectArrayCache;

/**
 * Usage example:
 * 
 * ERCEnterpriseObjectArrayCache&lt;Bug&gt; cache = new
 * ERCEnterpriseObjectArrayCache&lt;Bug&gt;("Bug"); cache.setObjectsForKey(bugs,
 * "all"); NSArray&lt;Bug&gt; objects = cache.objectsForKey("all");
 * 
 * @author ak
 * 
 * @param <T>
 */
public class ERCEnterpriseObjectArrayCache<T extends EOEnterpriseObject> extends ERXEnterpriseObjectArrayCache<EOEnterpriseObject> {

    protected ERCachingMap<String, NSArray<EOGlobalID>> _cache;

    public ERCEnterpriseObjectArrayCache(String entityName) {
        this(entityName, 0L);
    }

    public ERCEnterpriseObjectArrayCache(Class<? extends EOEnterpriseObject> c) {
        this(entityNameForClass(c));
    }

    public ERCEnterpriseObjectArrayCache(String entityName, long timeout) {
        super(entityName, timeout);
    }

    private synchronized ERCachingMap<String, NSArray<EOGlobalID>> cache() {
        if (_cache == null) {
            _cache = new ERCachingMap<String, NSArray<EOGlobalID>>();
        }
        return _cache;
    }

    protected void setCachedArrayForKey(NSArray<EOGlobalID> gids, Object key) {
        cache().put(key.toString(), gids, timeout());
    }

    protected NSArray<EOGlobalID> cachedArrayForKey(Object key) {
        return (NSArray<EOGlobalID>) cache().get(key);
    }

    public synchronized void reset() {
        // can't do any thing here...
    }
}
