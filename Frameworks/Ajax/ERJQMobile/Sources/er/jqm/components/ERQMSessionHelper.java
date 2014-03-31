package er.jqm.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ajax.ERXAjaxApplication;

/**
 * Helper class to send only ERQMAjaxUpdateContainer content to the client<br />
 * <br />
 * You need to add the following to your Session class
 * 
 * <pre>
 * &#064;Override
 * public WOActionResults invokeAction(WORequest request, WOContext context)
 * {
 * 	WOActionResults result = super.invokeAction(request, context);
 * 	result = ERQMSessionHelper.checkForUpdateContainer(result, request, context);
 * 	return result;
 * }
 * </pre>
 */
public class ERQMSessionHelper
{
	/**
	 * Helper class to send only ERQMAjaxUpdateContainer content to the client
	 * 
	 * @param result
	 * @param request
	 * @param context
	 * @return new response
	 */
	public static WOActionResults checkForUpdateContainer(WOActionResults result, WORequest request, WOContext context)
	{
		WOActionResults tmpResult = result;
		String updateContainerId = "_ju";
		if (context.request().formValueForKey(updateContainerId) != null && context.request().formValueForKey("_jqma") != null)
		{
			ERXAjaxApplication.enableShouldNotStorePage();
			String updateContainer = (String) context.request().formValueForKey(updateContainerId);
			if (result == null)
			{
				tmpResult = context.page();
			}
			// Schnipp Schnapp
			String delimiter = "<!-- " + updateContainerId + ":" + updateContainer + " -->";
			tmpResult.generateResponse();
			NSArray<String> tmp = NSArray.componentsSeparatedByString(context.response().contentString(), delimiter);
			if (tmp.count() == 3)
			{
				context.response().setContent(delimiter + tmp.get(1) + delimiter);
			}
			else
			{
				NSLog.err.appendln("ERQMSessionHelper -> can't find '" + delimiter + "'");
			}
		}
		return tmpResult;
	}

}
