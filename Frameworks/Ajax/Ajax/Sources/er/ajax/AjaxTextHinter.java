package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * Hints input and textarea fields with ghosted text that serves as an explanation for the user what to enter.
 * The style "ajax-hinted-text-with-default" defines the color for this.
 * 
 * The default value for the field comes from setting default = "something"; on the input element.  For instance,
 * to set the default value on a text field, you would do <wo:WOTextField value = "$value" default = "Fill this in"/>.
 * <pre>
 * HTML:
 * &lt;webobject name="Form"&gt;
 *  &lt;webobject name="SomeText" /&gt;
 * &lt;/webobject&gt;
 * &lt;webobject name="TextHinter"/ &gt;
 * 
 * WOD:
 * TextHinter : AjaxTextHinter {
 *         form = "EditForm";
 * }
 * Form : ERXWOForm {
 *         id = "EditForm";
 *         ....
 * }
 * SomeText: WOTextField {
 *         default = "Name oder Titel";
 *         ....
 * } </pre>
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
		addScriptResourceInHead(context, response, "behaviour.js");
		addScriptResourceInHead(context, response, "wonder.js");
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		String name = (String) valueForBinding("form", context.component());
		if(name != null) {
			name = "'" + name + "'";
		} else {
			name = "";
		}
		response.appendContentString("<script>AjaxHintedText.register(" + name + ")</script>");
	}

}