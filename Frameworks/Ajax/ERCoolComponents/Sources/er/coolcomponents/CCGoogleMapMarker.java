package er.coolcomponents;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxDynamicElement;

/**
 * WebObjects wrapper for INGoogleMap.js.
 *
 * <p>
 * CCGoogleMapMarker replaces AjaxGmapMarker and will allow you to easily insert one or more google markers inside a CCGoogleMap, CCGoogleMapMarker tag can be added everywhere in the page</p>
 *
 * <p>CCGoogleMapMarker will also insert an html Address element on the page,
 * the content of CCGoogleMapMarker wil be copied inside the ballon thet people wil see clicking on the marker
 * if you don't want the address element to be shown on page simply set display none using css (id, class or style)</p>
 * 
 * @binding id the id of the address element that CCGoogleMapMarker will generate
 * @binding class the class of the address element
 * @binding style the style of the address element
 * @binding googleMapId REQUIRED the id of the google map where you want the marker to be placed
 * @binding the address of the marked, formatted for google, like: 1 Infinite Loop, Cupertino CA, United States
 * @binding lat the latidude of the marker. if lat and lng are specified they will override the address lookup
 * @binding lng the longitude of the marker. if lat and lng are specified they will override the address lookup
 *
 * 
 * @see <a href="https://github.com/amedeomantica/INWebTools">INWebTools</a>
 *
 * @author amedeomantica (WebObjects wrapper and INGoogleMap.js)
 */

public class CCGoogleMapMarker extends AjaxDynamicElement {

	private WOAssociation _elementId;
    private WOAssociation _elementClass;
    private WOAssociation _elementStyle;
    private WOAssociation _googleMapId;
    private WOAssociation _draggable;
    private WOAssociation _address;
    private WOAssociation _lat;
    private WOAssociation _lng;
    private WOElement _children;
	
	public CCGoogleMapMarker(String name,
			NSDictionary<String, WOAssociation> someAssociations, WOElement children) {
		super(name, someAssociations, children);
		
		_elementId = someAssociations.objectForKey("id");
		_elementStyle = someAssociations.objectForKey("style");
		_elementClass = someAssociations.objectForKey("class");
		
		_googleMapId = someAssociations.objectForKey("googleMapId");
		_draggable = someAssociations.objectForKey("draggable");
		_address = someAssociations.objectForKey("address");
		_lat = someAssociations.objectForKey("lat");
		_lng = someAssociations.objectForKey("lng");
		
		_children = children;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		// We need to call these otherwise the method "addRequiredWebResources" would not be called
		super.appendToResponse(response, context);
		
		response.appendContentString("<address ");
    	
    	if(_elementId != null) {
    		response.appendContentString("id=\"" + _elementId.valueInComponent(context.component()) + "\" ");
    	}
    	
    	String classValue = "in_GoogleMapMarker ";
    	if(_elementClass != null) {
    		classValue = classValue + (String)_elementClass.valueInComponent(context.component());
    	}
    	response.appendContentString("class=\"" + classValue + "\" ");
    	
    	if(_elementStyle != null) {
    		response.appendContentString("style=\"" + _elementStyle.valueInComponent(context.component()) + "\" ");
    	}

    	response.appendContentString("data-in_GoogleMap-id=\"" + _googleMapId.valueInComponent(context.component()) + "\" ");
    	
    	String draggable = "false";
    	if(_draggable!=null) {
    		draggable = (String) _draggable.valueInComponent(context.component());
    	}
    	
    	response.appendContentString("data-draggable=\"" + draggable + "\" ");
    	
    	if (_address!=null) {
			response.appendContentString("data-address=\""
					+ _address.valueInComponent(context.component()) + "\" ");
		}
    	
    	if (_lng != null && _lat != null) {
    		response.appendContentString("data-lng=\"" + _lng.valueInComponent(context.component()) + "\" ");
    		response.appendContentString("data-lat=\"" + _lat.valueInComponent(context.component()) + "\"");
    	}
    	
    	response.appendContentString(">");
    	
    	if(_children!=null) {
    		_children.appendToResponse(response, context);
    	}
    	
    	response.appendContentString("</address>");
		
	}
	
	
	@Override
	protected void addRequiredWebResources(WOResponse response,
			WOContext context) {
		
		addScriptResourceInHead(context, response, "ERCoolComponents", "INGoogleMaps.min.js");
		
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
