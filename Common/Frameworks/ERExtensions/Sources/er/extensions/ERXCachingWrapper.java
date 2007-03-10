package er.extensions;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Wrapper that caches its content based on a set of bindings. Use this component to wrap
 * parts of your HTML whose generation is costly. <br />
 * Valid keys would be for example:
 * <ul>
 *   <li><code>parent.isEnabled</code>, where isEnabled would be some method on the parent.
 *   <li><code>session.user.name</code>
 *   <li><code>headers.hostName</code>
 *   <li><code>formValues.oid</code>
 *   <li><code>session.localizer.language</code>
 * </ul>
 * Basically, you would put there any key whose value would cause the content to change.
 * Session IDs are replaced automatically. Don't use this wrapper if the content contains
 * component actions. Drop only stateless components in this wrapper.
 *
 * @binding keys the keys to use for caching 
 * @binding duration the duration the entry stays in the cache
 * @binding entryName the name to cache on
 *
 * @created ak on 20.01.05
 * @project ERExtensions
 */

//ENHANCEME cache should get reaped every so often and remove stale entries. 

public class ERXCachingWrapper extends ERXStatelessComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXCachingWrapper.class);

    /** The cached entries */
    protected static Map cache = Collections.synchronizedMap(new HashMap() {
    	public Object get(Object key) {
    		Entry result = (Entry) super.get(key);
    		if(result != null) {
    			if(!result.isActive()) {
    				remove(key);
    				result = null;
    			}
    		}
    		return result;
    	}
    });
    
    /** Simply cache entry class. It caches a string for a duration and can replace the session ID on retrieval. */
    protected class Entry {
    	private long insertTime;
    	private long duration;
    	private String content;
    	private String sessionID;
    	
    	public Entry(String aContent, long aDuration, String aSessionID) {
    		this.insertTime = System.currentTimeMillis();
    		this.content = aContent;
    		this.duration = aDuration;
    		this.sessionID = aSessionID;
    	}

		public boolean isActive() {
			return System.currentTimeMillis() - (this.insertTime + this.duration) < 0;
		}
		
		public String content(WOContext arg1) {
			if(this.sessionID != null) {
				return content.replaceAll(this.sessionID, arg1.session().sessionID());
			}
			return this.content;
		}
    }

    protected NSArray keys;
    protected String entryName;
    protected Long cacheDuration;
    protected Entry entry;
    protected NSDictionary values;
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERXCachingWrapper(WOContext context) {
        super(context);
    }
    
    public void awake() {
    	super.awake();
    	keys = null;
    	entryName = null;
    	cacheDuration = null;
    	values = null;
     	entry = (Entry) cache.get(values());
    }
    
    protected NSArray keys() {
    	if(keys == null) {
    		Object value = valueForBinding("keys");
    		if(value instanceof NSArray) {
    			keys = (NSArray)value;
    		} else if(value instanceof String) {
    			keys = (NSArray) NSPropertyListSerialization.propertyListFromString((String) value);
    		} else {
    			throw new IllegalArgumentException("keys must be a NSArray or a property list String");
    		}
    		if(keys == null) {
    			keys = NSArray.EmptyArray;
    		}
    	}
    	return keys;
    }
    
    /**
     * Returns the request headers as a KVC object.
     */
    // ENHANCEME use symbolic names like "remoteHost" to broker between all those different adaptor keys
    public NSKeyValueCoding headers() {
    	return new NSKeyValueCoding() {
            public Object valueForKey(String s) {
            	return context().request().headerForKey(s);
            }

            public void takeValueForKey(Object obj, String s) {
            }
    		
    	};
    }
    
    /**
     * Returns the form values as a KVC object.
     */
    public NSKeyValueCoding formValues() {
    	return new NSKeyValueCoding() {
            public Object valueForKey(String s) {
            	return context().request().formValueForKey(s);
            }

            public void takeValueForKey(Object obj, String s) {
            }
    		
    	};
    }
    
    protected String entryName() {
    	if(entryName == null) {
    		entryName = (String)valueForBinding("entryName");
        }
        ERXAssert.PRE.notNull("cacheEntryName is required", entryName);
        return entryName;
    }
    
    protected long cacheDuration() {
        if(cacheDuration == null) {
            Number value = (Number)valueForBinding("duration");
            if(value == null) {
                cacheDuration = new Long(60L*1000L);
            } else {
                cacheDuration = new Long(value.longValue());
            }
        }
        return cacheDuration.longValue();
    }
    
    protected NSDictionary values() {
    	if(values == null) {
    		NSMutableDictionary result = new NSMutableDictionary();
    		for(Enumeration e = keys().objectEnumerator(); e.hasMoreElements();) {
    			String keyPath = (String)e.nextElement();
    			Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(this, keyPath);
    			if(value != null) {
    				result.setObjectForKey(value, keyPath);
    			}
    		}
    		result.setObjectForKey(entryName(), "ERXCachingWrapper.entryName");
    		values = result.immutableClone();
    	}
    	return values;
    }
    
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if(entry == null) {
			super.takeValuesFromRequest(request, context);
		}
	}
	
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if(entry == null) {
			return super.invokeAction(request, context);
		}
		return context().page();
	}
	
	public void appendToResponse(WOResponse response, WOContext context) {
		if(entry == null) {
			WOResponse newResponse = application().createResponseInContext(context);
			super.appendToResponse(newResponse, context);
			String content = newResponse.contentString();
			entry = new Entry(content, cacheDuration(), (context.hasSession() ? context.session().sessionID() : null));
			cache.put(values(), entry);
		}
		String content = entry.content(context);
		response.appendContentString(content);
	}
}
