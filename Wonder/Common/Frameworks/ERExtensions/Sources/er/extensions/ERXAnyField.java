package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.woextensions.*;

/**
 * Class for Wonder Component ERXAnyField.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Thu Feb 27 2003
 * @project ERExtensions
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
        return ERXStringUtilities.displayNameForKey(selectedKeyItem);
    }
}
