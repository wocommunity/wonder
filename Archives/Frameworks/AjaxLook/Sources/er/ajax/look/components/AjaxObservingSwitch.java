package er.ajax.look.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSNotificationCenter;

import er.directtoweb.components.ERDCustomComponent;
import er.extensions.appserver.ERXWOContext;

public class AjaxObservingSwitch extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	
    public AjaxObservingSwitch(WOContext context) {
        super(context);        
    }
    
    public String id() {
    	if(id == null) {
    		id = ERXWOContext.safeIdentifierName(context(), true);
    		AjaxNotificationCenter.PROPERTY_OBSERVER_ID.takeValueInObject(id, d2wContext());
    		NSNotificationCenter.defaultCenter().postNotification(AjaxNotificationCenter.RegisterPropertyObserverIDNotification, d2wContext());
    	}
    	return id;
    }

	public void postChangeNotification() {
		NSNotificationCenter.defaultCenter().postNotification(AjaxNotificationCenter.PropertyChangedNotification, d2wContext());
	}

	private Object object;

	/**
	 * @return the object
	 */
	public Object object() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}
}