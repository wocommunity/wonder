package er.extensions;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

/**
 * FavIcon link with resource manager support.
 * @binding href href to the icon
 * @binding filename filename of the icon
 * @binding framework framework of the icon
 * @author ak
 */
public class ERXFavIcon extends WOHTMLDynamicElement {

	protected WOAssociation _href;
	protected WOAssociation _framework;
	protected WOAssociation _filename;

	public ERXFavIcon(String aName, NSDictionary associations, WOElement template) {
		super("link", associations, template);
		_href = (WOAssociation) _associations.removeObjectForKey("href");
		_framework = (WOAssociation) _associations.removeObjectForKey("framework");
		_filename = (WOAssociation) _associations.removeObjectForKey("filename");
		if(_filename == null && _href == null) {
			throw new WODynamicElementCreationException("Either 'href' or 'filename' must be bound: " + this);
		}
		if(_filename != null && _href != null) {
			throw new WODynamicElementCreationException("'href' and 'filename' can't both  be bound: " + this);
		}
	}

	public void appendAttributesToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		String href;
		if(_href != null) {
			href = (String)_href.valueInComponent(component);
		} else {
			String framework = "app";
			if(_framework != null) {
				Object val = _framework.valueInComponent(component);
				if(val != null) {
					framework = val.toString();
				}
			}
			String filename = (String)_filename.valueInComponent(component);
			WOResourceManager rs = WOApplication.application().resourceManager();
			href = rs.urlForResourceNamed(filename, framework, null, null);
		}
		response._appendTagAttributeAndValue("href", href, false);
		response._appendTagAttributeAndValue("rel", "SHORTCUT ICON", false);
		super.appendAttributesToResponse(response, context);
	}
	
}
