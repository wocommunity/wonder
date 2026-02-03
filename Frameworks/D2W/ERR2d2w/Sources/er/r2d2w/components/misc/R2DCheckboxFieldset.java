package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXCheckboxMatrix;

public class R2DCheckboxFieldset extends ERXCheckboxMatrix {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DCheckboxFieldset(WOContext context) {
        super(context);
    }

    private String labelID;
	
	public void reset() {
		labelID = null;
		super.reset();
	}

	/**
	 * @return the labelID
	 */
	public String labelID() {
		return labelID;
	}

	public String generateID() {
		labelID = "id" + context().elementID();
		return labelID;
	}
	
	public boolean checked() {
		boolean checked = selections() != null && selections().containsObject(currentItem);
		return checked;
	}
	
	public boolean disabled() {
		boolean disabled = booleanValueForBinding("disabled", false);
		return disabled;
	}
	
    public String checkboxChecked() {
        return checked() ? "checked" : null;
    }
    
    public String checkboxDisabled() {
    	return disabled() ? "disabled" : null;
    }

}