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
    _actionUrl = AjaxUtils.ajaxComponentActionUrl(context());
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
    ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
	if (options.objectForKey("evalScripts") == null) {
		options.setObjectForKey("true", "evalScripts");
	}
    return options;
  }

  public String elementName() {
    return (String) valueForBinding("elementName", "div");
  }

  public String onDrop() {
    StringBuffer onDropBuffer = new StringBuffer();
    onDropBuffer.append("function(element, droppableElement) {");
    String droppableElementID = (String) valueForBinding("id");
    onDropBuffer.append("if (droppableElement.id == '" + droppableElementID + "') {");
    onDropBuffer.append("var data = '" + _draggableIDKeyName + "=' + element.getAttribute(\'draggableID\');");
	String updateContainerID = (String) valueForBinding("updateContainerID");
	if (updateContainerID == null) {
		onDropBuffer.append("var ajaxRequest = new Ajax.Request('" + _actionUrl + "', {method: 'get', parameters: data");
	}
	else {
		onDropBuffer.append("var ajaxRequest = new Ajax.Updater('" + updateContainerID + "','" + _actionUrl + "', {method: 'get', parameters: data, evalScripts: true");
	}
    if(canGetValueForBinding("onComplete")) {
        onDropBuffer.append(",onComplete:" ); 
        onDropBuffer.append(valueForBinding("onComplete"));
    }
    onDropBuffer.append("});");
    
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
	addScriptResourceInHead(res, "effects.js");
	addScriptResourceInHead(res, "dragdrop.js");
  }

  public WOActionResults handleRequest(WORequest request, WOContext context) {
	AjaxUpdateContainer.setUpdateContainerID(request, (String) valueForBinding("updateContainerID"));
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