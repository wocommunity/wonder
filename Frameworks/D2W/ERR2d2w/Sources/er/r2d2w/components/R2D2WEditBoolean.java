package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditBoolean;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

public class R2D2WEditBoolean extends D2WEditBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WEditBoolean(WOContext context) {
        super(context);
    }

    private static final String _COMPONENT_CLASS = "boolean";
	private String labelID;
	private NSArray<String> _choicesNames;

	public String componentClasses() {
    	return _COMPONENT_CLASS;
    }
    
    public void reset() {
    	super.reset();
    	labelID = null;
        _choicesNames = null;
    }

	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}

    public NSArray<String> choicesNames() {
        if (_choicesNames == null)
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    public String stringForYes() {
        return choicesNames().objectAtIndex(0);
    }
    
    public String stringForNo() {
        return choicesNames().objectAtIndex(1);
    }
    
    public String stringForNull() {
        if(choicesNames().count() > 2) {
            String choice = choicesNames().objectAtIndex(2);
            choice = ERXLocalizer.currentLocalizer().localizedStringForKey(choice);
            return choice;
        }
        return null;
    }
    
}