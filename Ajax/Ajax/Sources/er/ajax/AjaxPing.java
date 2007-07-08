package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * <p>
 * AjaxPing provides support for refreshing a large content area based on a series
 * of periodic refreshes of a very small area.  You provide a cache key that upon
 * changing, triggers the update of another target AjaxUpdateContainer.
 * </p>
 * <p>
 * For instance, if you have a list of blog entries, you might refresh the blog
 * entries container with a cache key "blogEntries.count".  When the count of the
 * blog entries changes, the entire container will be refreshed.
 * </p>
 * 
 * @binding frequency the frequency of refresh (in millis), defaults to 3000
 * @binding targetContainerID the ID of the update container to refresh when a change is detected
 * @binding cacheKey some hash value that represents the state of the target container
 *  
 * @author mschrag
 */
public class AjaxPing extends WOComponent {
	private Object _lastCacheKey;

	public AjaxPing(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/**
	 * Returns the frequency of refresh in millis.
	 * 
	 * @return the frequency of refresh in millis
	 */
	public Object frequency() {
		Object frequency = valueForBinding("frequency");
		if (frequency == null) {
			frequency = "3000";
		}
		return frequency;
	}
	
	/**
	 * Returns whether or not the target should be refreshed.
	 * 
	 * @return whether or not the target should be refreshed
	 */
	public boolean refreshTarget() {
		boolean refreshTarget = false;
		Object cacheKey = valueForBinding("cacheKey");
		if (_lastCacheKey == null) {
			_lastCacheKey = cacheKey;
		}
		else if (!cacheKey.equals(_lastCacheKey)) {
			refreshTarget = true;
			_lastCacheKey = cacheKey;
		}
		return refreshTarget;
	}

	/**
	 * Returns the target container ID.
	 * @return the target container ID
	 */
	public String targetContainerID() {
		return (String) valueForBinding("targetContainerID");
	}
	
	/**
	 * Returns the ID of the ping container.
	 * @return the ID of the ping container
	 */
	public String id() {
		return targetContainerID() + "Ping";
	}
}