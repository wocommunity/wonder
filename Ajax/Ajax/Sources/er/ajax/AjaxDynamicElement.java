package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public abstract class AjaxDynamicElement extends WODynamicElement {
  protected Logger log = Logger.getLogger(getClass());
  private WOElement _children;

  public AjaxDynamicElement(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
    _children = children;
  }

  protected void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
    AjaxUtils.addScriptResourceInHead(context, response, fileName);
  }

  /**
   * Execute the request, if it's comming from our action, then invoke the
   * ajax handler and put the key <code>AJAX_REQUEST_KEY</code> in the
   * request userInfo dictionary (<code>request.userInfo()</code>).
   */
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    Object result = null;
    if (AjaxUtils.shouldHandleRequest(request, context)) {
      result = handleRequest(request, context);
      AjaxUtils.updateMutableUserInfoWithAjaxInfo(context);
    }
    else if (_children != null) {
      result = _children.invokeAction(request, context);
    }
    return (WOActionResults) result;
  }

  /**
   * Overridden to call {@see #addRequiredWebResources(WOResponse)}.
   */
  public void appendToResponse(WOResponse response, WOContext context) {
    super.appendToResponse(response, context);
    addRequiredWebResources(response, context);
  }

  protected void appendChildrenToResponse(WOResponse response, WOContext context) {
    if (_children != null) {
      _children.appendToResponse(response, context);
    }
  }

  public void takeValuesFromRequest(WORequest request, WOContext context) {
    if (_children != null) {
      _children.takeValuesFromRequest(request, context);
    }
  }

  /**
   * Override this method to append the needed scripts for this component.
   * @param res
   */
  protected abstract void addRequiredWebResources(WOResponse response, WOContext context);

  /**
   * Override this method to return the response for an Ajax request.
   * @param request
   * @param context
   * @return
   */
  protected abstract WOActionResults handleRequest(WORequest request, WOContext context);

}
