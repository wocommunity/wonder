package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXFlickrBatchNavigation;

/**
 * <p>AjaxFlickrBatchNavigation is a batch navigation component that provides pagination that behaves like the paginator on
 * Flickr.com, and is just like ERXFlickrBatchNavigation except that the links are AjaxUpdateLinks.</p>
 * 
 * <p>Can also be used for pagination on the parent component, where the objects being paginated may be POJOs in an array, 
 * or where paging all the objects in the allObjects array is not feasible due to memory requirements.</p>
 * 
 * @author mschrag
 * @author rob, cug (non displayGroup batching)
 * 
 * @binding displayGroup the display group to paginate
 * @binding displayName the name of the items that are being display ("photo", "bug", etc)
 * @binding updateContainerID (optional) the id of the container to refresh (defaults to the nearest parent)
 * @binding showPageRange if true, the page of items on the page is shown, for example "(1-7 of 200 items)" 
 * @binding small if true, a compressed page count style is used 
 * 
 * @binding parentActionName (if you don't provide a displayGroup) the action to be executed on the parent component to get the next batch of items.
 * @binding currentBatchIndex (if you don't provide a displayGroup) used to get and set on the parent component the selected page index
 * @binding maxNumberOfObjects (if you don't provide a displayGroup) used to get the total number of objects that are being paginated.
 * @binding numberOfObjectsPerBatch (if you don't provide a displayGroup) the number of objects per batch (page)
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