package er.r2d2w.components.relationships;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.relationships.ERD2WEditToOneFault;
import er.extensions.localization.ERXLocalizer;

public class R2D2WEditToOneFault extends ERD2WEditToOneFault {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WEditToOneFault(WOContext context) {
        super(context);
    }

    public String helpString() {
    	String helpString = (String)d2wContext().valueForKey("tooltip");
    	if(helpString==null) {
    		helpString = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("R2D2W.editToOneFault.helpString", d2wContext());
    	}
    	return helpString;
    }

    private static final String _COMPONENT_CLASS = "toOne";
    
    public String componentClasses() {
    	return _COMPONENT_CLASS;
    }

}