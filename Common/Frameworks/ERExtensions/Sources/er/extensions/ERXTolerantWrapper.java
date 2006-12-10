package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

/**
 * Wrapper for areas that might throw exceptions and catches them.
 * This is very useful when developing or other complex wraps of switch components, 
 * as most of the time, you are stuck with 
 * an exception and have no idea where it might come from.
 * @author ak
 *
 */
public class ERXTolerantWrapper extends WODynamicGroup {

	private static final Logger log = Logger.getLogger(ERXTolerantWrapper.class);

	public ERXTolerantWrapper(String arg0, NSDictionary arg1, WOElement arg2) {
		super(arg0, arg1, arg2);
	}

	private boolean isTolerant() {
		return !WOApplication.application().isCachingEnabled();
	}

	public void appendToResponse(WOResponse arg0, WOContext arg1) {
		if(isTolerant()) {
			try {
				super.appendToResponse(arg0, arg1);
			} catch (RuntimeException ex) {
				arg0.appendContentString(ex.toString());
				log.error(ex, ex);
			}
		} else {
			super.appendToResponse(arg0, arg1);
		}
	}

	public WOActionResults invokeAction(WORequest arg0, WOContext arg1) {
		if(isTolerant()) {
			try {
				return super.invokeAction(arg0, arg1);
			} catch (RuntimeException ex) {
				log.error(ex, ex);
			}
			return null;
		} else {
			return super.invokeAction(arg0, arg1);
		}
	}

	public void takeValuesFromRequest(WORequest arg0, WOContext arg1) {
		if(isTolerant()) {
			try {
				super.takeValuesFromRequest(arg0, arg1);
			} catch (RuntimeException ex) {
				log.error(ex, ex);
			}
		} else {
			super.takeValuesFromRequest(arg0, arg1);
		}
	}

}
