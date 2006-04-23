package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

public class AjaxDroppable extends AjaxComponent {
  private String myDraggableIDKeyName;
  private String myActionUrl;

  public AjaxDroppable(WOContext _context) {
    super(_context);
  }

  public void awake() {
    super.awake();
    myDraggableIDKeyName = scriptBaseName() + "_draggableID";
  }

  public boolean isStateless() {
    return true;
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    myActionUrl = context().componentActionURL();
    super.appendToResponse(_response, _context);
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
    onDropBuffer.append("var data = '" + myDraggableIDKeyName + "=' + element.draggableID;");
    onDropBuffer.append("var ajaxRequest = new Ajax.Request('" + myActionUrl + "', {method: 'get', parameters: data});");
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

  protected void addRequiredWebResources(WOResponse _res) {
    addScriptResourceInHead(_res, "prototype.js");
    addScriptResourceInHead(_res, "scriptaculous.js");
    addScriptResourceInHead(_res, "effects.js");
    addScriptResourceInHead(_res, "builder.js");
    addScriptResourceInHead(_res, "dragdrop.js");
    addScriptResourceInHead(_res, "controls.js");
  }

  protected WOActionResults handleRequest(WORequest _request, WOContext _context) {
    String droppedDraggableID = _request.stringFormValueForKey(myDraggableIDKeyName);
    if (canSetValueForBinding("droppedDraggableID")) {
      setValueForBinding(droppedDraggableID, "droppedDraggableID");
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