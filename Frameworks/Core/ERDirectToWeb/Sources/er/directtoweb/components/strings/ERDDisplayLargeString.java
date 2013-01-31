package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;

public class ERDDisplayLargeString extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDDisplayLargeString(WOContext context) {
        super(context);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
     	return false;
    }

    @Override
    public boolean isStateless() {
     	return true;
    }
}
