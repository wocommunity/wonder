package er.coolcomponents;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Wrapper around CCSubmitLink to more closely resemble a submit button
 * 
 * @author davidleber
 *
 */
public class CCSubmitLinkButton extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public CCSubmitLinkButton(WOContext context) {
        super(context);
    }
    
}