package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomComponent;

/**
 * @d2wKey object
 */
public class ERDDisplayTemplateString extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDDisplayTemplateString(WOContext context) {
        super(context);
    }

    public String templateString() {
        return (String) valueForBinding("templateString");
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
}
