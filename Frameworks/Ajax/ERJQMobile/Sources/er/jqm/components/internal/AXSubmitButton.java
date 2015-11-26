package er.jqm.components.internal;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WOSubmitButton;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ajax.ERXAjaxApplication;

public class AXSubmitButton extends WOSubmitButton
{

	public AXSubmitButton(String aName, NSDictionary<String, WOAssociation> someAssociations, WOElement template)
	{
		super(aName, someAssociations, template);
	}

	public static boolean shouldHandleRequest(WORequest request, WOContext context, String containerID)
	{
		String elementID = context.elementID();
		String senderID = context.senderID();
		String updateContainerID = null;

		boolean shouldHandleRequest = elementID != null
				&& (elementID.equals(senderID) || (containerID != null && containerID.equals(updateContainerID)) || elementID.equals(ERXAjaxApplication
						.ajaxSubmitButtonName(request)));
		return shouldHandleRequest;
	}

	/**
	 * Checks if the current request should be handled by this element.
	 * 
	 * @param request the current request
	 * @param context context of the transaction
	 * @return <code>true</code> if we should handle the request
	 */
	protected boolean shouldHandleRequest(WORequest request, WOContext context)
	{
		String elementID = context.elementID();
		String senderID = context.senderID();

		boolean shouldHandleRequest = elementID != null && (elementID.equals(senderID) || elementID.equals(ERXAjaxApplication.ajaxSubmitButtonName(request)));
		return shouldHandleRequest;
	}

	public WOActionResults handleRequest(WOContext context)
	{
		WOActionResults anActionResult = null;
		WOComponent aComponent = context.component();

		context.setActionInvoked(true);
		if (_action != null)
		{
			anActionResult = (WOActionResults) _action.valueInComponent(aComponent);
		}
		if (anActionResult == null)
		{
			anActionResult = context.page();
		}
		return anActionResult;
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context)
	{
		WOActionResults result = null;
		if (shouldHandleRequest(request, context))
		{
			result = handleRequest(context);
			ERXAjaxApplication.enableShouldNotStorePage();
			if (ERXAjaxApplication.isAjaxUpdate(request))
			{
				result = context.page();
			}
			else if (ERXAjaxApplication.shouldIgnoreResults(request, context, result) && !ERXAjaxApplication.isAjaxUpdate(request))
			{
				// log.warn("An Ajax request attempted to return the page, which is almost certainly an error.");
				result = null;
			}
			else if (result == null && !ERXAjaxApplication.isAjaxReplacement(request))
			{
				result = AjaxUtils.createResponse(request, context);
			}
		}
		else if (hasChildrenElements())
		{
			result = super.invokeAction(request, context);
		}
		return result;
	}

}
