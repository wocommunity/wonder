package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXWOContext;

/**
 * Hints input and textarea fields with ghosted text that serves as an explanation for the user what to enter.
 * The style "ajax-hinted-text-with-default" defines the color for this.
 * 
 * The default value for the field comes from setting default = "something"; on the input element.  For instance,
 * to set the default value on a text field, you would do &lt;wo:WOTextField value = "$value" default = "Fill this in"/&gt;.
 * 
 * If you place this tag around your forms or input elements, all input elements and forms are automatically re-registered
 * after an Ajax-Refresh when placed within an AjaxUpdateContainer.
 * 
 * <pre>
 * 
 * Example 1: Given form and all input elements are registered on load (old behaviour of AjaxTextHinter)
 * 
 * <wo:Form id="myform" ...>
 *   <wo:WOTextField default="Login name"/>
 * </wo:Form>
 * <wo:AjaxTextHinter form="myform"/>
 * 
 * Example 2: Form is within AjaxUpdateContainer and has to be re-registered after Ajax refresh.
 *            So you can nest multiple AjaxTextHinter tags, if neccessary
 * 
 * <wo:AjaxUpdateContainer>
 *   <wo:AjaxTextHinter>
 *     <wo:Form id="myform" ...>
 *       <wo:WOTextField default="Login name"/>
 *     </wo:Form>
 *   </wo:AjaxTextHinter/>
 * </wo:AjaxUpdateContainer>
 * 
 * Exmaple 3: only some input elements are within AjaxUpdateContainer
 * 
 * <wo:AjaxTextHinter>
 *   <wo:Form id="myform" ...>
 *     <wo:AjaxUpdateContainer>
 *       <wo:AjaxTextHinter>
 *         <wo:WOTextField default="Login name"/>
 *      </wo:AjaxTextHinter/>
 *     </wo:AjaxUpdateContainer>
 *   </wo:Form>
 * </wo:AjaxTextHinter/>
 * 
 * </pre>
 *
 * @binding form ID of the form to apply the hints to
 * @author ak
 */
public class AjaxTextHinter extends AjaxDynamicElement {

	public AjaxTextHinter(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		addScriptResourceInHead(context, response, "prototype.js");
		addScriptResourceInHead(context, response, "wonder.js");
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		String formId = (String) valueForBinding("form", context.component());

		String id = _containerID(context);

		if(hasChildrenElements() && formId == null)
		{
			response.appendContentString("<div id='" + id + "'>");
			appendChildrenToResponse(response, context);
			response.appendContentString("</div>");
			if(AjaxUtils.isAjaxRequest(context.request()))
				response.appendContentString("<script>AjaxHintedText.register('div', '" + id + "');</script>");
			else
				response.appendContentString("<script>Event.observe(window, 'load', function() {AjaxHintedText.register('div', '" + id + "');});</script>");        
		} else
		{
			String formSelector = (formId != null) ? "form#" + formId : "form";

			if(AjaxUtils.isAjaxRequest(context.request()))
				response.appendContentString("<script>AjaxHintedText.registerForm('" + formSelector + "');</script>");        
			else
				response.appendContentString("<script>Event.observe(window, 'load', function() {AjaxHintedText.registerForm('" + formSelector + "');});</script>");        
		}

	}

	protected String _containerID(WOContext context) {
		return ERXWOContext.safeIdentifierName(context, false);
	}

}
