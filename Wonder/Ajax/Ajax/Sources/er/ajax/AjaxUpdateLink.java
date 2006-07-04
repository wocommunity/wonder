package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxUpdateLink extends AjaxComponent {

  public AjaxUpdateLink(WOContext context) {
    super(context);
  }

  public boolean isStateless() {
    return true;
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public String onClick() {
    NSDictionary options = createAjaxOptions();
    String actionUrl = context().componentActionURL();
    String id = (String) valueForBinding("updateContainerID");
    StringBuffer sb = new StringBuffer();
    sb.append("new Ajax.Updater('");
    sb.append(id);
    sb.append("', '");
    sb.append(actionUrl);
    sb.append("', ");
    AjaxOptions.appendToBuffer(options, sb, context());
    sb.append(");");
    return sb.toString();
  }

  protected NSDictionary createAjaxOptions() {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("insertion", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);

    options.setObjectForKey("'get'", "method");
    String updateContainerID = (String) valueForBinding("updateContainerID");
    if (updateContainerID != null) {
      options.setObjectForKey("'invokeWOElementID=' + $(" + updateContainerID + ").woElementID", "parameters");
    }

    return options;
  }

  protected void addRequiredWebResources(WOResponse res) {
    addScriptResourceInHead(res, "prototype.js");
    addScriptResourceInHead(res, "scriptaculous.js");
    addScriptResourceInHead(res, "effects.js");
    addScriptResourceInHead(res, "builder.js");
    addScriptResourceInHead(res, "dragdrop.js");
    addScriptResourceInHead(res, "controls.js");
  }

  protected WOActionResults handleRequest(WORequest request, WOContext context) {
    WOActionResults results = (WOActionResults) valueForBinding("action");
    if (results != null) {
      System.out.println("AjaxUpdateLink.handleRequest: Not quite sure what to do with non-null results yet ...");
    }
    return null;
  }
}