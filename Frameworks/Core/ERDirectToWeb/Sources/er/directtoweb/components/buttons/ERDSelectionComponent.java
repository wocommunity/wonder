package er.directtoweb.components.buttons;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.interfaces.ERDPickPageInterface;

/**
 * For editing a selection in a list repetition. You'd typicically but this somewhere into the actions.
 * @author ak on Thu Sep 04 2003
 */
public class ERDSelectionComponent extends ERDActionButton {
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
    public ERDSelectionComponent(WOContext context) {
        super(context);
    }

    public boolean checked() {
        return selectedObjects().containsObject(object());
    }

    public void setChecked(boolean newChecked) {
        if (newChecked) {
            if (!selectedObjects().containsObject(object())) {
            	selectedObjects().addObject(object());
            }
        } else {
            selectedObjects().removeObject(object());
        }
    }

    public NSMutableArray selectedObjects() {
    	ERDPickPageInterface pickPage = parentPickPage();
    	//ak: crude hack, we should convert to mutable and set the changed array
    	return (NSMutableArray) pickPage.selectedObjects();
    }
    
    public String selectionWidgetName() {
        return booleanValueForBinding("singleSelection") ? "WORadioButton" : "WOCheckBox";
    }
}
