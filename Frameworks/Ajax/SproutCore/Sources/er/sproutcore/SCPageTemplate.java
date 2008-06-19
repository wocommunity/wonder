package er.sproutcore;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Template for a page.
 * @author ak
 *
 */
public class SCPageTemplate extends ERXNonSynchronizingComponent {
    
    public SCPageTemplate(WOContext context) {
        super(context);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
    }
}