package er.coolcomponents;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXProperties;

/**
 * WebObjects wrapper for INGoogleMap.js.
 *
 * <p>
 * CCGoogleMap replaces AjaxGmap and will allow you to easily insert a google map inside your page</p>
 * <p>
 * You must set the property ajax.google.maps.V3.apiKey in your Properties file</p>
 *
 * <p>please note that is REQUIRED to set the widht height of the map, you can use id, class, or style to do that</p>
 * 
 * @binding id the id of the div element that CCGoogleMap will generate
 * @binding class the class of the div element
 * @binding style the style of the div element
 * @binding zoom the desidered map startup zoom level
 * @binding the address of the marked, formatted for google, like: 1 Infinite Loop, Cupertino CA, United States
 * @binding lat the latidude of the map center. if lat and lng are specified they will override the address lookup
 * @binding lng the longitude of the map center. if lat and lng are specified they will override the address lookup
 * @binding type, can be ROADMAP, SATELLITE, HYBRID or TERRAIN, default to ROADMAP. all uppercase string
 *
 * 
 * @see <a href="https://github.com/amedeomantica/INWebTools">INWebTools</a>
 *
 * @author amedeomantica (WebObjects wrapper and INGoogleMap.js)
 */

public class CCGoogleMap extends AjaxDynamicElement {
    //private static Logger log = Logger.getLogger(CCGoogleMap.class);
	
    private WOAssociation _elementId;
    private WOAssociation _elementClass;
    private WOAssociation _elementStyle;
    private WOAssociation _zoom;
    private WOAssociation _type;
    private WOAssociation _address;
    private WOAssociation _lat;
    private WOAssociation _lng;
    
	public CCGoogleMap(String aName, NSDictionary someAssociations,
			WOElement template) {
		super(aName, someAssociations, template);
		
		_elementId = (WOAssociation) someAssociations.objectForKey("id");
		_elementStyle = (WOAssociation) someAssociations.objectForKey("style");
		_elementClass = (WOAssociation) someAssociations.objectForKey("class");
		
		_zoom = (WOAssociation) someAssociations.objectForKey("zoom");
		_type = (WOAssociation) someAssociations.objectForKey("type");
		_address = (WOAssociation) someAssociations.objectForKey("address");
		_lat = (WOAssociation) someAssociations.objectForKey("lat");
		_lng = (WOAssociation) someAssociations.objectForKey("lng");
		
		if( ((_lat == null )||( _lng == null )) && (_address == null)) {
			throw new WODynamicElementCreationException("Unable to create CCGoogleMap, missing coordinates or address");
		}
		
	}

	@Override
    public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
    	AjaxUtils.addScriptCodeInHead(response, context, "var in_googleApiKey=\"" +
    			ERXProperties.stringForKey("ajax.google.maps.V3.apiKey") + "\"");
    	
    	response.appendContentString("<div ");
    	
    	if(_elementId != null) {
    		response.appendContentString("id=\"" + _elementId.valueInComponent(context.component()) + "\" ");
    	}
    	
    	String classValue = "in_GoogleMap ";
    	if(_elementClass != null) {
    		classValue = classValue + (String)_elementClass.valueInComponent(context.component());
    	}
    	response.appendContentString("class=\"" + classValue + "\" ");
    	
    	if(_elementStyle != null) {
    		response.appendContentString("style=\"" + _elementStyle.valueInComponent(context.component()) + "\" ");
    	}
    	
    	String zoomValue = "14";
    	if(_zoom!=null) {
    		zoomValue = (String) _zoom.valueInComponent(context.component());
    	}
    	response.appendContentString("data-zoom=\"" + zoomValue + "\" ");
    	
    	String mapType = "ROADMAP";
    	if(_type!=null) {
    		mapType = (String) _type.valueInComponent(context.component());
    	}
    	
    	response.appendContentString("data-type=\"" + mapType + "\" ");
    	
    	if(_address!=null) {
    		response.appendContentString("data-address=\"" + _address.valueInComponent(context.component()) + "\" ");
    	}
    	
    	if (_lng != null && _lat != null) {
    		response.appendContentString("data-lng=\"" + _lng.valueInComponent(context.component()) + "\" ");
    		response.appendContentString("data-lat=\"" + _lat.valueInComponent(context.component()) + "\"");
    	}
    	
    	response.appendContentString("></div>");
    }


	@Override
	protected void addRequiredWebResources(WOResponse response,WOContext context) {
		addScriptResourceInHead(context, response, "ERCoolComponents", "INGoogleMaps.min.js");
	}


	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		// TODO Auto-generated method stub
		return null;
	}
    
}