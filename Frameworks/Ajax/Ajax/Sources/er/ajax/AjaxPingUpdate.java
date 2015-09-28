package er.ajax;

import org.apache.commons.lang3.ObjectUtils;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * <p>
 * AjaxPingUpdate is primarily for use inside of an AjaxPing tag to support 
 * ping-updating multiple containers from a single AjaxPing request. Just 
 * like AjaxPing, you provide a cache key that upon changing, triggers the 
 * update of another target AjaxUpdateContainer.
 * </p>
 * 
 * @binding targetContainerID the ID of the update container to refresh when a change is detected
 * @binding cacheKey some hash value that represents the state of the target container
 * @binding onBeforeUpdate (optional) the javascript function to call before updating (should return true if the update
 *          should happen, false if not)
 *  
 * @author mschrag
 */
public class AjaxPingUpdate extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private Boolean _refreshTarget;
  	private static final Object NOT_INITIALIZED = new Object();  
	private Object _lastCacheKey = NOT_INITIALIZED;
	public AjaxPingUpdate(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/**
	 * Returns whether or not the target should be refreshed.
	 * 
	 * @return whether or not the target should be refreshed
	 */
	public boolean refreshTarget() {
    boolean refreshTarget = false;
	  if (_refreshTarget == null) {
  		Object cacheKey = valueForBinding("cacheKey");
  		if(_lastCacheKey == NOT_INITIALIZED) {
  			_lastCacheKey = cacheKey;
  		}
  		if (ObjectUtils.notEqual(_lastCacheKey, cacheKey)) {
  			refreshTarget = true;
  			_lastCacheKey = cacheKey;
  		}
  		_refreshTarget = Boolean.valueOf(refreshTarget);
	  }
		return _refreshTarget.booleanValue();
	}
	
	@Override
	public void sleep() {
		super.sleep();
		_refreshTarget = null;
	}

	/**
	 * Returns the target container ID.
	 * 
	 * @return the target container ID
	 */
	public String targetContainerID() {
		return (String) valueForBinding("targetContainerID");
	}
}
