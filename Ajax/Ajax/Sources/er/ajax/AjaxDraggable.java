package er.ajax;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxDraggable extends AjaxComponent {
  private static final String COMPONENT_DRAGGABLES_MAP_KEY = "AjaxComponentDraggablesMap";
  private String _id;

  public AjaxDraggable(WOContext _context) {
    super(_context);
  }
  
  public void awake() {
      super.awake();
  }

  public void reset() {
      _id = null;
      super.reset();
  }

  public boolean isStateless() {
    return true;
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
  
  public static Object draggableObjectForPage(WOComponent _page, String _draggableID) {
    Object droppedObject = null;
    Map componentDraggablesMap = (Map)_page.context().session().objectForKey(AjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
    if (componentDraggablesMap != null) {
      Map draggablesMap = (Map) componentDraggablesMap.get(_page);
      if (draggablesMap != null) {
        droppedObject = draggablesMap.get(_draggableID);
      }
    }
    return droppedObject;
  }
  
  public void appendToResponse(WOResponse _res, WOContext _ctx) {
    if (canGetValueForBinding("draggableObject")) {
      Object draggableObject = valueForBinding("draggableObject");
      WOComponent page = context().page();
      Map componentDraggablesMap = (Map)_ctx.session().objectForKey(AjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
      if (componentDraggablesMap == null) {
        componentDraggablesMap = new WeakHashMap();
        _ctx.session().setObjectForKey(componentDraggablesMap, AjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
      }
      Map draggablesMap = (Map) componentDraggablesMap.get(page);
      if (draggablesMap == null) {
        draggablesMap = new HashMap();
        componentDraggablesMap.put(page, draggablesMap);
      }
      String id = draggableID();
      if (draggableObject == null) {
        draggablesMap.remove(id);
      }
      else {
        draggablesMap.put(id, draggableObject);
      }
    }
    super.appendToResponse(_res, _ctx);
  }

  public NSDictionary createAjaxOptions() {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("starteffect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("reverteffect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("endeffect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("zindex", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("revert", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("snap", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("ghosting", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("handle", AjaxOption.DEFAULT));
    ajaxOptionsArray.addObject(new AjaxOption("change", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
    return options;
  }

  public String id() {
      if(_id == null) {
          _id = canGetValueForBinding("id") && valueForBinding("id") != null ? (String)valueForBinding("id") : scriptBaseName();
          if(canSetValueForBinding("id")) {
              setValueForBinding(_id, "id");
          }
      }
      return _id;
  }

  public String elementName() {
    return (String) valueForBinding("elementName", "div");
  }

  public String draggableID() {
    String draggableID;
    if (canGetValueForBinding("draggableID")) {
        draggableID = (String) valueForBinding("draggableID");
    } else {
        draggableID = id();
    }
    return draggableID;
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
    return null;
  }

}