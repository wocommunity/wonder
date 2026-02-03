package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.appserver.ERXSession;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WEditLanguage extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2D2WEditLanguage(WOContext context) {
        super(context);
    }

    private static final String _COMPONENT_CLASS = "string";
	private String labelID;

    public String componentClasses() {
    	return _COMPONENT_CLASS;
    }
    
    public void reset() {
    	labelID = null;
    	super.reset();
    }
    
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}

	public String noSelectionString() {
		String result = null;
		if(ERXValueUtilities.booleanValue(d2wContext().valueForKeyPath("smartAttribute.allowsNull"))) {
			result = (String)d2wContext().valueForKey("noSelectionStringValue");
		}
		return result;
	}

	public void setObjectPropertyValue(Object newValue) {
		super.setObjectPropertyValue(newValue);
		if(ERXValueUtilities.booleanValue(d2wContext().valueForKey("updatesCurrentLanguage"))) {
			ERXSession session = (ERXSession)session();
			EOEnterpriseObject user = (EOEnterpriseObject)session.objectForKey("user");
			if(user != null) {
				EOEnterpriseObject localUser = EOUtilities.localInstanceOfObject(object().editingContext(), user);
				if(object().equals(localUser)) { session.setLanguage((String)newValue); }
			}
		}
	}
}