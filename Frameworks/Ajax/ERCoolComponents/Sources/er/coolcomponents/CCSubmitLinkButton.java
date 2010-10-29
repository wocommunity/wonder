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
	
    public CCSubmitLinkButton(WOContext context) {
        super(context);
    }
    
}