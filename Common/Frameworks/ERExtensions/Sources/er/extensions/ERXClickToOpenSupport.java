package er.extensions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

/**
 * ERXClickToOpenSupport provides the component-side implementation of
 * WOLips.framework's click-to-open support.
 * 
 * @author mschrag
 * @property er.component.clickToOpen boolean that determines if click-to-open
 *           is enabled (only enables in development mode)
 */
public class ERXClickToOpenSupport {
	/**
	 * Boolean that controls whether or not click-to-open support is enabled.
	 */
	private static Boolean _enabled;

	/**
	 * Shared pattern for the click-to-open parser.
	 */
	private static Pattern _tagPattern = Pattern.compile("<[a-zA-Z]+\\s*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/**
	 * Returns whether or not click-to-open support is enabled.
	 * 
	 * @return whether or not click-to-open support is enabled
	 */
	public static boolean isEnabled() {
		// Just load click-to-open support one time ...
		if (_enabled == null) {
			if (!ERXApplication.isDevelopmentModeSafe()) {
				_enabled = Boolean.FALSE;
			}
			else {
				_enabled = Boolean.valueOf(ERXProperties.booleanForKeyWithDefault("er.component.clickToOpen", false));
			}
		}
		return ERXClickToOpenSupport._enabled.booleanValue();
	}

	/**
	 * Called before super.appendToResponse for click-to-open support.
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @return the "previousContentLength" (to pass to postProcessResponse)
	 * @param clickToOpenEnabled
	 *            if false, this method is basically a no-op; if true, it
	 *            processes the response
	 */
	public static int preProcessResponse(WOResponse response, WOContext context, boolean clickToOpenEnabled) {
		int previousContentLength;
		if (!clickToOpenEnabled) {
			previousContentLength = 0;
		}
		else {
			String contentStr = response.contentString();
			previousContentLength = contentStr == null ? 0 : contentStr.length();
		}
		return previousContentLength;
	}

	/**
	 * Called after super.appendToResponse for click-to-open support.
	 * 
	 * @param previousContentLength
	 *            the previousContentLength from preProcessResponse
	 * @param componentName
	 *            the name of the component being processed
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @param clickToOpenEnabled
	 *            if false, this method is basically a no-op; if true, it
	 *            processes the response
	 */
	public static void postProcessResponse(int previousContentLength, Class component, WOResponse response, WOContext context, boolean clickToOpenEnabled) {
		if (clickToOpenEnabled) {
			String contentStr = response.contentString();
			if (contentStr != null) {
				Matcher tagMatcher = ERXClickToOpenSupport._tagPattern.matcher(contentStr.substring(previousContentLength));
				if (tagMatcher.find()) {
					int attributeOffset = previousContentLength + tagMatcher.end();

					String componentName = component.getName();
					StringBuffer contentStringBuffer = new StringBuffer(contentStr);
					String componentNameTag = "_componentName";
					if (contentStr.substring(attributeOffset).startsWith(componentNameTag)) {
						int openQuoteIndex = contentStr.indexOf("\"", attributeOffset);
						contentStringBuffer.insert(openQuoteIndex + 1, componentName + ",");
					}
					else {
						contentStringBuffer.insert(attributeOffset, " " + componentNameTag + " = \"" + componentName + "\" ");
					}

					response.setContent(contentStringBuffer.toString());
				}
			}
		}
	}
}
