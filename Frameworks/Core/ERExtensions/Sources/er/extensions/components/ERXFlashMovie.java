package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Embeds a flash movie. You can add to the FlashVars by adding bindings with
 * "?" for simple values and "??" for actions on your page (which should return a WOActionResults).
 * @author ak
 * 
 * @binding movieUrl optional, the full url of the movie file's location
 * @binding movieName optional, the filename of the movie relative to your WebServerResources (e.g. movies/mymovie.swf). Must provide a framework binding when using movieName
 * @binding framework optional, the framework in which the file specified by movieName resides. Must provide a movieName when using framework
 * @binding ? binding for simple FlashVars
 * @binding ?? binding to tie actions on your page to FlashVars (should return a WOActionResults)
 * 
 */

public class ERXFlashMovie extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXFlashMovie(WOContext context) {
		super(context);
	}

	public String flashVars() {
		String result = "";
		for (String key : bindingKeys()) {
			if(key.startsWith("?")) {
				String flashKey = key.substring(1);
				Object value = null;
				if(key.startsWith("??")) {
					String url =  context().componentActionURL();
					url = ERXStringUtilities.keyPathWithoutLastProperty(url);
					flashKey = key.substring(2);
					//ak: flag for keys containing "." like JWPlayer 
					value = url + ".@" + flashKey;
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
		String url = context.senderID();
		if(url.startsWith(context.elementID() + ".@")) {
			String last = context.senderID().substring(context.elementID().length()+2);
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