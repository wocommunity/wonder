//
//  ERXWOResponseCache.java
//  ERExtensions
//
//  Created by Max Muller on Thu Dec 05 2002.
//
package er.extensions;

import java.util.*;

import com.webobjects.appserver.*;

/**
 * The response cache is a way to cache WOResponse
 * objects for a given set of cache keys.
 */
public class ERXWOResponseCache {

    /**
     * Holds a reference to the shared instance
     */
    protected static ERXWOResponseCache sharedInstance;

    /**
     * Gets the shared instance
     * @return the shared instance
     */ 
    public static ERXWOResponseCache sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new ERXWOResponseCache();
        }
        return sharedInstance;
    }

    public static interface Policy {
        public boolean actionNameIsCachableForClass(Class actionClass, String actionName);
        public ERXMultiKey cacheKeyForRequest(Class actionClass, String actionName, WORequest request);
        public boolean shouldResetCache();
    }

    public static interface Cacheable {
    }
    
    protected Map cache;

    protected Policy policy;

    protected Boolean isEnabled;
    
    public ERXWOResponseCache() {
        super();
        if (WOApplication.application().isConcurrentRequestHandlingEnabled()) {
            cache = Collections.synchronizedMap(new HashMap());
        } else {
            cache = new HashMap();
        }
    }

    public boolean isEnabled() {
        if (isEnabled == null) {
            isEnabled = ERXProperties.booleanForKey("er.extensions.ERXWOResponseCache.Enabled") ? Boolean.TRUE : Boolean.FALSE;
        }
        return isEnabled.booleanValue();
    }
    
    public Policy policy() {
        return this.policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public boolean hasPolicy() {
        return policy != null;
    }
    
    public boolean actionNameIsCachableForClass(Class actionClass, String actionName) {
        return hasPolicy() ? policy().actionNameIsCachableForClass(actionClass, actionName) : false;
    }

    public void flushCache() {
        cache.clear();
    }
    
    public WOResponse cachedResponseForRequest(Class actionClass, String actionName, WORequest request) {
        if (policy().shouldResetCache()) {
            flushCache();
        }
        ERXMultiKey cacheKey = policy().cacheKeyForRequest(actionClass, actionName, request);
        return cacheKey != null ? (WOResponse)cache.get(cacheKey) : null;
    }

    public void cacheResponseForRequest(Class actionClass, String actionName, WORequest request, WOResponse response) {
        ERXMultiKey cacheKey = policy().cacheKeyForRequest(actionClass, actionName, request);
        if (cacheKey != null) {
            cache.put(cacheKey, response);
        }
    }
}
