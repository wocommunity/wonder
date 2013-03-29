package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXProperties;

/**
 * Instantiates a Google Map (an object of GMap2 javascript class) at the given location (specified by lat&lng or address) with given properties. IMPORTANT: your GMaps api key must be specified in your properties file as <code>ajax.google.maps.apiKey</code>.
 * 
 * @author eric robinson
 * @binding id the id of the div that the map is rendered into. If none is given, a unique id will be generated. This is also the name of the map javascript object, which can be accessed after the map has been instantiated.
 * @binding address The address that the map will be centered on at load. 
 * @binding lng longitude for map center (must be paired with lat, cannot coexist with address)
 * @binding lat latitude for map center (must be paired with lng, cannot coexist with address)
 * @binding width the width of the map (does not have to be specified here, but must be specified somewhere [ie. css])
 * @binding height the height of the map (does not have to be specified here, but must be specified somewhere [ie. css])
 * @binding control control type name. Will automatically append a 'G' before and 'Control' after the name given, possible values here: http://www.google.com/apis/maps/documentation/reference.html#GControlImpl
 * @binding zoomLevel zoom level of map (will default to 13 is not specified). Higher is closer.
 * @binding apiKey apiKey to use for the map, if you want to ovverride the property below
 * @property ajax.google.maps.apiKey an api key you can get from http://www.google.com/apis/maps/ . If your app runs on http://ip:port/cgi-bin/WebObjects/GoogleMaps.woa, register the key for http://ip:port/cgi-bin/WebObjects/ . Using a fixed WO port is recommended (unless you want to get a new api key everytime you restart your server). AjaxGMaps will not work without an Api Key. 
 */

@Deprecated
public class AjaxGMap extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String _id;

	public AjaxGMap(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	@Override
	protected void addRequiredWebResources(WOResponse response) {
		String mapApiJsFilename = "http://maps.google.com/maps?file=api&amp;v=2&amp;key=" + apiKey();
		addScriptResourceInHead(response, mapApiJsFilename);
		addScriptResourceInHead(response, "WonderGMapsHelpers.js");
	}

	public String id() {
		if (_id == null) {
			_id = (String) valueForBinding("id");
			if (_id == null) {
				_id = ERXWOContext.safeIdentifierName(context(), true);
			}
		}
		return _id;
	}
	
	protected String apiKey() {
		return valueForStringBinding("apiKey", ERXProperties.stringForKey("ajax.google.maps.apiKey"));
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

	public String mapContainerStyle() {
		Object width = valueForBinding("width");
		Object height = valueForBinding("height");

		if (width instanceof Integer) {
			width = width + "px";
		}

		if (height instanceof Integer) {
			height = height + "px";
		}

		if (width == null || height == null) {
			return null;
		}
		return "width: " + width + "; height: " + height;
	}
}
