package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Wrapper for areas that might throw exceptions and catches them. This is very useful when developing or other complex
 * wraps of switch components, as most of the time, you are stuck with an exception and have no idea where it might come
 * from.
 * 
 * @author ak
 */
public class ERXTolerantWrapper extends WODynamicGroup {
	private static final Logger log = Logger.getLogger(ERXTolerantWrapper.class);
	private WOAssociation _tolerant;

	public ERXTolerantWrapper(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_tolerant = (WOAssociation) ((NSMutableDictionary) associations).removeObjectForKey("tolerant");
	}

	private boolean isTolerant(WOComponent component) {
		boolean tolerant;
		if (_tolerant != null) {
			tolerant = _tolerant.booleanValueInComponent(component);
		}
		else {
			tolerant = !WOApplication.application().isCachingEnabled();
		}
		return tolerant;
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		if (isTolerant(context.component())) {
			try {
				super.appendToResponse(response, context);
			}
			catch (Throwable ex) {
				response.appendContentString(ex.toString());
				log.error(ex, ex);
			}
		}
		else {
			super.appendToResponse(response, context);
		}
	}

	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if (isTolerant(context.component())) {
			try {
				return super.invokeAction(request, context);
			}
			catch (Throwable ex) {
				log.error(ex, ex);
			}
			return null;
		}
		return super.invokeAction(request, context);
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (isTolerant(context.component())) {
			try {
				super.takeValuesFromRequest(request, context);
			}
			catch (Throwable ex) {
				log.error(ex, ex);
			}
		}
		else {
			super.takeValuesFromRequest(request, context);
		}
	}

}
