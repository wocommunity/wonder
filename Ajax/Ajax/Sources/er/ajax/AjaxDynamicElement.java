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
  private WOElement myChildren;

  public AjaxDynamicElement(String _name, NSDictionary _associations, WOElement _children) {
    super(_name, _associations, _children);
    myChildren = _children;
  }

  protected void addScriptResourceInHead(WOContext _context, WOResponse _response, String _fileName) {
    AjaxUtils.addScriptResourceInHead(_context, _response, _fileName);
  }

  /**
   * Execute the request, if it's comming from our action, then invoke the
   * ajax handler and put the key <code>AJAX_REQUEST_KEY</code> in the
   * request userInfo dictionary (<code>request.userInfo()</code>).
   */
  public WOActionResults invokeAction(WORequest _request, WOContext _context) {
    Object result = null;
    if (AjaxUtils.shouldHandleRequest(_request, _context)) {
      result = handleRequest(_request, _context);
      AjaxUtils.updateMutableUserInfoWithAjaxInfo(_context);
    }
    else if (myChildren != null) {
      result = myChildren.invokeAction(_request, _context);
    }
    return (WOActionResults) result;
  }

  /**
   * Overridden to call {@see #addRequiredWebResources(WOResponse)}.
   */
  public void appendToResponse(WOResponse _response, WOContext _context) {
    super.appendToResponse(_response, _context);
    addRequiredWebResources(_response, _context);
  }

  protected void appendChildrenToResponse(WOResponse _response, WOContext _context) {
    if (myChildren != null) {
      myChildren.appendToResponse(_response, _context);
    }
  }

  public void takeValuesFromRequest(WORequest _request, WOContext _context) {
    if (myChildren != null) {
      myChildren.takeValuesFromRequest(_request, _context);
    }
  }

  /**
   * Override this method to append the needed scripts for this component.
   * @param res
   */
  protected abstract void addRequiredWebResources(WOResponse _response, WOContext _context);

  /**
   * Override this method to return the response for an Ajax request.
   * @param request
   * @param context
   * @return
   */
  protected abstract WOActionResults handleRequest(WORequest _request, WOContext _context);

}
