//
//  ERXWOResponseCache.java
//  ERExtensions
//
//  Created by Max Muller on Thu Dec 05 2002.
//
package er.extensions.appserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.foundation.ERXMultiKey;
import er.extensions.foundation.ERXProperties;

/**
 * The response cache is a way to cache WOResponse output from a DirectAction
 * for a given set of cache keys. You can specify the headers, formValues and cookies
 * to take into account. Your DirectAction class must implement the {@link Cacheable} interface and
 * should look like this:<pre><code>

 public class DirectAction extends WODirectAction implements ERXWOResponseCache.Cacheable {
    static {
        ERXWOResponseCacheKeyPolicy.sharedInstance().createPolicyEntryForClass(DirectAction.class, 
            new NSArray(new Object[] {"default", "cached"}), 
            NSArray.EmptyArray, NSArray.EmptyArray, NSArray.EmptyArray);
    }
    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults notCachedAction() {
        return pageWithName("NotCached");
    }
    
    public WOActionResults cachedAction() {
        return pageWithName("Cached");
    }
    
    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }
}
</code></pre>
You must also set the default <code>er.extensions.ERXWOResponseCache.Enabled=true</code> for the cache to get used.
 * 
 * @property er.extensions.ERXWOResponseCache.Enabled
 */
public class ERXWOResponseCache {

    /**
     * Holds a reference to the shared instance
     */
    protected static ERXWOResponseCache sharedInstance;

    /**
     * Header key you can set in the response when creating an error page you don't want to get cached.
     */
    public static String NO_CACHE_KEY = "ERXDirectActionRequestHandler.DontCache";

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
    public void setIsEnabled(boolean enabled) {
        isEnabled = (enabled ? Boolean.TRUE : Boolean.FALSE);
    }
    
    
    public Policy policy() {
        return policy;
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
        if(response.headerForKey(NO_CACHE_KEY) == null) {
            ERXMultiKey cacheKey = policy().cacheKeyForRequest(actionClass, actionName, request);
            if (cacheKey != null) {
                cache.put(cacheKey, response);
            }
        } else {
            response.removeHeadersForKey(NO_CACHE_KEY);
        }
    }
}
