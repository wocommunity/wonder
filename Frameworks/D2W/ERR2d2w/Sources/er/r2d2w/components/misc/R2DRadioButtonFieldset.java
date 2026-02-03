package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXRadioButtonMatrix;

public class R2DRadioButtonFieldset extends ERXRadioButtonMatrix {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DRadioButtonFieldset(WOContext context) {
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

	public String isCurrentItemDisabled() {
		return disabled()?"disabled":null;
	}
}