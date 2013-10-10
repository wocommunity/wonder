package er.ajax;

//http://jquery.com/demo/thickbox/

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
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Shows a link and wraps an area that is later presented as a modal window. Alternately, when you bind <b>action</b> then the content is used as the link.
 * 
 * @binding label label for the link
 * @binding class class for the link
 * @binding style style for the link
 * @binding value value for the link (??)
 * @binding id id for the link
 * @binding containerID container ID for non-Ajax WOComponentContent
 * @binding closeLabel string for the close link
 * @binding title title string for the link label and the window
 * @binding href when it is bound, the content of the url will be fetched into an iframe.
 * @binding directActionName the direct action to fetch
 * @binding action when it is bound, the content of the url will be fetched into a div
 * @binding ajax (optional) when true, the contents are only rendered during the Ajax request, using ajax=true is the preferred way to use this
 * @binding open if true, the container is rendered already opened (currently only workings, i think, with ajax=true)
 * @binding locked if true, the container will be "locked" and will not close unless you explicitly close it
 * @binding secure (only applicable for directAtionName) if true, the generated url will be https
 * @binding skin the name of the skin to use (lightbox or darkbox right now)
 * 
 * If your content changes height and you want to autosize your iBox, you can add &lt;script&gt;iBox.contentChanged()&lt;/script&gt; into your
 * AjaxUpdateContainer to trigger an iBox resize.
 *  
 * @author timo
 * @author ak
 */
public class AjaxModalContainer extends AjaxDynamicElement {

    public AjaxModalContainer(String name, NSDictionary associations, WOElement children) {
        super(name, associations, children);
    }

    public boolean shouldHandle(WOContext context) {
    	return context.elementID().equals(context.senderID());
    }
    
