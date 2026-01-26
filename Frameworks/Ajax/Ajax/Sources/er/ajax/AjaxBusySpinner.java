package er.ajax;

import java.util.Optional;
import java.util.function.Consumer;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXPropertyListSerialization;

/**
 * AjaxBusySpinner provides various ways of performing operations when an Ajax request is in process.
 * It uses http://fgnass.github.com/spin.js to render the spinner, and can be configured to the desired
 * style using the optional bindings
 * 
 * @binding busyClass (optional) the CSS class to apply to the updating element during the request
 * @binding divID (optional) the id of the div to show and hide during the request
 * @binding onCreate (optional) the function to execute when the request starts
 * @binding onComplete (optional) the function to execute when the request ends
 * @binding watchContainerID (optional) if set, the other bindings will only apply when this container ID is being
 *          updated, which provides for per-element busy controls
 * @binding id (optional) if bound, you can provide a custom style for the generated busy image div
 * @binding class (optional) if bound, you can provide a custom style for the generated busy image div 
 * @binding style (optional) if bound, you can provide a custom style for the generated busy image div
 * @binding lines (optional) number of lines to draw
 * @binding length (optional) length of each line
 * @binding width (optional) line thickness
 * @binding radius (optional) radius of the inner circle
 * @binding color (optional) #rgb or #rrggbb
 * @binding speed (optional) rounds per second
 * @binding trail (optional) afterglow percentage
 * @binding shadow (optional) whether to render a shadow
 * @binding spinOpts (optional) json style list of spinner options (explicit binding values take precedence)
 * 
 * @property er.extensions.ERXResponseRewriter.resource.Ajax.spin.js=Ajax.spin.min.js replaces the normal
 *           spin.js file with the minified version
 * 
 * @author qdolan
 */
public class AjaxBusySpinner extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public AjaxBusySpinner(WOContext context) {
		super(context);
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	protected void addRequiredWebResources(WOResponse res) {
		addScriptResourceInHead(res, "prototype.js");
		addScriptResourceInHead(res, "effects.js");
		addScriptResourceInHead(res, "wonder.js");
		addScriptResourceInHead(res, "spin.js");
	}

	public boolean customStyle() {
		return hasBinding("id") || hasBinding("class") || hasBinding("style");
	}

	public String style() {
		String style = (String) valueForBinding("style", "display: none");
		return style;
	}

	public String divID() {
		String id = (String) valueForBinding("divID");
		if (id == null) {
			id = (String) valueForBinding("id");
			if (id == null) {
				id = "busy";
			}
		}
		return id;
	}

	public String onCreate() {
		return (String) valueForBinding("onCreate", "null");
	}

	public String onComplete() {
		return (String) valueForBinding("onComplete", "null");
	}
	
	public String spinOpts() {
		String defaults = (String) valueForBinding("spinOpts", "{speed:1,color:'#000',shadow:false,trail:60,width:4,length:7,radius:10,lines:12}");
		NSDictionary<String, Object> json = ERXPropertyListSerialization.dictionaryForJSONString(defaults);
		Consumer<String> cons = key -> Optional.ofNullable(valueForBinding(key)).ifPresent(val -> json.put(key, val));
		cons.accept("lines");
		cons.accept("length");
		cons.accept("width");
		cons.accept("radius");
		cons.accept("color");
		cons.accept("speed");
		cons.accept("trail");
		cons.accept("shadow");
		return ERXPropertyListSerialization.jsonStringFromPropertyList(json, true);
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}
