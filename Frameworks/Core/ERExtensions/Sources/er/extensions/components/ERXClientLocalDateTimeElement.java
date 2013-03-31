/*
  Klaus Berkling for DynEd Sept 19, 2012
  kiberkli@gmail.com
 
  Usage:
  1) Add to your component
  
          ClientLocalDateTime : ERXClientLocalDateTime {}
          
  2) Add
          ClientLocalDateTimeElement : ERXClientLocalDateTimeElement {
               value = <UTC time in seconds>;
               id = <unique identifier>;
               valueWhenEmpty = <alternate value when value is empty>;
               formatFunction = [ clientLocalDateTime  |clientLocalDateTimeUS | clientLocalTime | clientLocalTimeUS | clientLocalDate | clientLocalDateUS ];
               class = [ <class-string> ];
               style = [ <styl-tags> ];
          }

	Supported formatsfunctions are:
	- clientLocalDateTime: Date and time in european style format
	- clientLocalDateTimeUS: Date and time in a US style format
	- clientLocalTime: Time in 24hr format
	- clientLocalTimeUS: Time in US format (12hr am|pm)
	- clientLocalDate: Date in european style format
	- clientLocalDateUS: Date in US style format

*/

package er.extensions.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

public class ERXClientLocalDateTimeElement extends WODynamicElement {

	public static final Logger log = Logger.getLogger(ERXClientLocalDateTimeElement.class);
	
	protected WOAssociation _id;
	protected WOAssociation _value;
	protected WOAssociation _valueWhenEmpty;
	protected WOAssociation _style;
	protected WOAssociation _class;
	protected WOAssociation _formatFunction;
	
	public ERXClientLocalDateTimeElement(String aName, NSDictionary associations, WOElement template) {
		super(null, null, null);
		
		_value = (WOAssociation)associations.objectForKey("value");
		if (_value == null) {
			throw new WODynamicElementCreationException("<"+getClass().getName()+"> no 'value' attribute specified.");
		}
		
		_id = (WOAssociation)associations.objectForKey("id");
		if (_id == null) {
			throw new WODynamicElementCreationException("<"+getClass().getName()+"> no 'id' attribute specified.");
		}
		
		_valueWhenEmpty = (WOAssociation)associations.objectForKey("valueWhenEmpty");
		_style = (WOAssociation)associations.objectForKey("style");
		_class = (WOAssociation)associations.objectForKey("class");
		_formatFunction = (WOAssociation)associations.objectForKey("formatFunction");

	}
	
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		
		String idString = null;
		Long valueLong = null;
		String valueWhenEmptyString = null;
		String styleString = null;
		String classString = null;
		String formatFunction = null;
		
		StringBuilder elementString = new StringBuilder("<input type=\"hidden\" ");
		
		if (_id != null) {
			idString = (String)_id.valueInComponent(component);
			if (idString == null) {
				throw new WODynamicElementCreationException("<"+getClass().getName()+"> no 'id' attribute specified.");
			}
		}
		
		if (_value != null) {
			valueLong = (Long)_value.valueInComponent(component);
		}

		if (_valueWhenEmpty != null) {
			valueWhenEmptyString = (String)_valueWhenEmpty.valueInComponent(component);
		}
		
		if (_style != null) {
			styleString = (String)_style.valueInComponent(component);
		}

		if (_class != null) {
			classString = (String)_class.valueInComponent(component);
		}

		if (_formatFunction != null) {
			formatFunction = (String)_formatFunction.valueInComponent(component);
		} else {
			formatFunction = "clientLocalDateTime";
		}

		// ======================================================================
		
		if (valueLong != null && valueLong >= 0) {
			elementString.append("value=\""+valueLong+"\" ");
		} else if (valueWhenEmptyString != null && valueWhenEmptyString.length() > 0) {
			elementString.append("value=\""+valueWhenEmptyString+"\" ");
		}
		if (idString != null && idString.length() > 0) {
			elementString.append("id=\""+idString+"\" ");
		}
		
		elementString.append("/><div");
		
		if (styleString != null && styleString.length() > 0) {
			elementString.append(" style=\""+styleString+"\"");
		}
		if (classString != null && classString.length() > 0) {
			elementString.append(" class=\""+classString+"\"");
		}
		elementString.append("><script type=\"text/javascript\">document.write("+formatFunction+"(document.getElementById("+idString+").value));</script></div>");
		
		response.appendContentString(elementString.toString());
	}
}
