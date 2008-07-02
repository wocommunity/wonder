package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Embeds a flash movie.
 * 
 * @author ak
 * 
 */

public class ERXFlashMovie extends ERXStatelessComponent {
	
	public ERXFlashMovie(WOContext context) {
		super(context);
	}

	public String flashVars() {
		String result = "";
		for(String key : ((NSArray<String>)bindingKeys())) {
			if(key.startsWith("?") && valueForBinding(key) != null) {
				Object value = valueForBinding(key);
				if(value == null) {
					value = "";
				}
				result += ERXStringUtilities.urlEncode(key.substring(1)) + "=" + ERXStringUtilities.urlEncode(value.toString()) + "&";
			}
		}
		return result.length() == 0 ? null : result;
	}

	public String movieUrl() {
		String movieUrl = stringValueForBinding("movieUrl");
		if(movieUrl == null) {
			String name = stringValueForBinding("movieName");
			String framework = stringValueForBinding("framework");
			movieUrl = application().resourceManager().pathURLForResourceNamed(name, framework, context().request().browserLanguages()).toExternalForm();
		}
		return movieUrl;
	}
}