package er.ajax;

import com.webobjects.appserver.*;

public class AjaxBusyIndicator extends AjaxComponent {
  public AjaxBusyIndicator(WOContext context) {
    super(context);
  }

  public boolean isStateless() {
    return true;
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  protected void addRequiredWebResources(WOResponse res) {
    addScriptResourceInHead(res, "prototype.js");
    addScriptResourceInHead(res, "scriptaculous.js");
    addScriptResourceInHead(res, "effects.js");
  }

  public String busyImage() {
    return (String) valueForBinding("busyImage");
  }

  public String busyImageFramework() {
    return (String) valueForBinding("busyImageFramework");
  }

  public String divID() {
    return (String) valueForBinding("divID", "busy");
  }

  public WOActionResults handleRequest(WORequest request, WOContext context) {
    return null;
  }

}
