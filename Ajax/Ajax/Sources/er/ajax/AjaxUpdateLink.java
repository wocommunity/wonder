package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxUpdateLink extends AjaxComponent {

  public AjaxUpdateLink(WOContext _context) {
    super(_context);
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
    String id = (String) valueForBinding("id");
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

  protected void addRequiredWebResources(WOResponse _res) {
    addScriptResourceInHead(_res, "prototype.js");
    addScriptResourceInHead(_res, "scriptaculous.js");
    addScriptResourceInHead(_res, "effects.js");
    addScriptResourceInHead(_res, "builder.js");
    addScriptResourceInHead(_res, "dragdrop.js");
    addScriptResourceInHead(_res, "controls.js");
  }

  protected WOActionResults handleRequest(WORequest _request, WOContext _context) {
    WOActionResults results = (WOActionResults) valueForBinding("action");
    if (results != null) {
      System.out.println("AjaxUpdateLink.handleRequest: Not quite sure what to do with non-null results yet ...");
    }
    return null;
  }
}