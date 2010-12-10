package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * Adds a script tag with a Behaviour.register() with the content as the method argument.
 * Mostly useful because it spares you the hassle of including the script.
 * @binding includeScriptTag boolean also write out script tag
 * @author ak
 */
public class AjaxBehaviour extends AjaxDynamicElement {

	public AjaxBehaviour(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
	}

	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "behaviour.js");
	}
	
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		boolean includeScriptTag = booleanValueForBinding("includeScriptTag", true, context.component());
		if(includeScriptTag) {
			AjaxUtils.appendScriptHeader(response);
		}
		response.appendContentString("Behaviour.register(");
		appendChildrenToResponse(response, context);
		response.appendContentString(");");
		if(includeScriptTag) {
			AjaxUtils.appendScriptFooter(response);
		}
	}

	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

}
