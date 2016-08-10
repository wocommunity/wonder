package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WOAnyField;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * ERXAnyField, who extends WOAnyField, shows a popup with a list of keys for an entity to
 * let you filter a display group.  
 *
 * @binding displayGroup The display group to apply the filter on
 * @binding keyList Array of strings of available keys to filter the display group
 * @binding key An item in the key list
 * @binding selectedKey Selection made by the user in the key list
 * @binding sourceEntity Name Name of the entity
 * @binding value Value of the qualifier
 * @binding displayKey
 * @binding formatter
 * @binding relationshipKey
 * 
 * @see WOAnyField
 * 
 * @author ak on Thu Feb 27 2003
 */

public class ERXAnyField extends WOAnyField {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
