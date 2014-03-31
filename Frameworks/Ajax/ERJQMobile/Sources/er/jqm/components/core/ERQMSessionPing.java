package er.jqm.components.core;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.appserver.ERXResponse;
import er.jqm.components.ERQMComponentBase;

/**
 * Simple component to ping the session in the background. It can do two things. The first is to execute JavaScript if the session is no longer valid. The default action is to
 * close the window that the ping came from. The second thing it can do is to keep the session alive. This can be useful if you want the session to not time out on particular
 * pages. It should be used with caution as it can prevent scheduled restarts if the user leaves the browser window open.
 * 
 * @binding frequency the period between pings of the application (optional, default 60 seconds)
 * @binding keepSessionAlive true if session should be checked out to reset timeout when the application is pinged (optional, default false)
 * @binding onFailure function to execute if the session has expired or other HTTP error code returned from ping (optional, default "function(response) { window.close();}")
 * 
 */
public class ERQMSessionPing extends ERQMComponentBase
{
	public ERQMSessionPing(WOContext context)
	{
		super(context);
	}

	public String url()
	{
		if (booleanValueForBinding("keepSessionAlive", false))
		{
			return context().directActionURLForActionNamed("ERQMSessionPing$Action/pingSessionAndKeepAlive", null, context().secureMode(), false);
		}
		return context().directActionURLForActionNamed("ERQMSessionPing$Action/pingSession", null, context().secureMode(), false);
	}

	public String frequency()
	{
		long f = intValueForBinding("frequency", 60);
		f = f * 1000;
		return Long.valueOf(f).toString();
	}

	public String onFailure()
	{
		return stringValueForBinding("onFailure", "window.close();");
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context)
	{
		super.appendToResponse(response, context);

		StringBuilder b = new StringBuilder();
		b.append("<script>");
		b.append("function jqmPingSesseion() {");
		b.append(" var url = '" + url() + "' + '&t=' +new Date().getTime();");
		b.append(" $.get( url )");
		b.append(".fail(function() { " + onFailure() + " })");
		b.append(".always(function() { setTimeout(jqmPingSesseion, " + frequency() + " ); });}\n");
		b.append("$(document).ready(function() { setTimeout(jqmPingSesseion, " + frequency() + " );});");
		b.append("</script>");

		response.appendContentString(b.toString());
	}

	/**
	 * Internal WODirectAction subclass to handle the request from AjaxSessionPing.
	 */
	public static class Action extends WODirectAction
	{

		public Action(WORequest request)
		{
			super(request);
		}

		/**
		 * If there is a session, returns a response with a success (200) code. If there is not a session, returns a response with not found (404) code so that the
		 * ActivePeriodicalUpdater can call the onFailure call back.
		 * 
		 * @return bare HTTP response with status set
		 */
		public WOActionResults pingSessionAction()
		{
			ERXResponse response = new ERXResponse();
			if (existingSession() != null)
			{
				session();
			}
			else
			{
				response.setStatus(ERXHttpStatusCodes.NOT_FOUND);
			}
			return response;
		}

		/**
		 * Same as pingSessionAction, but also checks out session to keep it alive.
		 * 
		 * @see #pingSessionAction
		 * @return bare HTTP response with status set
		 */
		public WOActionResults pingSessionAndKeepAliveAction()
		{
			if (existingSession() != null)
			{
				session();
			}
			return pingSessionAction();
		}

	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		// Nothing to do

	}
}