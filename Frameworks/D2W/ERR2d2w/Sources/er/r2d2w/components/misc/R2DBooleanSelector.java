package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXBooleanSelector;
import er.extensions.foundation.ERXStringUtilities;

public class R2DBooleanSelector extends ERXBooleanSelector {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String labelID;
	
	public R2DBooleanSelector(WOContext context) {
		super(context);
	}
	
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}
	
	@Override
	public void reset() {
		super.reset();
		labelID = null;
	}
}