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
 * <p>
 * The default value for the field comes from setting default = "something"; on the input element.  For instance,
 * to set the default value on a text field, you would do &lt;wo:WOTextField value = "$value" default = "Fill this in"/&gt;.
 * <p>
 * If you place this tag around your forms or input elements, all input elements and forms are automatically re-registered
 * after an Ajax-Refresh when placed within an AjaxUpdateContainer.
 * <p>
 * Example 1: Given form and all input elements are registered on load (old behaviour of AjaxTextHinter)
 * <pre>
 * &lt;wo:Form id="myform" ...&gt;
 *   &lt;wo:WOTextField default="Login name"/&gt;
 * &lt;/wo:Form&gt;
 * &lt;wo:AjaxTextHinter form="myform"/&gt;
 * </pre>
 * Example 2: Form is within AjaxUpdateContainer and has to be re-registered after Ajax refresh.
 *            So you can nest multiple AjaxTextHinter tags, if necessary
 * <pre>
 * &lt;wo:AjaxUpdateContainer&gt;
 *   &lt;wo:AjaxTextHinter&gt;
 *     &lt;wo:Form id="myform" ...&gt;
 *       &lt;wo:WOTextField default="Login name"/&gt;
 *     &lt;/wo:Form&gt;
 *   &lt;/wo:AjaxTextHinter/&gt;
 * &lt;/wo:AjaxUpdateContainer&gt;
 * </pre>
 * Example 3: only some input elements are within AjaxUpdateContainer
 * <pre>
 * &lt;wo:AjaxTextHinter&gt;
 *   &lt;wo:Form id="myform" ...&gt;
 *     &lt;wo:AjaxUpdateContainer&gt;
 *       &lt;wo:AjaxTextHinter&gt;
 *         &lt;wo:WOTextField default="Login name"/&gt;
 *      &lt;/wo:AjaxTextHinter/&gt;
 *     &lt;/wo:AjaxUpdateContainer&gt;
 *   &lt;/wo:Form&gt;
 * &lt;/wo:AjaxTextHinter/&gt;
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
