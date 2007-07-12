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

public abstract class AjaxDynamicElement extends WODynamicGroup {
  protected Logger log = Logger.getLogger(getClass());
  private WOElement _children;
  private NSDictionary _associations;
  
  public AjaxDynamicElement(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
    _children = children;
    _associations = associations;
  }
  
  public NSDictionary associations() {
	  return _associations;
  }
  
  public Object valueForBinding(String name, Object defaultValue, WOComponent component) {
      Object value = valueForBinding(name, component);
      if(value != null) {
          return value;
      }
      return defaultValue;
  }
  
  public Object valueForBinding(String name, WOComponent component) {
      WOAssociation association = (WOAssociation) _associations.objectForKey(name);
      if(association != null) {
          return association.valueInComponent(component);
      }
      return null;
  }
  
  public void setValueForBinding(Object value, String name, WOComponent component) {
	  WOAssociation association = (WOAssociation) _associations.objectForKey(name);
	  if(association != null) {
		   association.setValue(value, component);
	  }
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
    } else if (hasChildrenElements()) {
      result = super.invokeAction(request, context);
    }
    return (WOActionResults) result;
  }

  /**
   * Overridden to call {@see #addRequiredWebResources(WOResponse)}.
   */
  public void appendToResponse(WOResponse response, WOContext context) {
    addRequiredWebResources(response, context);
  }

  public void appendTagAttributeToResponse(WOResponse response, String name, Object object) {
	  if(object != null) {
		  response._appendTagAttributeAndValue(name, object.toString(), true);
	  }
  }

  public void takeValuesFromRequest(WORequest request, WOContext context) {
    takeChildrenValuesFromRequest(request, context);
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
