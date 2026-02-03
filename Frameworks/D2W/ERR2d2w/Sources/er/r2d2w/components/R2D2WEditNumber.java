package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.numbers.ERD2WEditNumber;
import er.extensions.foundation.ERXStringUtilities;

public class R2D2WEditNumber extends ERD2WEditNumber {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WEditNumber(WOContext context) {
        super(context);
    }
    
    private static final String _COMPONENT_CLASS = "number";
	private String labelID;
    
    public String componentClasses() {
    	return _COMPONENT_CLASS;
    }
    
    public void reset() {
    	super.reset();
    	labelID = null;
    }

	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}

}