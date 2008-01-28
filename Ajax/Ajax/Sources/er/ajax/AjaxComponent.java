package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

/**
 * This abstract (by design) superclass component isolate general utility methods.
 * 
 * @author Jean-Francois Veillette <jean.francois.veillette@gmail.com>
 * @version $Revision $, $Date $ <br>
 *          &copy; 2006 OS communications informatiques, inc. http://www.os.ca
 *          Tous droits réservés.
 */

public abstract class AjaxComponent extends WOComponent implements IAjaxElement {
    /** logging */
    protected Logger log = Logger.getLogger(getClass());

    public AjaxComponent(WOContext context) {
        super(context);
    }

    /**
     * Utility to get the value of a binding or a default value if none is
     * supplied.
     * @param name
     * @param defaultValue
     */
    public Object valueForBinding(String name, Object defaultValue) {
        Object value = defaultValue;
        if(hasBinding(name)) {
            value = valueForBinding(name);
        }
        return value;
    }
    
    protected void addScriptResourceInHead(WOResponse _response, String _fileName) {
	AjaxUtils.addScriptResourceInHead(context(), _response, _fileName);
    }

    protected void addScriptResourceInHead(WOResponse _response, String _framework, String _fileName) {
	AjaxUtils.addScriptResourceInHead(context(), _response, _framework, _fileName);
    }
    
    protected void addStylesheetResourceInHead(WOResponse _response, String _fileName) {
      AjaxUtils.addStylesheetResourceInHead(context(), _response, _fileName);
    }
    
    protected void addStylesheetResourceInHead(WOResponse _response, String _framework, String _fileName) {
	AjaxUtils.addStylesheetResourceInHead(context(), _response, _framework, _fileName);
    }

    /**
     * Execute the request, if it's comming from our action, then invoke the
     * ajax handler and put the key <code>AJAX_REQUEST_KEY</code> in the
     * request userInfo dictionary (<code>request.userInfo()</code>).
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {
        Object result;
        if (AjaxUtils.shouldHandleRequest(request, context, _containerID(context))) {
            result = handleRequest(request, context);
            AjaxUtils.updateMutableUserInfoWithAjaxInfo(context());
			if (result == null) {
				result = AjaxUtils.createResponse(request, context);
			}
        } else {
            result = super.invokeAction(request, context);
        }
        return (WOActionResults) result;
    }

    protected String _containerID(WOContext context) {
    	return null;
    }

    public String safeElementID() {
    	String id = (String)valueForBinding("id");
    	if(id == null) {
    		return AjaxUtils.toSafeElementID(context().elementID());
    	}
    	return id;
    }

    /**
     * Overridden to call {@link #addRequiredWebResources(WOResponse)}.
     */
    public void appendToResponse(WOResponse res, WOContext ctx) {
        super.appendToResponse(res, ctx);
        addRequiredWebResources(res);
    }

    /**
     * Override this method to append the needed scripts for this component.
     * @param res
     */
    protected abstract void addRequiredWebResources(WOResponse res);
    
    /**
     * Override this method to return the response for an Ajax request.
     * @param request
     * @param context
     */
    public abstract WOActionResults handleRequest(WORequest request, WOContext context);

}
