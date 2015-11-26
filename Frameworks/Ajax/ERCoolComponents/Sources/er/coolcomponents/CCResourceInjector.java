package er.coolcomponents;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXStatelessComponent;

/**
 * Stupid little component to allow you to inject a resource at any point on a page.
 * Useful for instance, if you need to guarantee that your css will override the dynamically 
 * injected css from some other component.
 * 
 * @author davidleber
 *
 */
public class CCResourceInjector extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public CCResourceInjector(WOContext context) {
        super(context);
    }
    
    /**
     * Adds a link to the resource specified by filename and framework bindings to the header or includes it in an Ajax friendly manner.
     * 
     * Assumes that if the isScript binding is null or false resource is a css file.
     *
     * @see er.extensions.components.ERXNonSynchronizingComponent#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
     * @see er.extensions.appserver.ERXResponseRewriter#addScriptResourceInHead(WOResponse, WOContext, String, String)
     */
    @Override
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