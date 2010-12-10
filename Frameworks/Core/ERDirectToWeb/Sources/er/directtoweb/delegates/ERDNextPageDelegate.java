package er.directtoweb.delegates;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * The regular NextPageDelegate interface from
 * d2w has hard coded the return type of WOComponent.
 * Sometimes you need to return a WOResponse instead
 * of a component. This interface solves this problem.
 */
// ENHANCEME: Might want this interface to extend NextPageDelegate, so that casting wise things would be fine.
public interface ERDNextPageDelegate {
    public WOActionResults erNextPage(WOComponent sender);
}

abstract class ERDDictNextPageDelegate implements NextPageDelegate {
    private NSMutableDictionary _data = new NSMutableDictionary();
    public void takeValueForKey(Object value, Object key) { _data.setObjectForKey(value, key); }
    public Object valueForKey(Object key) { return _data.objectForKey(key); }
}