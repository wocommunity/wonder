package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WDisplayBoolean extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WDisplayBoolean(WOContext context) {
        super(context);
    }

	public Boolean isFalse() {
		if(objectPropertyValueIsNonNull()) {
			boolean b = ERXValueUtilities.booleanValue(objectPropertyValue());
			return Boolean.valueOf(!b);
		}
		return Boolean.FALSE;
	}
}