    @Override
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
        WOComponent component = wocontext.component();
    	if (!booleanValueForBinding("ajax", false, component)) {
	        WOAssociation action = associations().objectForKey("action");
	        if(action != null && wocontext.elementID().equals(wocontext.senderID())) {
	            return (WOActionResults) action.valueInComponent(component);
	        }
    	}
        return super.invokeAction(worequest, wocontext);
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();
        String linkID = (String)valueForBinding("id", component);
        if (linkID == null) {
        	linkID=ERXWOContext.safeIdentifierName(context, false);
        }
        String containerID = (String)valueForBinding("containerID", linkID + "Container", component);
        response.appendContentString("<a");
        String href = (String) valueForBinding("href", component);
        if (href == null) {
        	String directActionName = stringValueForBinding("directActionName", component);
        	if (directActionName != null) {
        		NSDictionary queryDictionary = (NSDictionary)valueForBinding("queryDictionary", component);
        		boolean secure = booleanValueForBinding("secure", ERXRequest.isRequestSecure(context.request()), component);
        		if (secure) {
              boolean generatingCompleteURLs = context.doesGenerateCompleteURLs();
              if (!generatingCompleteURLs) {
        				context.generateCompleteURLs();
        			}
              try {
          			href = context._directActionURL(directActionName, queryDictionary, secure, 0, false);
          			ERXMutableURL u = new ERXMutableURL(href);
          			u.addQueryParameter(String.valueOf(System.currentTimeMillis()), null);
          			href = u.toExternalForm();
              }
              catch (MalformedURLException e) {
                throw new NSForwardException(e);
              }
              finally {
          			if (!generatingCompleteURLs) {
          				context.generateRelativeURLs();
          			}
              }
        		}
        		else {
              href = context.directActionURLForActionNamed(directActionName, queryDictionary);
        		}
        	}
        }
        boolean isAjax = booleanValueForBinding("ajax", false, component);
        if(href == null) {
			if (isAjax) {
            	if (valueForBinding("id", component) == null) {
    				throw new IllegalArgumentException("If ajax = 'true', you must also bind 'id'.");
            	}
            	href = AjaxUtils.ajaxComponentActionUrl(context);
            }
            else if(associations().objectForKey("action") != null) {
            	// don't use ajax request handler here
                href = context.componentActionURL();
            }
            if(href == null) {
                href = "#" + containerID;
            }
        }
        appendTagAttributeToResponse(response, "href", href);
		String relAttributeValue = "ibox";
		Object height = valueForBinding("height", component);
		Object width = valueForBinding("width", component);
		Object closeLabel = valueForBinding("closeLabel", component);
		if (height != null) {
			relAttributeValue += "&height=" +  ERXStringUtilities.urlEncode(height.toString());
		}
		if (width != null) {
			relAttributeValue += "&width=" +  ERXStringUtilities.urlEncode(width.toString());
		}
		if (closeLabel != null) {
			relAttributeValue += "&closeLabel=" + ERXStringUtilities.urlEncode(closeLabel.toString());
		}
		if (booleanValueForBinding("locked", false, component)) {
			relAttributeValue += "&locked=true";
		}
		response._appendTagAttributeAndValue("rel", relAttributeValue, false); // don't escape the ampersands
        appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
        appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
        appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
        appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
        appendTagAttributeToResponse(response, "id", linkID);
        response.appendContentString(">");
        if(!href.startsWith("#") && !isAjax && childrenElements() != null && childrenElements().count() > 0) {
        	appendChildrenToResponse(response, context);
        } else {
            Object label = valueForBinding("label", "", component);
            response.appendContentString(label.toString());
        }
        response.appendContentString("</a>");
        if (AjaxUtils.isAjaxRequest(context.request())) {
	        NSMutableDictionary userInfo = ERXWOContext.contextDictionary();
	        if (!userInfo.containsKey("er.ajax.AjaxModalContainer.init")) {
	            AjaxUtils.appendScriptHeader(response);
	            response.appendContentString("iBox.init()");
	            AjaxUtils.appendScriptFooter(response);
	            userInfo.setObjectForKey(Boolean.TRUE, "er.ajax.AjaxModalContainer.init");
	        }
        }
        if (booleanValueForBinding("open", false, component)) {
        	if (AjaxUtils.isAjaxRequest(context.request())) {
        		// PROTOTYPE FUNCTIONS
        		response.appendContentString("<script>iBox.handleTag.bind($wi('" + linkID + "'))()</script>");
        	}
        	else {
        		// PROTOTYPE FUNCTIONS
        		response.appendContentString("<script>Event.observe(window, 'load', iBox.handleTag.bind($wi('" + linkID + "')))</script>");
        	}
        }
        if(href.startsWith("#")) {
        	response.appendContentString("<div");

        	appendTagAttributeToResponse(response, "id", containerID);
        	appendTagAttributeToResponse(response, "style", "display:none;");
        	response.appendContentString(">");
        	appendChildrenToResponse(response, context);
        	response.appendContentString("</div>");
        }
        super.appendToResponse(response, context);
    }

    @Override
    protected void addRequiredWebResources(WOResponse response, WOContext context) {
    	addScriptResourceInHead(context, response, "prototype.js");
    	addScriptResourceInHead(context, response, "ibox/ibox.js");
    	String skinName = stringValueForBinding("skin", context.component());
    	String skinCSS;
    	if (skinName == null) {
    	  skinCSS = "ibox/ibox.css";
    	}
    	else {
    	  skinCSS = "ibox/skins/" + skinName + "/" + skinName + ".css";
    	}
    	addStylesheetResourceInHead(context, response, skinCSS);
    }

	@Override
	protected String _containerID(WOContext context) {
		String id = (String) valueForBinding("id", context.component());
		return id;
	}

    @Override
    public WOActionResults handleRequest(WORequest request, WOContext context) {
        WOComponent component = context.component();

        WOResponse response = null;
        WOAssociation action = associations().objectForKey("action");
        if(action != null) {
            action.valueInComponent(component);
        }

    	if (booleanValueForBinding("ajax", false, component) && hasChildrenElements()) {
			response = AjaxUtils.createResponse(request, context);
			AjaxUtils.setPageReplacementCacheKey(context, _containerID(context));
			appendChildrenToResponse(response, context);
    	}
        return response;
    }
}
