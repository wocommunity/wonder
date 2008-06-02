package er.extensions.components;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WOAnyField;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Class for Wonder Component ERXAnyField.
 * @created ak on Thu Feb 27 2003
 */

public class ERXAnyField extends WOAnyField {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXAnyField.class);
	
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
