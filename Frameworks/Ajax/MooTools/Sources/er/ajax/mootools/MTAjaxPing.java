package er.ajax.mootools;

import com.webobjects.appserver.WOContext;

import er.ajax.AjaxPing;

/**
 * <p>
 * AjaxPing provides support for refreshing a large content area based on a series of periodic refreshes of a very small
 * area. You provide a cache key that upon changing, triggers the update of another target AjaxUpdateContainer.
 * </p>
 * <p>
 * For instance, if you have a list of blog entries, you might refresh the blog entries container with a cache key
 * "blogEntries.count". When the count of the blog entries changes, the entire container will be refreshed.
 * </p>
 * <p>The MooTools version just replaces the AUC with an MTAUC and changes the javascript slightly in the MTAjaxPingUpdate</p>
 * 
 * @binding frequency the frequency of refresh (in millis), defaults to 3000
 * @binding targetContainerID the ID of the update container to refresh when a change is detected
 * @binding cacheKey some hash value that represents the state of the target container
 * @binding onBeforeUpdate (optional) the javascript function to call before updating (should return true if the update
 *          should happen, false if not)
 * @binding id (optional) the id of the ping update container (set this if you want to attach an AjaxBusyIndicator)
 * @binding stop (optional) if true, the ping will stop.  If false, the ping will run.  It's up to you to refresh the ping's container to get it running again after the binding changes from false to true.  
 * 
 * @author mschrag
 */

public class MTAjaxPing extends AjaxPing {

	private static final long serialVersionUID = 1L;

    public MTAjaxPing(WOContext context) {
        super(context);
    }
}