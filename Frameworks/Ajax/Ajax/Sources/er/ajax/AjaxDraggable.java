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

/**
 * AjaxDraggable makes HTML elements draggable. Use in conjunction with
 * {@link AjaxDroppable}.
 * 
 * When omitContainer is false (default), the contents nested inside of the
 * AjaxDraggable will be made draggable. Otherwise an existing DOM element with
 * the id specified via the id binding will be made draggable.
 * 
 * For the scriptaculous options see
 * http://wiki.github.com/madrobby/scriptaculous/draggable
 * 
 * @binding id the id of the element to drag. When omitContainer is false, this
 *          is the id of the container surrounding the component content. When
 *          unspecified, a unique id will be generated.
 * @binding omitContainer if set to true, the container element will be omitted.
 *          The DOM id of the object to be made draggable must be specified with
 *          the id binding. Defaults to false.
 * @binding elementName the element to use for the container. defaults to "div".
 * @binding class the css class of the container
 * @binding style the css styles of the container
 * @binding draggableObject a java object which is passed to the AjaxDroppable
 *          when this draggable is dropped onto it.
 * @binding draggableID
 * @binding starteffect Effect, defaults to Effect.Opacity. Defines the effect
 *          to use when the draggable starts being dragged
 * @binding reverteffect Effect, default to Effect.Move. Defines the effect to
 *          use when the draggable reverts back to its starting position
 * @binding endeffect Effect, defaults to Effect.Opacity. Defines the effect to
 *          use when the draggable stops being dragged
 * @binding zindex integer value, defaults to 1000. The css z-index of the
 *          draggable item
 * @binding revert boolean or function reference, defaults to false. If set to
 *          true, the element returns to its original position when the drags
 *          ends. Revert can also be an arbitrary function reference, called
 *          when the drag ends. Specifying 'failure' will instruct the draggable
 *          not to revert if successfully dropped in a droppable.
 * @binding snap set to false no snapping occurs. Otherwise takes one of the
 *          following forms – Δi: one delta value for both horizontal and
 *          vertical snap, [Δx, Δy]: delta values for horizontal and vertical
 *          snap, function(x, y, draggable_object) { return [x, y]; }: a
 *          function that receives the proposed new top left coordinate pair and
 *          returns the coordinate pair to actually be used.
 * @binding ghosting boolean, defaults to false. Clones the element and drags
 *          the clone, leaving the original in place until the clone is dropped
 * @binding handle string or DOM reference, not set by default. Sets whether the
 *          element should only be draggable by an embedded handle. The value
 *          must be an element reference or element id. The value may also be a
 *          string referencing a CSS class value. The first
 *          child/grandchild/etc. element found within the element that has this
 *          CSS class value will be used as the handle.
 * @binding change Called just as onDrag (which is the preferred callback). Gets
 *          the Draggable instance as its parameter.
 * @binding keyPress
 * @binding scroll can be either a dom ID or a dom reference. In case of a dom
 *          reference the value must not be quoted. Set binding to "window" to
 *          scroll the window when the draggable reaches the window boundary.
 *          Set binding to "'someID'" to scroll the element with ID "someID"
 */
public class AjaxDraggable extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private static final String COMPONENT_DRAGGABLES_MAP_KEY = "AjaxComponentDraggablesMap";
  private String _id;

  public AjaxDraggable(WOContext context) {
    super(context);
  }
  
  @Override
  public void awake() {
      super.awake();
  }

  @Override
  public void reset() {
      _id = null;
      super.reset();
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  @SuppressWarnings("unchecked")
  public static Object draggableObjectForPage(WOComponent page, String draggableID) {
    Object droppedObject = null;
    Map<WOComponent, Map<String, Object>> componentDraggablesMap = (Map<WOComponent, Map<String, Object>>)page.context().session().objectForKey(AjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
    if (componentDraggablesMap != null) {
      Map<String, Object> draggablesMap = componentDraggablesMap.get(page);
      if (draggablesMap != null) {
        droppedObject = draggablesMap.get(draggableID);
      }
    }
    return droppedObject;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void appendToResponse(WOResponse res, WOContext ctx) {
    if (canGetValueForBinding("draggableObject")) {
      Object draggableObject = valueForBinding("draggableObject");
      WOComponent page = context().page();
      Map<WOComponent, Map<String, Object>> componentDraggablesMap = (Map<WOComponent, Map<String, Object>>) ctx.session().objectForKey(AjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
      if (componentDraggablesMap == null) {
        componentDraggablesMap = new WeakHashMap<WOComponent, Map<String, Object>>();
        ctx.session().setObjectForKey(componentDraggablesMap, AjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
      }
      Map<String, Object> draggablesMap = componentDraggablesMap.get(page);
      if (draggablesMap == null) {
        draggablesMap = new HashMap<>();
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
    super.appendToResponse(res, ctx);
  }

  public NSDictionary<String, String> createAjaxOptions() {
    NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<>();
	// PROTOTYPE OPTIONS
    ajaxOptionsArray.addObject(new AjaxOption("starteffect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("reverteffect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("endeffect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("zindex", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("revert", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("snap", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("ghosting", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("handle", AjaxOption.DEFAULT));
    ajaxOptionsArray.addObject(new AjaxOption("change", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("keyPress", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("scroll", AjaxOption.SCRIPT));
    NSMutableDictionary<String, String> options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
    return options;
  }

  public String id() {
		if (_id == null) {
			if (canGetValueForBinding("id") && valueForBinding("id") != null) {
				_id = (String) valueForBinding("id");
			}
			else {
				// only try to set the id binding if the id is auto-generated.
				// canSetValueForBinding will report true for all OGNL expressions
				_id = safeElementID();
				if (canSetValueForBinding("id")) {
					setValueForBinding(_id, "id");
				}
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

  @Override
  protected void addRequiredWebResources(WOResponse res) {
    addScriptResourceInHead(res, "prototype.js");
	addScriptResourceInHead(res, "effects.js");
	addScriptResourceInHead(res, "dragdrop.js");
	addScriptResourceInHead(res, "wonder.js");
  }

  @Override
  public WOActionResults handleRequest(WORequest request, WOContext context) {
    return null;
  }
}
