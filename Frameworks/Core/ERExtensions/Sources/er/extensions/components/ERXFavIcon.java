package er.extensions.components;

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
 * @binding type the type of icon to use. Default is for a favicon, 
 * 			while "touch" generates an 'apple-touch-icon-precomposed' 
 * 			icon for android and iDevices. For information about icon
 * 			sizes, see <a href="http://mathiasbynens.be/notes/touch-icons">touch icons</a>
 * @author ak
 */
public class ERXFavIcon extends WOHTMLDynamicElement {

	protected WOAssociation _href;
	protected WOAssociation _framework;
	protected WOAssociation _filename;
	protected WOAssociation _type;

	public ERXFavIcon(String aName, NSDictionary associations, WOElement template) {
		super("link", associations, template);
		_href = _associations.removeObjectForKey("href");
		_type = _associations.removeObjectForKey("type");
		_framework = _associations.removeObjectForKey("framework");
		_filename = _associations.removeObjectForKey("filename");
		if(_filename == null && _href == null) {
			throw new WODynamicElementCreationException("Either 'href' or 'filename' must be bound: " + this);
		}
		if(_filename != null && _href != null) {
			throw new WODynamicElementCreationException("'href' and 'filename' can't both  be bound: " + this);
		}
	}

	@Override
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
			href = rs.urlForResourceNamed(filename, framework, null, context.request());
		}
		response._appendTagAttributeAndValue("href", href, false);
		String rel = "SHORTCUT ICON";
		if(_type != null) {
			String val = (String) _type.valueInComponent(component);
			if("touch".equalsIgnoreCase(val)) {
				rel = "apple-touch-icon-precomposed";
			}
		}
		response._appendTagAttributeAndValue("rel", rel, false);
		super.appendAttributesToResponse(response, context);
	}
	
	@Override
	protected boolean hasContent() { return false; }
	
}
