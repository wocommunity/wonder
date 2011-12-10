package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ajax.ERXAjaxApplication;

public abstract class AjaxDynamicElement extends WODynamicGroup implements IAjaxElement {
	protected Logger log = Logger.getLogger(getClass());
	private NSDictionary<String, WOAssociation> _associations;

	public AjaxDynamicElement(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
		_associations = associations;
	}

	public NSDictionary<String, WOAssociation> associations() {
		return _associations;
	}
	
	public boolean hasBinding(String name) {
		return AjaxUtils.hasBinding(name, associations());
	}
	
	public WOAssociation bindingNamed(String name) {
		return associations().objectForKey(name);
	}

	public Object valueForBinding(String name, Object defaultValue, WOComponent component) {
		return AjaxUtils.valueForBinding(name, defaultValue, associations(), component);
	}

	public Object valueForBinding(String name, WOComponent component) {
		return AjaxUtils.valueForBinding(name, associations(), component);
	}

	public String stringValueForBinding(String name, WOComponent component) {
		return AjaxUtils.stringValueForBinding(name, associations(), component);
	}
	
	public String stringValueForBinding(String name, String defaultValue, WOComponent component) {
		return AjaxUtils.stringValueForBinding(name, defaultValue, associations(), component);
	}
	
	public boolean booleanValueForBinding(String name, boolean defaultValue, WOComponent component) {
		return AjaxUtils.booleanValueForBinding(name, defaultValue, associations(), component);
	}

	public void setValueForBinding(Object value, String name, WOComponent component) {
		AjaxUtils.setValueForBinding(value, name, associations(), component);
	}

	protected void addScriptResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		AjaxUtils.addScriptResourceInHead(context, response, framework, fileName);
	}

	protected void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
		AjaxUtils.addScriptResourceInHead(context, response, fileName);
	}

	protected void addStylesheetResourceInHead(WOContext context, WOResponse response, String fileName) {
		AjaxUtils.addStylesheetResourceInHead(context, response, fileName);
	}

	protected void addStylesheetResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		AjaxUtils.addStylesheetResourceInHead(context, response, framework, fileName);
	}
  
	/**
	 * Execute the request, if it's coming from our action, then invoke the ajax handler and put the key
	 * <code>AJAX_REQUEST_KEY</code> in the request userInfo dictionary (<code>request.userInfo()</code>).
	 * 
	 * @param request the current request
	 * @param context the current context
	 * @return the action results
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		if (shouldHandleRequest(request, context)) {
			result = handleRequest(request, context);
			AjaxUtils.updateMutableUserInfoWithAjaxInfo(context);
        	if (ERXAjaxApplication.shouldIgnoreResults(request, context, result)) {
        		log.warn("An Ajax request attempted to return the page, which is almost certainly an error.");
        		result = null;
        	}
			if (result == null && !ERXAjaxApplication.isAjaxReplacement(request)) {
				result = AjaxUtils.createResponse(request, context);
			}
		}
		else if (hasChildrenElements()) {
			result = super.invokeAction(request, context);
		}
		return result;
	}

	protected String _containerID(WOContext context) {
		return null;
	}
	
    protected boolean shouldHandleRequest(WORequest request, WOContext context) {
    	return AjaxUtils.shouldHandleRequest(request, context, _containerID(context));
	}

	/**
	 * Overridden to call {@link #addRequiredWebResources(WOResponse, WOContext)}.
	 * 
	 * @param response the current response
	 * @param context the current context
	 */
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
		addRequiredWebResources(response, context);
	}

	public void appendTagAttributeToResponse(WOResponse response, String name, Object object) {
		if (object != null) {
			response._appendTagAttributeAndValue(name, object.toString(), true);
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		takeChildrenValuesFromRequest(request, context);
	}

	/**
	 * Override this method to append the needed scripts for this component.
	 * 
	 * @param request the current request
	 * @param context the current context
	 */
	protected abstract void addRequiredWebResources(WOResponse response, WOContext context);

	/**
	 * Override this method to return the response for an Ajax request.
	 * 
	 * @param request the current request
	 * @param context the current context
	 */
	public abstract WOActionResults handleRequest(WORequest request, WOContext context);

}
