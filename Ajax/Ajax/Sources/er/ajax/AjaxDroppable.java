package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxDroppable extends AjaxComponent {
  private String _draggableIDKeyName;
  private String _actionUrl;

  public AjaxDroppable(WOContext _context) {
    super(_context);
  }

  public void awake() {
    super.awake();
    _draggableIDKeyName = safeElementID() + "_draggableID";
  }

  public boolean isStateless() {
    return true;
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    _actionUrl = context().componentActionURL();
    super.appendToResponse(response, context);
  }

  public NSDictionary createAjaxOptions() {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("accept", AjaxOption.STRING_ARRAY));
    ajaxOptionsArray.addObject(new AjaxOption("containment", AjaxOption.STRING_ARRAY));
    ajaxOptionsArray.addObject(new AjaxOption("hoverclass", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("overlap", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("greedy", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("onHover", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
    return options;
  }

  public String elementName() {
    return (String) valueForBinding("elementName", "div");
  }

  public String onDrop() {
    StringBuffer onDropBuffer = new StringBuffer();
    onDropBuffer.append("function(element, droppableElement) {");
    // onComplete:ajaxResponse
    String droppableElementID = (String) valueForBinding("id");
    onDropBuffer.append("if (droppableElement.id == '" + droppableElementID + "') {");
    onDropBuffer.append("var data = '" + _draggableIDKeyName + "=' + element.getAttribute(\'draggableID\');");
    onDropBuffer.append("var ajaxRequest = new Ajax.Request('" + _actionUrl + "', {method: 'get', parameters: data});");
    if (canGetValueForBinding("onDrop")) {
      String onDrop = (String) valueForBinding("onDrop");
      onDropBuffer.append(" var parentOnDrop = ");
      onDropBuffer.append(onDrop);
      onDropBuffer.append(";");
      onDropBuffer.append("parentOnDrop(element, droppableElement);");
    }
    onDropBuffer.append("}");
    onDropBuffer.append("}");
    return onDropBuffer.toString();
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
    String droppedDraggableID = request.stringFormValueForKey(_draggableIDKeyName);
    if (canSetValueForBinding("droppedDraggableID")) {
      setValueForBinding(droppedDraggableID, "droppedDraggableID");
    }
    if (canSetValueForBinding("droppedObject")) {
      WOComponent page = context.page();
      Object droppedObject = AjaxDraggable.draggableObjectForPage(page, droppedDraggableID);
      setValueForBinding(droppedObject, "droppedObject");
    }
    if (canGetValueForBinding("action")) {
      WOActionResults results = (WOActionResults) valueForBinding("action");
      if (results != null) {
        System.out.println("AjaxDroppable.handleRequest: Not quite sure what to do with non-null results yet ...");
      }
    }
    return null;
  }

}