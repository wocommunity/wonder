package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXResponse;

/**
 * IFRAME that can use its own contents to render when none of the other
 * bindings are supplied. Makes page-writing a lot easier as you don't need an
 * extra page for the contents. Beware, this can confuse the backtrack cache a lot..
 * 
 * @author ak
 * @binding src absolute url to render from
 * @binding pageName name of the page to open
 * @binding action renders the action result as the content
 */
public class ERXIFrame extends WOHTMLDynamicElement {

	WOAssociation _src;
	WOAssociation _pageName;
	WOAssociation _action;

	public ERXIFrame(String name, NSDictionary<String, WOAssociation> associations, WOElement parent) {
		super("iframe", associations, parent);
		_src = associations.objectForKey("src");
		_pageName = associations.objectForKey("pageName");
		_action = associations.objectForKey("action");
	}

	@Override
	public String elementName() {
		return "iframe";
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOComponent component = context.component();
		if(context.senderID().startsWith(context.elementID())) {
			if(context.senderID().equals(context.elementID())) {
				if (_pageName != null) {
					String pageName = (String) _pageName.valueInComponent(component);
					return WOApplication.application().pageWithName(pageName, context);
				}
				else if (_action != null) {
					return (WOActionResults) _action.valueInComponent(component);
				} else {
					ERXResponse response = new ERXResponse();
					//AK: we might want to be able to set this...
					response.appendContentString("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
					response.appendContentString("<html><body style='border:0; margin: 0; padding: 0; width:100%; height: 100%'>");
					doAppendChildrenToResponse(response, context);
					response.appendContentString("</body></html>");
					ERXResponseComponent comp = (ERXResponseComponent) WOApplication.application().pageWithName("ERXResponseComponent", context);
					comp.setResponse(response);
					comp.setComponent(context.page());
					return comp;
				}
			}
			return invokeChildrenAction(request, context);
		}
		return null;
	}
	
	public void doAppendChildrenToResponse(WOResponse response, WOContext context) {
		super.appendChildrenToResponse(response, context);
	}
	
	@Override
	public void appendChildrenToResponse(WOResponse response, WOContext context) {
		// nothing to do
	}
	
	@Override
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
    	WOComponent component = context.component();
		String src = null;
		if (_src != null) {
			src = (String) _src.valueInComponent(component);
		}
		else {
			src = context.componentActionURL();
		}
		response.appendContentString(" src=\"");
    	response.appendContentHTMLAttributeValue(src);
    	response.appendContentString("\"");
    	super.appendAttributesToResponse(response, context);
    }
}
