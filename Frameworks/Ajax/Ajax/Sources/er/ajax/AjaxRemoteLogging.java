package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.appserver.ERXResponse;

/**
 * Allows you to log <code>window.console</code> JS messages from the browser to
 * a logger on the server. Pretty helpful when trying to debug JS problems that
 * do not occur on your machine. As you will to a round trip to the server on
 * each message, it's pretty costly and should be used with care. 
 * 
 * @author ak
 * 
 * @binding logger the log4j logger to append to (default: "AjaxRemoteLogging")
 * @binding level the log4j logging level to use (default: "info")
 * @binding throttle the number of milliseconds to collect statements before actually sending (default: 100)
 * @binding filter a javascript function that returns true on a single argument
 *          msg when the logging should go to the server
 * 
 */
public class AjaxRemoteLogging extends AjaxDynamicElement {

	private WOAssociation _logger;
	private WOAssociation _filter;
	private WOAssociation _level;
	private WOAssociation _throttle;

	public AjaxRemoteLogging(String arg0, NSDictionary arg1, WOElement arg2) {
		super(arg0, arg1, arg2);
		_filter = (WOAssociation) arg1.objectForKey("filter");
		_logger = (WOAssociation) arg1.objectForKey("logger");
		_level = (WOAssociation) arg1.objectForKey("level");
		_throttle = (WOAssociation) arg1.objectForKey("throttle");
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		String level = "info";
		String logger = getClass().getSimpleName();
		String filter = null;
		Object throttle = "100";

		if (_filter != null) {
			filter = (String) _filter.valueInComponent(context.component());
		}
		if (_logger != null) {
			logger = (String) _logger.valueInComponent(context.component());
		}
		if (_level != null) {
			level = (String) _level.valueInComponent(context.component());
		}
		if (_throttle != null) {
			throttle =  _throttle.valueInComponent(context.component());
		}
		String url = context.directActionURLForActionNamed(Log.class.getName(), null);
		StringBuilder buf = new StringBuilder();
		buf.append("<script type='text/javascript'>\n");
		buf.append("WonderRemoteLogging.install({url: '").append(url);
		buf.append("', level: '").append(level);
		buf.append("', logger: '").append(logger);
		buf.append("', throttle: ").append(throttle);
		buf.append(" , filter: ").append(filter);
		buf.append("});\n");
		buf.append("</script>");
		response.appendContentString(buf.toString());
	}

	public static class Log extends ERXDirectAction {

		public Log(WORequest r) {
			super(r);
		}

		@Override
		public WOActionResults performActionNamed(String logger) {
			String level = context().request().stringFormValueForKey("l");
			String msg = context().request().stringFormValueForKey("m");
			if (logger == null) {
				logger = AjaxRemoteLogging.class.getSimpleName();
			}
			// trigger session loading if present
			WOSession existing = existingSession();
			Logger log = Logger.getLogger(logger);
			if ("fatal".equalsIgnoreCase(level)) {
				log.fatal(msg);
			}
			else if ("error".equalsIgnoreCase(level)) {
				log.error(msg);
			}
			else if ("warn".equalsIgnoreCase(level)) {
				log.warn(msg);
			}
			else if ("info".equalsIgnoreCase(level)) {
				log.info(msg);
			}
			else if ("debug".equalsIgnoreCase(level)) {
				log.debug(msg);
			}
			return new ERXResponse();
		}
	}

	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		addScriptResourceInHead(context, response, "prototype.js");
		addScriptResourceInHead(context, response, "wonder.js");
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}