package er.modern.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXStatelessComponent;

/**
 * Stupid little component to allow you to inject a resource at any point on a page.
 * Useful for instance, if you need to guaruntee that your css will override the dynamically 
 * injected css from some other component.
 * 
 * @author davidleber
 *
 */
public class ERMResourceInjector extends ERXStatelessComponent {
	
    public ERMResourceInjector(WOContext context) {
        super(context);
    }
    
    /**
     * Adds a link to the resource specified by filename and framework bindings to the header or includes it in an Ajax friendly manner.
     * 
     * Assumes that if the isScript binding is null or false resource is a css file.
     *
     * @see er.extensions.components.ERXNonSynchronizingComponent#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
     * @see ERXResponseRewriter#addScriptResourceInHead(WOResponse, WOContext, String, String)
     */
    public void appendToResponse(WOResponse response, WOContext context)
    {
    	String framework = stringValueForBinding("framework", "app");
    	String filename = stringValueForBinding("filename");
    	if (booleanValueForBinding("isScript", false)) {
    		ERXResponseRewriter.addScriptResourceInHead(response, context, framework, filename);
    	} else {
    		String media = stringValueForBinding("media");
    		ERXResponseRewriter.addStylesheetResourceInHead(response, context, framework, filename, media);
    	}
        super.appendToResponse(response, context);
    }
}