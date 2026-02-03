package er.r2d2w.components.buttons;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.buttons.ERDSelectionComponent;
import er.extensions.appserver.ERXWOContext;

public class R2DSelectionComponent extends ERDSelectionComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	private String labelID;
	
    public R2DSelectionComponent(WOContext context) {
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
			labelID = ERXWOContext.safeIdentifierName(context(), false);
		}
		return labelID;
	}
}