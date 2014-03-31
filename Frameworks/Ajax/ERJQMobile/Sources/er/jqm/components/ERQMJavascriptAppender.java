package er.jqm.components;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXThreadStorage;

public class ERQMJavascriptAppender
{
	/**
	 * Adds a script tag with a correct resource URL on the current position of the response if it isn't already present in the response. This is needed because jQueryMobile reads
	 * and execute only stuff in <div data-role='...'>
	 * 
	 * @param response the response
	 * @param context the context
	 * @param framework the framework that contains the file
	 * @param fileName the name of the javascript file to add
	 */
	public static void addScriptResourceAtCurrentPosition(WOResponse response, WOContext context, String framework, String fileName)
	{
		if (!isResourceAddedInThread(framework, fileName))
		{
			boolean appendTypeAttribute = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXResponseRewriter.javascriptTypeAttribute", false);
			if (appendTypeAttribute)
			{
				response.appendContentString("<script type=\"text/javascript\" src=\"");
			}
			else
			{
				response.appendContentString("<script src=\"");
			}

			String url;
			if (fileName.indexOf("://") != -1 || fileName.startsWith("/"))
			{
				url = fileName;
			}
			else
			{
				WOResourceManager rm = WOApplication.application().resourceManager();
				NSArray<String> languages = null;
				if (context.hasSession())
				{
					languages = context.session().languages();
				}
				url = rm.urlForResourceNamed(fileName, framework, languages, context.request());
			}
			response.appendContentString(url);
			response.appendContentString("\"></script>");

			resourceAddedInThread(framework, fileName);
		}
	}

	public static boolean isResourceAddedInThread(String framework, String fileName)
	{
		String key = (framework != null) ? framework : "App";
		key += ":";
		key += fileName;
		return (ERXThreadStorage.valueForKey(key) != null);
	}

	public static void resourceAddedInThread(String framework, String fileName)
	{
		String key = (framework != null) ? framework : "App";
		key += ":";
		key += fileName;
		ERXThreadStorage.takeValueForKey(Boolean.TRUE, key);
	}
}
