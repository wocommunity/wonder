package er.ajax.mootools;

import com.webobjects.appserver.WOContext;

import er.ajax.AjaxPingUpdate;

/**
 * <p>
 * AjaxPingUpdate is primarily for use inside of an AjaxPing tag to support 
 * ping-updating multiple containers from a single AjaxPing request. Just 
 * like AjaxPing, you provide a cache key that upon changing, triggers the 
 * update of another target AjaxUpdateContainer.
 * </p>
 * <p>The MooTools version just changes the JavaScript to MTAUC.update from AUC.update</p>

 * @binding targetContainerID the ID of the update container to refresh when a change is detected
 * @binding cacheKey some hash value that represents the state of the target container
 * @binding onBeforeUpdate (optional) the javascript function to call before updating (should return true if the update
 *          should happen, false if not)
 *  
 * @author mschrag
 */

public class MTAjaxPingUpdate extends AjaxPingUpdate {

	private static final long serialVersionUID = 1L;

    public MTAjaxPingUpdate(WOContext context) {
        super(context);
    }
}