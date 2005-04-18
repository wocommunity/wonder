package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/**
 * Class for Wonder Component ERXCachingWrapper.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on 20.01.05
 * @project ERExtensions
 */

public class ERXCachingWrapper extends ERXNonSynchronizingComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERXCachingWrapper.class,"components");

    protected NSArray cachingKeys;
    protected String cacheEntryName;
    protected Long cacheDuration;
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERXCachingWrapper(WOContext context) {
        super(context);
    }
    
    public NSArray cachingKeys() {
        if(cachingKeys == null) {
            Object value = valueForBinding("cachingKeys");
            if(value instanceof NSArray) {
                cachingKeys = (NSArray)value;
            } else if(value instanceof String) {
                cachingKeys = (NSArray) NSPropertyListSerialization.propertyListFromString((String) value);
            } else {
                throw new IllegalArgumentException("cachingKeys must be a NSArray or a property list String");
            }
            if(cachingKeys == null) {
                cachingKeys = NSArray.EmptyArray;
            }
        }
        return cachingKeys;
    }
    
    public String cacheEntryName() {
        if(cacheEntryName == null) {
            cacheEntryName = (String)valueForBinding("cacheEntryName");
        }
        ERXAssert.PRE.notNull("cacheEntryName is required", cacheEntryName);
        return cacheEntryName;
    }
    
    public long cacheDuration() {
        if(cacheDuration == null) {
            Number value = (Number)valueForBinding("cacheDuration");
            if(value == null) {
                cacheDuration = new Long(60L*1000L);
            } else {
                cacheDuration = new Long(value.longValue());
            }
        }
        return cacheDuration.longValue();
    }
}
