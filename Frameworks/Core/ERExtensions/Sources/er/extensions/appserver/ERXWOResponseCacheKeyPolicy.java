//
//  ERXWOResponseCachePolicy.java
//  ERExtensions
//
//  Created by Max Muller on Mon Dec 09 2002.
//
package er.extensions.appserver;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXMultiKey;

public class ERXWOResponseCacheKeyPolicy implements ERXWOResponseCache.Policy {

    protected static ERXWOResponseCacheKeyPolicy sharedInstance;

    public static ERXWOResponseCacheKeyPolicy sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new ERXWOResponseCacheKeyPolicy();
        }
        return sharedInstance;
    }

    public static class PolicyCacheEntry {

        public NSArray formKeys;
        public NSArray headerKeys;
        public NSArray cookieKeys;

        public int count;
        
        public PolicyCacheEntry(NSArray formKeys, NSArray headerKeys, NSArray cookieKeys) {
            this.formKeys = formKeys != null ? formKeys : NSArray.EmptyArray;
            this.headerKeys = headerKeys != null ? headerKeys : NSArray.EmptyArray;
            this.cookieKeys = cookieKeys != null ? cookieKeys : NSArray.EmptyArray;

            count = this.formKeys.count() + this.headerKeys.count() + this.cookieKeys.count();
        }
        
    }

    protected Map cacheKeyMap = new HashMap();
    
    public ERXWOResponseCacheKeyPolicy() {
    }
    
    public void createPolicyEntryForClass(Class actionClass,
                                          NSArray actionNames,
                                          NSArray formKeys,
                                          NSArray headerKeys,
                                          NSArray cookieKeys) {
        PolicyCacheEntry policyCacheEntry = new PolicyCacheEntry(formKeys, headerKeys, cookieKeys);
        if (actionNames.count() > 0) {
            for (Enumeration actionNameEnumerator = actionNames.objectEnumerator(); actionNameEnumerator.hasMoreElements();) {
                String actionName = (String)actionNameEnumerator.nextElement();
                cacheKeyMap.put(actionClass.getName() + "@" + actionName, policyCacheEntry);
            }
        } else {
            cacheKeyMap.put(actionClass.getName(), policyCacheEntry);
        }
    }

    public boolean actionNameIsCachableForClass(Class actionClass, String actionName) {
        return policyCacheEntryForClass(actionClass, actionName) != null;
    }

    public ERXMultiKey cacheKeyForRequest(Class actionClass, String actionName, WORequest request) {
        ERXMultiKey cacheKey = null;
        PolicyCacheEntry cacheEntry = policyCacheEntryForClass(actionClass, actionName);
        if (cacheEntry != null) {
            int count = 0;
            Object[] cache = new Object[cacheEntry.count + 2];
            cache[count++] = actionClass;
            cache[count++] = actionName;
            if (cacheEntry.formKeys.count() > 0) {
                for (Enumeration formKeyEnumerator = cacheEntry.formKeys.objectEnumerator();
                     formKeyEnumerator.hasMoreElements();) {
                    String formKey = (String)formKeyEnumerator.nextElement();
                    cache[count++] = request.formValueForKey(formKey);
                }
            }
            if (cacheEntry.headerKeys.count() > 0) {
                for (Enumeration headerKeyEnumerator = cacheEntry.headerKeys.objectEnumerator();
                     headerKeyEnumerator.hasMoreElements();) {
                    String headerKey = (String)headerKeyEnumerator.nextElement();
                    cache[count++] = request.headerForKey(headerKey);
                }                
            }
            if (cacheEntry.cookieKeys.count() > 0) {
                for (Enumeration cookieKeyEnumerator = cacheEntry.cookieKeys.objectEnumerator();
                     cookieKeyEnumerator.hasMoreElements();) {
                    String cookieKey = (String)cookieKeyEnumerator.nextElement();
                    cache[count++] = request.cookieValueForKey(cookieKey);
                }                
            }
            cacheKey = new ERXMultiKey(cache);
        }
        return cacheKey;
    }

    public PolicyCacheEntry policyCacheEntryForClass(Class actionClass, String actionName) {
        PolicyCacheEntry cacheEntry = (PolicyCacheEntry)cacheKeyMap.get(actionClass.getName() + "@" + actionName);
        return cacheEntry != null ? cacheEntry : (PolicyCacheEntry)cacheKeyMap.get(actionClass.getName());
    }
    
    /**
     * Can be overridden by subclasses to perform specific checks
     * to see if the cache should be reset.
     * @return if the cache should be reset.
     */
    public boolean shouldResetCache() {
        return false;
    }
}
