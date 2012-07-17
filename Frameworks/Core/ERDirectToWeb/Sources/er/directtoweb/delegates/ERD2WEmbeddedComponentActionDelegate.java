
package er.directtoweb.delegates;

import java.io.Serializable;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.directtoweb.NextPageDelegate;

public class ERD2WEmbeddedComponentActionDelegate implements Serializable, NextPageDelegate {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static ERD2WEmbeddedComponentActionDelegate instance = new ERD2WEmbeddedComponentActionDelegate();

	public WOComponent nextPage(WOComponent sender) {
		return (WOComponent) D2WEmbeddedComponent.findTarget(sender).valueForBinding("action");
	}

}
