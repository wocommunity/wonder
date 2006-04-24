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
      if (canSetValueForBinding("droppedObject")) {
        WOComponent page = _context.page();
        Object droppedObject = AjaxDraggable.draggableObjectForPage(page, droppedDraggableID);
        setValueForBinding(droppedObject, "droppedObject");
      }
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