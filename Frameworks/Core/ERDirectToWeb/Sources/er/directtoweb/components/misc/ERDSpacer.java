package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomComponent;

/**
 * A silly simple component to show a horizontal rule
 */
public class ERDSpacer extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDSpacer(WOContext context) { super(context); }

    @Override
    public final boolean isStateless() { return true; }
    @Override
    public final boolean synchronizesVariablesWithBindings() { return false; }
}
