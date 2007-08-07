package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.ERXFlickrBatchNavigation;

/**
 * AjaxFlickrBatchNavigation is a batch navigation component that provides pagination that behaves like the paginator on
 * Flickr.com, and is just like ERXFlickrBatchNavigation except that the links are AjaxUpdateLinks.
 * 
 * @author mschrag
 * @binding displayGroup the display group to paginate
 * @binding displayName the name of the items that are being display ("photo", "bug", etc)
 * @binding updateContainerID (optional) the id of the container to refresh (defaults to the nearest parent)
 */
public class AjaxFlickrBatchNavigation extends ERXFlickrBatchNavigation {
	public AjaxFlickrBatchNavigation(WOContext context) {
		super(context);
	}

	public String updateContainerID() {
		String updateContainerID = (String) valueForBinding("updateContainerID");
		if (updateContainerID == null) {
			updateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
		}
		return updateContainerID;
	}
}