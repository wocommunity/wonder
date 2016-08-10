package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Ajax enabled Hyperlink. Calls an action on the server, then executes methods on the client.
 * 
 * @binding elementName the HTML element name
 * @binding onSuccess JS function, called on a 2xx response on the client
 * @binding onFailure JS function, called on a non-200 response on the client
 * @binding onComplete JS function, called on completion
 * @binding onLoading the js function to call when loading
 * @binding evalScripts whether or not to eval scripts on the result
 * @binding onClickBefore if the given function returns true, the onClick is executed. This is to support confirm(..)
 *          dialogs.
 * @binding onClick JS function, called after the click on the client
 * @binding onClickServer JS returned from the server after the update
 * @binding action method to call
 * @binding title title of the link
 * @binding string optional string for link text for when there is no element content
 * @binding id id of the link
 * @binding class class of the link
 * @binding style style of the link
 * @binding disabled whether or not this link is disabled
 * @binding functionName if set, the link becomes a javascript function
 * @author ak
 */
public class AjaxHyperlink extends AjaxDynamicElement {

	public AjaxHyperlink(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
	}

	public String onClick(WOContext context) {
		StringBuffer sb = new StringBuffer();

		String onClickBefore = (String) valueForBinding("onClickBefore", context.component());
		if (onClickBefore != null) {
			sb.append("if (");
			sb.append(onClickBefore);
			sb.append(") {");
		}
		NSDictionary options = createAjaxOptions(context.component());
		String actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
		// PROTOTYPE FUNCTIONS
		sb.append("new Ajax.Request('");
		sb.append(actionUrl);
		sb.append("', ");
		AjaxOptions.appendToBuffer(options, sb, context);
		sb.append(')');
		String onClick = (String) valueForBinding("onClick", context.component());
		if (onClick != null) {
			sb.append(';');
			sb.append(onClick);
		}
		if (onClickBefore != null) {
			sb.append('}');
		}
		return sb.toString();
	}

	protected NSDictionary createAjaxOptions(WOComponent component) {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		// PROTOTYPE OPTIONS
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onLoading", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.setObjectForKey("'get'", "method");
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();

		boolean disabled = booleanValueForBinding("disabled", false, component);
		String elementName = (String) valueForBinding("elementName", "a", component);
		String functionName = (String) valueForBinding("functionName", null, component);
		boolean isATag = "a".equalsIgnoreCase(elementName);
		boolean renderTags = ((!disabled || !isATag) && functionName == null);
		if (renderTags) {
			response.appendContentString("<");
			response.appendContentString(elementName);
			response.appendContentString(" ");
			if (isATag) {
				appendTagAttributeToResponse(response, "href", "javascript:void(0)");
			}
			appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
			appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
			appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
			appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
			appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
			if (!disabled) {
				appendTagAttributeToResponse(response, "onclick", onClick(context));
			}
			response.appendContentString(">");
		}
		if (functionName != null) {
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString(functionName + " = function() { " + onClick(context) + " }\n");
			AjaxUtils.appendScriptFooter(response);
		}
		if ( ! hasBinding("string")) {
			appendChildrenToResponse(response, context);
		} else {
			response.appendContentString((String)valueForBinding("string", component));
		}
		
		if (renderTags) {
			response.appendContentString("</");
			response.appendContentString(elementName);
			response.appendContentString(">");
		}
		super.appendToResponse(response, context);
	}

	@Override
	protected void addRequiredWebResources(WOResponse res, WOContext context) {
		addScriptResourceInHead(context, res, "prototype.js");
		addScriptResourceInHead(context, res, "effects.js");
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		WOActionResults results = (WOActionResults) valueForBinding("action", component);
		if (results == null) {
			String script = (String) valueForBinding("onClickServer", component);
			if (script != null) {
				WOResponse response = AjaxUtils.createResponse(request, context);
				AjaxUtils.appendScriptHeader(response);
				response.appendContentString(script);
				AjaxUtils.appendScriptFooter(response);
				results = response;
			}
		}
		return results;
	}
}