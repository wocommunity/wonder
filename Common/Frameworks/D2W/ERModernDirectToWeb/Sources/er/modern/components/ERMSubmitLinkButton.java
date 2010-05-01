package er.modern.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Wrapper around ERMSubmitLink to more closely resemble a submit button
 * 
 * @author davidleber
 *
 */
public class ERMSubmitLinkButton extends ERXNonSynchronizingComponent {
	
    public ERMSubmitLinkButton(WOContext context) {
        super(context);
    }
    
}