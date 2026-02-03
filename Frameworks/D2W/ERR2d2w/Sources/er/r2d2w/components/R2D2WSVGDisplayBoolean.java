package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WSVGDisplayBoolean extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WSVGDisplayBoolean(WOContext context) {
        super(context);
    }

    public Boolean isFalse() {
		if(objectPropertyValueIsNonNull()) {
			boolean b = ERXValueUtilities.booleanValue(objectPropertyValue());
			return Boolean.valueOf(!b);
		}
		return Boolean.FALSE;
	}

	public String spanClass() {
		StringBuilder sb = new StringBuilder(20);
		if(ERXValueUtilities.booleanValue(d2wContext().valueForKey("isMandatory"))) {
			sb.append("allowsNull ");
		}
		Boolean b = (Boolean) objectPropertyValue();
		if(b == null) {
			sb.append("boolNull");
		} else {
			sb.append(Boolean.TRUE.equals(b)?"boolTrue":"boolFalse");
		}
		return sb.toString();
	}

	public String spanTitle() {
		Boolean b = (Boolean) objectPropertyValue();
		if(b == null) {
			return (String) d2wContext().valueForKey("nullString");
		} else {
			 return (String) d2wContext().valueForKey(Boolean.TRUE.equals(b)?"trueString":"falseString");
		}

	}

	public String imageID() {
		StringBuilder sb = new StringBuilder(20);
		if(!ERXValueUtilities.booleanValue(d2wContext().valueForKey("isMandatory"))) {
			sb.append("ballot_");
		}
		Boolean b = (Boolean) objectPropertyValue();
		if(b == null) {
			sb.append("boolean_null");
		} else {
			sb.append(Boolean.TRUE.equals(b)?"boolean_true":"boolean_false");
		}
		return sb.toString();
	}
}