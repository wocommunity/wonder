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

public abstract class AjaxComponent extends WOComponent {
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
     * @return
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

    /**
     * Execute the request, if it's comming from our action, then invoke the
     * ajax handler and put the key <code>AJAX_REQUEST_KEY</code> in the
     * request userInfo dictionary (<code>request.userInfo()</code>).
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {
        Object result;
        if (AjaxUtils.shouldHandleRequest(request, context)) {
            result = handleRequest(request, context);
            AjaxUtils.updateMutableUserInfoWithAjaxInfo(context());
        } else {
            result = super.invokeAction(request, context);
        }
        return (WOActionResults) result;
    }
    
    public String safeElementID() {
      return AjaxUtils.toSafeElementID(context().elementID());
    }

    /**
     * Overridden to call {@see #addRequiredWebResources(WOResponse)}.
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
     * @return
     */
    protected abstract WOActionResults handleRequest(WORequest request, WOContext context);

}
