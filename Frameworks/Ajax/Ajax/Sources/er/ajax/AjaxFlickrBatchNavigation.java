package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.batching.ERXFlickrBatchNavigation;

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
 * @binding onClick an optional JavaScript String to bind to the previous, next, and select page AjaxUpdateLinks.
 * @binding updateContainerID (optional) the id of the container to refresh (defaults to the nearest parent)
 * @binding showPageRange if true, the page of items on the page is shown, for example "(1-7 of 200 items)" 
 * @binding showBatchSizes if <code>true</code>, a menu to change the items per page is shown "Show: (10) 20 (100) (All) items per page"
 * @binding batchSizes can be either a string or an NSArray of numbers that define the batch sizes to chose from. The number "0" provides an "All" items batch size. For example "10,20,30" or "10,50,100,0"
 * @binding small if true, a compressed page count style is used 
 * 
 * @binding parentActionName (if you don't provide a displayGroup) the action to be executed on the parent component to get the next batch of items.
 * @binding currentBatchIndex (if you don't provide a displayGroup) used to get and set on the parent component the selected page index
 * @binding maxNumberOfObjects (if you don't provide a displayGroup) used to get the total number of objects that are being paginated.
 * @binding numberOfObjectsPerBatch (if you don't provide a displayGroup) the number of objects per batch (page)
 */
public class AjaxFlickrBatchNavigation extends ERXFlickrBatchNavigation {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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