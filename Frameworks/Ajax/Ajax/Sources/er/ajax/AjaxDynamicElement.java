package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXDynamicElement;

/**
 * AjaxDynamicElement provides a common base class for dynamic Ajax elements.
 */
public abstract class AjaxDynamicElement extends ERXDynamicElement implements IAjaxElement {

	public AjaxDynamicElement(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
	}
	
	public AjaxDynamicElement(String name, NSDictionary<String, WOAssociation> associations, NSMutableArray<WOElement> children) {
		super(name, associations, children);
	}
	
	/**
	 * Execute the request, if it's coming from our action, then invoke the ajax handler and put the key
	 * <code>AJAX_REQUEST_KEY</code> in the request userInfo dictionary (<code>request.userInfo()</code>).
	 * 
	 * @param request the current request
	 * @param context context of the transaction
	 * @return the action results
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		if (shouldHandleRequest(request, context)) {
			result = handleRequest(request, context);
			ERXAjaxApplication.enableShouldNotStorePage();
        	if (ERXAjaxApplication.shouldIgnoreResults(request, context, result)) {
        		log.warn("An Ajax request attempted to return the page, which is almost certainly an error.");
        		result = null;
        	}
			if (result == null && !ERXAjaxApplication.isAjaxReplacement(request)) {
				result = AjaxUtils.createResponse(request, context);
			}
		} else if (hasChildrenElements()) {
			result = super.invokeAction(request, context);
		}
		return result;
	}
	
	/**
	 * Override this method and return an update container ID this element should react on.
	 * 
	 * @param context context of the transaction
	 * @return <code>null</code>
	 */
	protected String _containerID(WOContext context) {
		return null;
	}
	
	/**
	 * Checks if the current request should be handled by this element.
	 * 
	 * @param request the current request
	 * @param context context of the transaction
	 * @return <code>true</code> if we should handle the request
	 */
	protected boolean shouldHandleRequest(WORequest request, WOContext context) {
    	return AjaxUtils.shouldHandleRequest(request, context, _containerID(context));
	}
	
	/**
	 * Overridden to call {@link #addRequiredWebResources(WOResponse, WOContext)}.
	 * 
	 * @param response the current response
	 * @param context context of the transaction
	 */
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
		addRequiredWebResources(response, context);
	}
    
    /**
	 * Override this method to append the needed scripts for this component.
	 * 
	 * @param response the current response
	 * @param context context of the transaction
	 */
	protected abstract void addRequiredWebResources(WOResponse response, WOContext context);
	
	/**
	 * Override this method to return the response for an Ajax request.
	 * 
	 * @param request the current request
	 * @param context context of the transaction
	 * @return the action results
	 */
	public abstract WOActionResults handleRequest(WORequest request, WOContext context);

	/**
	 * Adds a script link tag with a correct resource URL in the HTML head tag if it isn't
	 * already present in the response.
	 * 
	 * @param context context of the transaction
	 * @param response the current response
	 * @param framework name of the framework that contains the file
	 * @param fileName script file name
	 */
	protected void addScriptResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		AjaxUtils.addScriptResourceInHead(context, response, framework, fileName);
	}

	/**
	 * Adds a script link tag with a correct resource URL in the HTML head tag if it isn't
	 * already present in the response. The script file will be searched within the
	 * Ajax framework.
	 * 
	 * @param context context of the transaction
	 * @param response the current response
	 * @param fileName script file name
	 */
	protected void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
		AjaxUtils.addScriptResourceInHead(context, response, fileName);
	}

	/**
	 * Adds a stylesheet link tag with a correct resource URL in the HTML head
	 * tag if it isn't already present in the response.
	 * 
	 * @param context context of the transaction
	 * @param response the current response
	 * @param framework name of the framework that contains the file
	 * @param fileName CSS file name
	 */
	protected void addStylesheetResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		AjaxUtils.addStylesheetResourceInHead(context, response, framework, fileName);
	}

	/**
	 * Adds a stylesheet link tag with a correct resource URL in the HTML head
	 * tag if it isn't already present in the response. The CSS file will be searched
	 * within the Ajax framework.
	 * 
	 * @param context context of the transaction
	 * @param response the current response
	 * @param fileName CSS file name
	 */
	protected void addStylesheetResourceInHead(WOContext context, WOResponse response, String fileName) {
		AjaxUtils.addStylesheetResourceInHead(context, response, fileName);
	}
}
