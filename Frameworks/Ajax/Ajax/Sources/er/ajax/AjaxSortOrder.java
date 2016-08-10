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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public AjaxSortOrder(WOContext context) {
		super(context);
	}
	
	public String updateContainerID() {
		return stringValueForBinding("updateContainerID", AjaxUpdateContainer.currentUpdateContainerID());
	}
}