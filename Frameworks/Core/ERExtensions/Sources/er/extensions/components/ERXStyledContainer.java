package er.extensions.components;

import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOGenericContainer;
import com.webobjects.appserver._private.WOURLValuedElementData;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Same as a WOGenericContainer, except that you can add individual style attributes by
 * supplying <code>style.background-color="red"</code> bindings. This is sometimes preferable to
 * putting all the styles in code.<br>
 * If a normal <code>style</code> attribute is present, its value will be prepended. You can also
 * define a background-image and a <code>style.background-image.type</code> (mime type of the image in case it is an NSData).
 * Some fixing will be done for you, like you can give a <code>style.unit</code> which will be applied to all bindings
 * that evalutate to a number. If none is supplied, "px" is appended to make size definitions settable via plain numerical bindings.
 *
 * @binding elementId The type of element (div, p, span, etc.) to generate
 *
 * @author ak
 */
public class ERXStyledContainer extends WOGenericContainer {

	NSMutableDictionary _styles;
	WOAssociation _style;
	WOAssociation _mimeType;
	WOAssociation _unit;
	
	public ERXStyledContainer(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_style = _associations.removeObjectForKey("style");
		_styles = new NSMutableDictionary();
		for (Enumeration enumerator = _associations.keyEnumerator(); enumerator.hasMoreElements();) {
			String key = (String) enumerator.nextElement();
			if(key.startsWith("style.")) {
				String styleKey = key.substring(6);
				WOAssociation association = _associations.removeObjectForKey(key);
				if("background-image.type".equals(styleKey)) {
					_mimeType = association;
				} else if("unit".equals(styleKey)) {
					_unit = association;
				} else {
					_styles.setObjectForKey(association, styleKey);
				}
			}
		}
	}

	@Override
	 public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
		 super.appendAttributesToResponse(woresponse, wocontext);
		 WOComponent component = wocontext.component();
		 StringBuilder style = new StringBuilder();
		 if(_style != null) {
			 String s = (String) _style.valueInComponent(component);
			 if(s != null) {
				 style.append(s);
				 style.append("; ");
			 }
		 }
		 if(_styles.count() > 0) {
			 for (Enumeration enumerator = _styles.keyEnumerator(); enumerator.hasMoreElements();) {
				 String key = (String) enumerator.nextElement();
				 WOAssociation association = (WOAssociation) _styles.objectForKey(key);
				 Object value = association.valueInComponent(component);
				 if(value != null) {
					 String stringValue;
					 if("background-image".equals(key)) {
						 if (value instanceof NSData) {
							 NSData data = (NSData) value;
							 WOResourceManager rm = WOApplication.application().resourceManager();
							 String mimeType = (String) (_mimeType != null ? _mimeType.valueInComponent(component) : "image/jpeg");
							 WOURLValuedElementData uve = new WOURLValuedElementData(data, mimeType, null);
							 rm._cacheData(uve);
							 stringValue = uve.dataURL(wocontext);
						 } else {
							 stringValue = value.toString();
						 }
						 if(stringValue.indexOf("url(") < 0) {
							 stringValue = "url(" + stringValue + ")";
						 }
					 } else {
						 stringValue = value.toString();
					 }
					 if(value instanceof Number) {
						 stringValue += (_unit != null ? _unit.valueInComponent(component) : "px");
					 }
					 style.append(key).append(": ").append(stringValue).append("; ");
				 }
			 }
		 }
		 if(style.length() > 0) {
			 woresponse._appendTagAttributeAndValue("style", style.toString(), false);
		 }
	 }
}
