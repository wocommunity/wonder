package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

public class AjaxDraggable extends AjaxComponent {
  public AjaxDraggable(WOContext _context) {
    super(_context);
  }

  public boolean isStateless() {
    return true;
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public String elementName() {
    return (String) valueForBinding("elementName", "div");
  }

  protected void addRequiredWebResources(WOResponse _res) {
    addScriptResourceInHead(_res, "prototype.js");
    addScriptResourceInHead(_res, "scriptaculous.js");
    addScriptResourceInHead(_res, "effects.js");
    addScriptResourceInHead(_res, "builder.js");
    addScriptResourceInHead(_res, "dragdrop.js");
    addScriptResourceInHead(_res, "controls.js");
  }

  protected WOActionResults handleRequest(WORequest _request, WOContext _context) {
    System.out.println("AjaxDraggable.handleRequest: handled request!");
    WOResponse response = createResponse(_context);
    return response;
  }

}