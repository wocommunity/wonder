package er.ajax.mootools;

import java.net.MalformedURLException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUpdateContainer;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXMutableURL;

/*
 *  * @binding useDefaultCSS (boolean) defaults to true.  Will load a default CSS file for the modal container.
 */

public class MTAjaxModalContainer extends AjaxDynamicElement {
	
	private String _updateContainerID = null;
	private String _url = null;

	public MTAjaxModalContainer(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
        super(name, associations, children);
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {

		WOComponent component = context.component();
		
		String linkID = (String) valueForBinding("id", component);
		if(linkID == null) {
			linkID = ERXWOContext.safeIdentifierName(context, false);
		}
		String containerID = (String) valueForBinding("containerID", linkID + "Container", component);
		
		String linkElementName = (String)valueForBinding("linkElementName", "a", component);
		String label = (String)valueForBinding("label", "Open", component);

		_url = (String) valueForBinding("href", component);
		if(_url == null) {
			String directActionName = stringValueForBinding("directActionName", component);

			if(directActionName != null) {
				NSDictionary queryDictionary = (NSDictionary)valueForBinding("queryDictionary", component);
				boolean secure = booleanValueForBinding("secure", ERXRequest.isRequestSecure(context.request()), component);
				if(secure) {
					boolean generatingCompleteURLs = context.doesGenerateCompleteURLs();
					if(!generatingCompleteURLs) {
						context.generateCompleteURLs();
					}
					try {
						_url = context._directActionURL(directActionName, queryDictionary, secure, 0, false);
						ERXMutableURL u = new ERXMutableURL(_url);
						u.addQueryParameter(String.valueOf(System.currentTimeMillis()), null);
						_url = u.toExternalForm();
					} catch (MalformedURLException e) {
						throw new NSForwardException(e);
					} finally {
						if(!generatingCompleteURLs) {
							context.generateRelativeURLs();
						}
					}
				} else {
					_url = context.directActionURLForActionNamed(directActionName, queryDictionary);
				}
			}
		}
		
		boolean isAjax = booleanValueForBinding("ajax", false, component);
    	if(_url == null) {
    		if(isAjax) {
				if(valueForBinding("id", component) == null) {
					throw new IllegalArgumentException("If ajax = 'true', you must also bind 'id'");
				}
				_url = AjaxUtils.ajaxComponentActionUrl(context);
			} else if (associations().objectForKey("action") != null) {
				_url = context.componentActionURL();
			} 
			if(_url == null) {
				_url = "#" + containerID;
			}
    	}
		
		response.appendContentString("<");
		response.appendContentString(linkElementName);
        appendTagAttributeToResponse(response, "id", linkID);
        appendTagAttributeToResponse(response, "href", _url);
        appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
        appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
        appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
        appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
		response.appendContentString(">");
		response.appendContentString(label);
		response.appendContentString("</");
		response.appendContentString(linkElementName);
		response.appendContentString(">");

		Boolean autoWrapContent = (Boolean)valueForBinding("autoWrapContent", Boolean.TRUE, component);
		Boolean showFooter = (Boolean)valueForBinding("showFooter", Boolean.TRUE, component);
		Boolean showTitle = (Boolean)valueForBinding("showTitle", Boolean.TRUE, component);
		String modalClassNames = (String)valueForBinding("modalClassNames", "modal fade", component);
		_updateContainerID = null;
		if(_url.startsWith("#")) {
			
			response.appendContentString("\n<div class=\"");
			response.appendContentString(modalClassNames);
			response.appendContentString("\" ");
	        appendTagAttributeToResponse(response, "id", containerID);
	        response.appendContentString(">");
	    	
	        String title = stringValueForBinding("title", component);
	        if(showTitle.booleanValue() && title != null) {
		        response.appendContentString("\n\t<div class=\"modal-header\">");
		        response.appendContentString("\n\t\t<a class=\"close\" data-dismiss=\"modal\">x</a>");
		        response.appendContentString("\n\t\t<h3>");
		        response.appendContentString(title);
		        response.appendContentString("</h3>");
		        response.appendContentString("\n\t</div>");
	        }

	        if(autoWrapContent.booleanValue()) {
		        response.appendContentString("\n\t<div class=\"modal-body\">");
	        }
	        
	        appendChildrenToResponse(response, context);

	        if(autoWrapContent.booleanValue()) {
		        response.appendContentString("\n\t</div>");
	        }
	        
	        if(showFooter.booleanValue()) {
		        response.appendContentString("\n\t<div class=\"modal-footer\">");
		        response.appendContentString("\n\t\t<a href=\"#\" class=\"dismiss btn\">Close</a>");	
		        response.appendContentString("\n\t</div>");
	        }
	    
	        response.appendContentString("</div>");
		
		} else {
			_updateContainerID = containerID;
			response.appendContentString("\n<div class=\"");
			response.appendContentString(modalClassNames);
			response.appendContentString("\" ");
	        appendTagAttributeToResponse(response, "id", containerID);
	        response.appendContentString(">");

	        String title = stringValueForBinding("title", component);
	        if(showTitle.booleanValue() && title != null) {
		        response.appendContentString("\n\t<div class=\"modal-header\">");
		        response.appendContentString("\n\t\t<a class=\"close\" data-dismiss=\"modal\">x</a>");
		        response.appendContentString("\n\t\t<h3>");
		        response.appendContentString(title);
		        response.appendContentString("</h3>");
		        response.appendContentString("\n\t</div>");
	        }

	        if(autoWrapContent.booleanValue()) {
	        	String modalBodyID = stringValueForBinding("modalBodyID", component);
	        	if(modalBodyID == null) {
	        		modalBodyID = "modalBody" + ERXWOContext.safeIdentifierName(context, false);
	        	}
	        	_updateContainerID = modalBodyID;
	        	response.appendContentString("\n\t<div class=\"modal-body\"");
	        	appendTagAttributeToResponse(response, "id", modalBodyID);
	        	response.appendContentString(">");
	        	
	        }

	        if(autoWrapContent.booleanValue()) {
		        response.appendContentString("\n\t</div>");
	        }
	        
	        if(showFooter.booleanValue()) {
		        response.appendContentString("\n\t<div class=\"modal-footer\">");
		        response.appendContentString("\n\t\t<a href=\"#\" class=\"dismiss btn\">Close</a>");	
		        response.appendContentString("\n\t</div>");
	        }
	    
	        response.appendContentString("</div>");
	        
	        
		}
		
		String varName = stringValueForBinding("varName", component);
		if(varName == null) {
			varName = "bsPopUp" + ERXWOContext.safeIdentifierName(context, false);
		}
		
		AjaxUtils.appendScriptHeader(response);
		response.appendContentString("\nwindow.addEvent('domready', function() {");
		response.appendContentString("\n\tvar ");
		response.appendContentString(varName);
		response.appendContentString(" = new Bootstrap.Popup('");
		response.appendContentString(containerID);
		response.appendContentString("', {");
		if(_updateContainerID != null) {
			response.appendContentString("onShow: function() { ");
			response.appendContentString("\n\tnew Request.HTML(");
			AjaxOptions.appendToResponse(createAjaxOptions(component), response, context);
			response.appendContentString(").send();");
			response.appendContentString("}");
		}
		response.appendContentString("});");
		response.appendContentString("\n\tdocument.id('");
		response.appendContentString(linkID);
		response.appendContentString("').addEvent('click', function(e) { e.preventDefault(); this.show(); }.bind(");
		response.appendContentString(varName);
		response.appendContentString("));");
		response.appendContentString("\n})");
		AjaxUtils.appendScriptFooter(response);		
    	super.appendToResponse(response, context);
    }
	
	@Override
	protected String _containerID(WOContext context) {
		String id = (String) valueForBinding("id", context.component());
		return id;
	}
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public NSDictionary createAjaxOptions(WOComponent component) {

		NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();
		ajaxOptionsArray.addObject(new AjaxOption("async", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("onRequest", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("useSpinner", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerTarget", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerOptions", AjaxOption.DICTIONARY));
		
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.setObjectForKey("'" + _url + "'", "url");
		options.setObjectForKey("'" + _updateContainerID + "'", "update");
		options.setObjectForKey("'get'", "method");

		if (options.objectForKey("async") == null) {
			options.setObjectForKey("true", "async");
		}		
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}
		
		AjaxUpdateContainer.expandInsertionFromOptions(options, this, component);
		return options;

	}

	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/bootstrap/ui/Bootstrap.js");
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/bootstrap/behaviors/Behavior.js");
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/bootstrap/behaviors/Behavior.BS.Popup.js");
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/bootstrap/ui/Bootstrap.Popup.js");
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/bootstrap/ui/CSSEvents.js");
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);

		Boolean useDefaultCSS = (Boolean)valueForBinding("useDefaultCSS", Boolean.TRUE, context.component());
		if(useDefaultCSS.booleanValue()) {
			AjaxUtils.addStylesheetResourceInHead(context, context.response(), "MooTools", "scripts/plugins/bootstrap/modal/modal.css");
		}

	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {

		WOComponent component = context.component();
		
		WOResponse response = null;
		WOAssociation action = associations().objectForKey("action");
		if(action != null) {
			action.valueInComponent(component);
		}
		
		if(booleanValueForBinding("ajax", false, component) && hasChildrenElements()) {
			response = AjaxUtils.createResponse(request, context);
			AjaxUtils.setPageReplacementCacheKey(context, _containerID(context));
			appendChildrenToResponse(response, context);
		}
		
		return response;
		
	}




}