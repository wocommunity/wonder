package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.foundation.ERXStringUtilities;

public class R2D2WQueryPassword extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private String labelID;

	public R2D2WQueryPassword(WOContext context) {
        super(context);
    }

	public void reset() {
		labelID = null;
		super.reset();
	}
	
	/**
	 * @return the labelID
	 */
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(),"id",'_');
		}
		return labelID;
	}

}