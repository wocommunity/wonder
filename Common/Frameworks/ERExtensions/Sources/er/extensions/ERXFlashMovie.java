package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

import java.util.Enumeration;

/**
 * Embeds a flash movie. You can add to the FlashVars by adding bindings with
 * "?" for simple values and "??" for actions on your page (which should return a WOActionResults).
 * @author ak
 * 
 */

public class ERXFlashMovie extends ERXStatelessComponent {
	
	public ERXFlashMovie(WOContext context) {
		super(context);
	}

	public String flashVars() {
		String result = "";
		for(Enumeration keysEnum = bindingKeys().objectEnumerator(); keysEnum.hasMoreElements();) {
			String key = (String)keysEnum.nextElement();
			if(key.startsWith("?")) {
				String flashKey = key.substring(1);
				Object value = null;
				if(key.startsWith("??")) {
					String url =  context().componentActionURL();
					url = ERXStringUtilities.keyPathWithoutLastProperty(url);
					flashKey = key.substring(2);
					value = url + "." + flashKey;
				} else {
					value = valueForBinding(key);
				}
				if(value != null) {
					result += ERXStringUtilities.urlEncode(flashKey) + "=" + ERXStringUtilities.urlEncode(value.toString()) + "&";
				}
			}
		}
		return result.length() == 0 ? null : result;
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		String url = ERXStringUtilities.keyPathWithoutLastProperty(context.senderID());
		if(context.elementID().equals(url)) {
			String last =  ERXStringUtilities.lastPropertyKeyInKeyPath(context.senderID());
			result = (WOActionResults) valueForBinding("??" + last);
		} else {
			result = super.invokeAction(request, context);
		}
		return result;
	}
	
	public String movieUrl() {
		String movieUrl = stringValueForBinding("movieUrl");
		if(movieUrl == null) {
			String name = stringValueForBinding("movieName");
			String framework = stringValueForBinding("framework");
			movieUrl = application().resourceManager().urlForResourceNamed(name, framework, context().request().browserLanguages(), context().request());
		}
		return movieUrl;
	}
}