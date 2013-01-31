package er.directtoweb.delegates;

import java.io.Serializable;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

public class ERD2WSelectActionDelegate implements NextPageDelegate, Serializable {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final ERD2WSelectActionDelegate instance = new ERD2WSelectActionDelegate();

	public WOComponent nextPage(WOComponent sender) {
		WOComponent target = D2WEmbeddedComponent.findTarget(sender);
		if(target.hasBinding("selectedObject")){
			EOEnterpriseObject selectedObject = ((SelectPageInterface)sender).selectedObject();
			target.setValueForBinding(selectedObject, "selectedObject");
		}
		return (WOComponent)target.valueForBinding("action");
	}

}
