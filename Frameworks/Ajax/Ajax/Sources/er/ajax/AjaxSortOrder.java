package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXSortOrder;

/**
 * An Ajax version of ERXSortOrder.
 * 
 * @binding updateContainerID the container to refresh after sorting 
 * @binding d2wContext
 * @binding displayGroup
 * @binding displayKey
 * @binding key
 * @binding unsortedImageSrc
 * @binding sortedAscendingImageSrc
 * @binding sortedDescendingImageSrc
 * @binding unsortedImageName
 * @binding sortedAscendingImageName
 * @binding sortedDescendingImageName
 * @binding imageFramework
 * @author mschrag
 */
public class AjaxSortOrder extends ERXSortOrder {
	
	public AjaxSortOrder(WOContext context) {
		super(context);
	}
	
	public String updateContainerID() {
		return stringValueForBinding("updateContainerID", AjaxUpdateContainer.currentUpdateContainerID());
	}
}