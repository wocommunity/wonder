package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxUtils;
import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;

public class MTMediaBox extends AjaxDynamicElement {
	
	public MTMediaBox(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
    }

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		
		WOComponent component = context.component();
		response.appendContentString("<a ");
		String href = null;
		ERAttachment attachment = (ERAttachment)valueForBinding("attachment", component);

		if(valueForBinding("href", component) != null) {
			href = (String)valueForBinding("href", component);
		} else if(attachment != null) {
			href = ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context.request(), context);
		} else {
			WOResourceManager rm = WOApplication.application().resourceManager();
			String fileName = (String)valueForBinding("filename", component);
			String frameWork = (String)valueForBinding("framework", component);
			href = rm.urlForResourceNamed(fileName, frameWork, null, context.request()).toString();
		}
		
		appendTagAttributeToResponse(response, "href", href);
		appendTagAttributeToResponse(response, "rel", valueForBinding("rel", component));
		appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
		appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
		appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
		response.appendContentString(">");
		appendChildrenToResponse(response, context);
		response.appendContentString("</a>");
		super.appendToResponse(response, context);
		
		
	}
	
	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
	
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/mediabox/mediaboxAdv-1.2.5.js");
		
		String theme = context.component().valueForStringBinding("theme", "dark");
		
		if(theme.equals("dark")) {
			AjaxUtils.addStylesheetResourceInHead(context, response, "MooTools", "scripts/plugins/mediabox/mediaboxAdvBlack.css");
		} else {
			AjaxUtils.addStylesheetResourceInHead(context, response, "MooTools", "scripts/plugins/mediabox/mediaboxAdvWhite.css");
		}

	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}