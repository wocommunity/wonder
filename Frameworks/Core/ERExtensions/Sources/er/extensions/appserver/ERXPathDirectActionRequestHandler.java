//
//  ERXPathDirectActionRequestHandler.java
//  ERExtensions
//
//  Created by Max Muller on Thu May 08 2003.
//
package er.extensions.appserver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WOURLEncoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * The path direct action request handler allows for storing information
 * in the request path. For instance you could have the request path:
 * /WebObjects/MyApp.woa/wpa/com.foo.SomeActionClass/bar/gee/wiz=neat/actionName
 * This action is treated just like:
 * /WebObjects/MyApp.woa/wpa/com.foo.SomeActionClass/actionName
 * 
 */
public class ERXPathDirectActionRequestHandler extends ERXDirectActionRequestHandler {
	private static final Logger log = LoggerFactory.getLogger(ERXDirectActionRequestHandler.class);

	/**
	 * Specifies if the request handler eats the action class from the URL
	 */
	private boolean useClassName = true;

	/**
	 * Specifies if the request handler eats the action name from the URL
	 */
	private boolean useActionName = true;


	/**
     * Public constructor, just calls super
     */
    public ERXPathDirectActionRequestHandler() {
        super();
    }


    /**
	 * Public constructor, just calls super
	 * 
	 * @param actionClassName
	 *            action class name
	 * @param defaultActionName
	 *            action name
	 * @param shouldAddToStatistics
	 *            boolean to add to stats
	 */
	public ERXPathDirectActionRequestHandler(String actionClassName, String defaultActionName, boolean shouldAddToStatistics) {
		super(actionClassName, defaultActionName, shouldAddToStatistics);
	}

	/**
	 * Creates the request handler and lets you specify if the URLs assume and action class in front and action name in back.
	 * This is useful when you have inner DA classes like SomeComponent$SomeAction and don't want this to appear in the URLs.
	 * @param actionClassName action class name
	 * @param defaultActionName default action name
	 * @param shouldAddToStatistics boolean if request should add to stats
	 * @param doUseClassName if false, do not assume action class name in URL
	 * @param doUseActionName if false, do not assume action name in URL
	 */
	public ERXPathDirectActionRequestHandler(String actionClassName, String defaultActionName, boolean shouldAddToStatistics, boolean doUseClassName, boolean doUseActionName) {
		this(actionClassName, defaultActionName, shouldAddToStatistics);
		useClassName = doUseClassName;
		useActionName = doUseActionName;
	}

    /**
	 * Modified version for getting the request handler path for a given
	 * request.
	 *
	 * @param aRequest
	 *			  a given request
	 * @return array of request handler paths for a given request, truncates to
	 *		   the first and last component if the number of components is
	 *		   greater than 2, and tries to divine the meaning of paths when there
	 *		   are only one or 2 path components.
	 */
	@Override
	public NSArray getRequestHandlerPathForRequest(WORequest aRequest) {
		NSArray paths = super.getRequestHandlerPathForRequest(aRequest);
		NSMutableArray temp = new NSMutableArray();
		if (paths != null) {
			int pathCount = paths.count();
			String firstPathComponent = pathCount > 0 ? (String)paths.objectAtIndex(0) : null;
			String lastPathComponent = (String)paths.lastObject();

			if (pathCount > 2) {
				if (useClassName && useActionName) {
					temp.addObject(firstPathComponent);
					temp.addObject(lastPathComponent);
				} else {
					if (!useClassName) {
						temp.addObject(lastPathComponent);
					} else {
						temp.addObject(firstPathComponent);
					}
				}
			} else if (pathCount == 2) {
				if (useClassName) {
					temp.addObject(firstPathComponent);
				}
				if (useActionName) {
					temp.addObject(lastPathComponent);
				}
			} else if (pathCount == 1) {
				// If there's just 1 path component it can either be an action name or the name of the action class.
				if ((useActionName && !lastPathComponent.equals(defaultActionName)) ||
					(useClassName && !lastPathComponent.equals(actionClassName))) {
					temp.addObject(lastPathComponent);
				}
			}
		}
		if (actionClassName != null && temp.count() == 0) {
			temp.addObject(actionClassName);
		}
		return temp.immutableClone();
	}
	
	/**
	 * Returns a dictionary similar to the normal request form value dict.
	 * Translates /cgi-bin/.../wpa/foo/bar=2/baz into {foo = foo; bar = 2;
	 * baz = baz}
	 * 
	 * @param request request to parse
	 * @param useActionClass true if first item should get ignored
	 * @param useActionName true if last item should get ignored
	 * @return the dictionary of form values
	 */
	public static NSDictionary<String, String> formValuesFromRequest(WORequest request, boolean useActionClass, boolean useActionName) {
		NSMutableDictionary<String, String> params = new NSMutableDictionary<>();
		boolean foundRequestHandler = false;
		for (Enumeration<String> e = NSArray.componentsSeparatedByString(request.uri(), "/").objectEnumerator(); e.hasMoreElements();) {
			String part = e.nextElement();
			try {
				part = URLDecoder.decode(part, WOURLEncoder.WO_URL_ENCODING);
			}
			catch (UnsupportedEncodingException ex) {
				log.error("Encoding not found: " + WOURLEncoder.WO_URL_ENCODING, ex);
			}
			if (foundRequestHandler && (!useActionName || (useActionName && e.hasMoreElements()))) {
				String[] pair = part.split("=", 2);
				String key = pair[0];
				String value = (pair.length == 2 ? pair[1] : pair[0]);
				if (pair.length == 2) {
					params.setObjectForKey(pair[1], pair[0]);
				}
				else {
					params.setObjectForKey(pair[0], pair[0]);
				}
			}
			if (part.equals(request.requestHandlerKey())) {
				foundRequestHandler = true;
				if (useActionClass && e.hasMoreElements()) {
					part = e.nextElement();
				}
			}
		}
		return params;
	}
}
