package er.extensions;
import com.webobjects.appserver.*;
import com.webobjects.woextensions.*;

/**
 * Class for Wonder Component ERXAnyField.
 * @created ak on Thu Feb 27 2003
 */

public class ERXAnyField extends WOAnyField {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERXAnyField.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERXAnyField(WOContext context) {
        super(context);
    }


    public String itemName() {
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(ERXStringUtilities.displayNameForKey(selectedKeyItem));
    }
}
