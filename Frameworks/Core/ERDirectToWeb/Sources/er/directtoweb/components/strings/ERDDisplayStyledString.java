package er.directtoweb.components.strings;

import java.text.Format;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * The as D2WDisplayStyledString, except that you can add a formatter and have a CSS class.
 * 
 *
 * @author ak
 */
public class ERDDisplayStyledString extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDDisplayStyledString(WOContext context) {
        super(context);
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public boolean isNotItalic() {
        return !booleanValueForBinding("italic");
    }
    
    public boolean isNotBold() {
        return !booleanValueForBinding("bold");
    }
    
    public boolean hasNoColor() {
        return valueForBinding("color") == null;
    }
    
    public boolean hasNoCssClass() {
        return valueForBinding("cssClass") == null;
    }
    
    public Format formatter() {
        return null;
    }
}
