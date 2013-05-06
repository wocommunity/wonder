package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXWOContext;

/**
 * Adds a GMarker instance to a Google Map.
 * @author eric robinson
 * @binding id the name of the marker variable, if not specified a unique id is generated automatically.
 * @binding mapID the id used when created AjaxGMap component. The id of the map which the marker is added to.
 * @binding address The address that the marker will be located at. 
 * @binding lng longitude for marker location (must be paired with lat, cannot coexist with address)
 * @binding lat latitude for marker location (must be paired with lng, cannot coexist with address)
 * @binding infoWindowHtml the html that is inside the infowindow
 * @binding options the opts? argument to the constructor of the GMarker class. Value will be place inside {}
 */

@Deprecated
public class AjaxGMarker extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String _id;

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public AjaxGMarker(WOContext context) {
		super(context);
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
}
