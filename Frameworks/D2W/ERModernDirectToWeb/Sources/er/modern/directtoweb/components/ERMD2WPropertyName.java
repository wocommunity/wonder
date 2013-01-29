package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WPropertyName;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Modern property name component.
 * 
 * @binding localContext
 * @d2wKey displayRequiredMarkerCell
 * @d2wKey escapeHTML
 * @d2wKey displayNameForProperty
 * @d2wKey componentName
 * @d2wKey customComponentName
 * @d2wKey hidePropertyName
 * @d2wKey displayRequiredMarker
 * @d2wKey keyPathsWithValidationExceptions
 * 
 * 
 * @author davidleber
 *
 */
public class ERMD2WPropertyName extends ERD2WPropertyName {
	
    public ERMD2WPropertyName(WOContext context) {
        super(context);
    }

	public String fieldLabelElement() {
		return  isEditing() ? "label" : "span";
	}
	
	public String fieldLabelClass() {
		String capKey = ERXStringUtilities.capitalize(propertyKey());
		String result = hasNoErrors() ? "" : " ErrorLabel";
		if (isEditing()) {
			result = capKey + "Label" + result;
		} else {
			result = "Label " + capKey + "Label" + result;
		}
		return result;
	}
   
	public String fieldLabelForValue() {
		return isEditing() ? d2wContext().displayNameForProperty() : null;
	}
	
	@Override
	public boolean isEditing() {
		String task = d2wContext().task();
		return "edit".equals(task) || "query".equals(task);
	}
    
    
}