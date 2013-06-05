package er.ajax.mootools;

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

import er.ajax.AjaxComponent;
import er.ajax.AjaxOption;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;

/*
 * AjaxDraggable makes HTML elements draggable. Use in conjunction with
 * {@link AjaxDroppable}.	 * @binding onBeforeStart - Executed before the Drag instance attaches the events. Receives the dragged element as an argument.
 * @binding onStart - Executed when the user starts to drag (on mousedown). Receives the dragged element as an argument.
 * @binding onSnap - Executed when the user has dragged past the snap option. Receives the dragged element as an argument.
 * @binding onDrag - Executed on every step of the drag. Receives the dragged element and the event as arguments.
 * @binding onComplete - Executed when the user completes the drag. Receives the dragged element and the event as arguments.
 * @binding onCancel - Executed when the user has cancelled the drag. Receives the dragged element as an argument.
 * @binding onDrop- Executed when the element drops. Passes as argument the element and the element dropped on and the event. If dropped on nothing, the second argument is null.	
 * @binding onLeave - Executed when the element leaves one of the droppables.
 * @binding onEnter - Executed when the element enters one of the droppables.
 * Options
 * 
 * @binding grid - (integer: defaults to false) Distance in pixels for snap-to-grid dragging.
 * @binding handle - (element: defaults to the element passed in) The Element to act as the handle for the draggable element.
 * @binding invert - (boolean: defaults to false) Whether or not to invert the values reported on start and drag.
 * @binding limit - (object: defaults to false) An object with x and y dimensions used to limit the movement of the Element.
 * @binding modifiers - (object: defaults to {'x': 'left', 'y': 'top'}) An object with x and y properties used to indicate the CSS modifiers (i.e. 'left').
 * @binding style - (boolean: defaults to true) Whether or not to set the modifier as a style property of the element.
 * @binding snap - (integer: defaults to 6) The distance to drag before the Element starts to respond to the drag.
 * @binding unit - (string: defaults to 'px') A string indicating the CSS unit to append to all integer values.
 * @binding preventDefault - (boolean: defaults to false) Calls preventDefault on the event while dragging. See Event:preventDefault
 * @binding stopPropagation - (boolean: defaults to false) Prevents the event from "bubbling" up in the DOM tree. See Event:stopPropagation 
 * @binding container - (element) If an Element is passed, drag will be limited to the passed Element's size and position.
 * @binding droppables - String that contains the class names of the droppable elements.
 * @binding precalculate - (boolean; defaults to false) If true, the class will calculate the locations and dimensions of the droppables which will increase performance. If the droppables are likely to change shape, size, or location it is best to leave this as false.
 * @binding includeMargins - (boolean; defaults to true) This option only applies when the container option is set. If true (the default) the margins are included in the calculations for the bounding box of the draggable item. This means that if you have a margin on your draggable then the border of the draggable can never touch the edge of the container. Setting it to false ignores this margin.
 * @binding checkDroppables - (boolean; defaults to true) Checks against the droppables on drag if true.
 * @binding useSpinner (boolean) use the Spinner class with this request
 * @binding defaultSpinnerClass inclue the default spinner css class in the headers - if false provide your own.
 * @binding spinnerOptions - (object) the options object for the Spinner class
 * @binding spinnerTarget - (mixed) a string of the id for an Element or an Element reference to use instead of the one specifed in the update option. This is useful if you want to overlay a different area (or, say, the parent of the one being updated).
 * 
 */



public class MTAjaxDraggable extends AjaxComponent {

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_DRAGGABLES_MAP_KEY = "AjaxComponentDraggablesMap";
	private String _id;
	private String _variableName;
	private String _draggableIDKeyName;
	private String _actionUrl;
	private String _elementID;
	private String _droppedArea;

	public MTAjaxDraggable(WOContext context) {
        super(context);
    }

	@Override
	public void reset() {
		_id = null;
		super.reset();
	}

	@Override
	public void awake() {
		super.awake();
		_draggableIDKeyName = safeElementID() + "_draggableID";
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static Object draggableObjectForPage(WOComponent page, String draggableID) {
		Object droppedObject = null;
		Map componentDraggablesMap = (Map)page.context().session().objectForKey(MTAjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
		if(componentDraggablesMap != null) {
			Map draggablesMap = (Map)componentDraggablesMap.get(page);
			if(draggablesMap != null) {
				droppedObject = draggablesMap.get(draggableID);
			}
		}
		return droppedObject;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {

		_actionUrl = AjaxUtils.ajaxComponentActionUrl(context());
		_elementID = context.elementID();
		if(canGetValueForBinding("draggableObject")) {
			Object draggableObject = valueForBinding("draggableObject");
			WOComponent page = context().page();
			Map componentDraggablesMap = (Map)context.session().objectForKey(MTAjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
			if(componentDraggablesMap == null) {
				componentDraggablesMap = new WeakHashMap();
				context.session().setObjectForKey(componentDraggablesMap, MTAjaxDraggable.COMPONENT_DRAGGABLES_MAP_KEY);
			}
			Map draggablesMap = (Map)componentDraggablesMap.get(page);
			if(draggablesMap == null) {
				draggablesMap = new HashMap();
				componentDraggablesMap.put(page, draggablesMap);
			}
			String id = draggableID();
			if(draggableObject == null) {
				draggablesMap.remove(id);
			} else {
				draggablesMap.put(id, draggableObject);
			}
		}
		super.appendToResponse(response, context);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public NSDictionary createAjaxOptions() {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();

		// PROTOTYPE OPTIONS
		ajaxOptionsArray.addObject(new AjaxOption("grid", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("handle", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("invert", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("limit", AjaxOption.DICTIONARY));
		ajaxOptionsArray.addObject(new AjaxOption("modifiers", AjaxOption.DICTIONARY));
		ajaxOptionsArray.addObject(new AjaxOption("snap", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("style", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("unit", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("preventDefault", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("stopPropagation", AjaxOption.BOOLEAN));
		
		ajaxOptionsArray.addObject(new AjaxOption("container", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("droppables", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("precalculate", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("includeMargins", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("checkDroppables", AjaxOption.BOOLEAN));
		
		ajaxOptionsArray.addObject(new AjaxOption("onBeforeStart", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onStart", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSnap", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onCancel", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onDrag", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onDrop", AjaxOption.SCRIPT));

		ajaxOptionsArray.addObject(new AjaxOption("onEnter", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onLeave", AjaxOption.SCRIPT));

		ajaxOptionsArray.addObject(new AjaxOption("ghost", Boolean.FALSE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("useSpinner", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerTarget", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerOptions", AjaxOption.DICTIONARY));

		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
		
		return options;
	
	}

	public String draggableID() {
		String draggableID;
		if(canGetValueForBinding("draggableID")) {
			draggableID = (String)valueForBinding("draggableID");
		} else {
			draggableID = id();
		}
		return draggableID;
	}	
	
	public String id() {
		if(_id == null) {
			if(canGetValueForBinding("id") && valueForBinding("id") != null) {
				_id = (String) valueForBinding("id");
			} else {
				_id = safeElementID();
				if(canSetValueForBinding("id")) {
					setValueForBinding(_id, "id");
				}
			}
		}
		return _id;
	}	
	
	public String variableName() {
		if(_variableName == null) {
			_variableName = valueForStringBinding("variableName",  "mtAD" + ERXWOContext.safeIdentifierName(context(), true));
		}		
		return _variableName;
	}
	
	public boolean ghost() {
		return (Boolean)valueForBinding("ghost", Boolean.FALSE);
	}
	
	public String quotedID() {
		return AjaxUtils.quote(id());
	}

	public String quotedContextID() {
		return AjaxUtils.quote(context().contextID());
	}
	
	public String quotedElementID() {
		return AjaxUtils.quote(_elementID);
	}
	
	public String elementName() {
		return (String)valueForBinding("elementName", "div");
	}

	public String updateContainerID() {
		return (String) valueForBinding("updateContainerID");
	}
	
	public String quotedUpdateContainerID() {
		return AjaxUtils.quote(updateContainerID());
	}	

	public String quotedActionUrl() {
		boolean submit = ERXComponentUtilities.booleanValueForBinding(this, "submit", false);
		String actionUrl = (submit && updateContainerID() == null) ? null : _actionUrl;
		return actionUrl != null ? AjaxUtils.quote(_actionUrl) : null;
	}
	
	public String quotedDraggableKeyName() {
		return AjaxUtils.quote(_draggableIDKeyName);
	}
	
	public boolean submit() {
		return ERXComponentUtilities.booleanValueForBinding(this, "submit", false);
	}
	
	public String form() {
		return (String) valueForBinding("formName");
	}	
	
	/*
	@SuppressWarnings("unused")
	public String onDrop() {
		
		boolean submit = ERXComponentUtilities.booleanValueForBinding(this, "submit", false);
		
		String contextID = AjaxUtils.quote(context().contextID());
		String elementID = AjaxUtils.quote(_elementID);
		String droppableElementID = AjaxUtils.quote((String)valueForBinding("id"));
		String draggableKeyName = AjaxUtils.quote(_draggableIDKeyName);
		String updateContainerID = AjaxUtils.quote((String) valueForBinding("updateContainerID"));
		String actionUrl = (submit && updateContainerID == null) ? null : AjaxUtils.quote(_actionUrl);
		String form = (String) valueForBinding("formName");
	
		if(submit) {
			if(form == null) {
				form = ERXWOForm.formName(context(), null);
				if(form == null) {
					throw new IllegalArgumentException("If submit is true, you must provide either a formName or your containing form must have a name.");
				}
			}
			form = "document." + form;
		}

		return "function(element, droppable) { if(droppable) { element.destroy(); MTAUL.update("+updateContainerID+", { data-updateUrl: " + actionUrl + "}, " + contextID + " + '.' + " + elementID + ", null); }}";
	}	
	*/
	
	@Override
	protected void addRequiredWebResources(WOResponse res) {

		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		Boolean useSpinner = booleanValueForBinding("useSpinner", false);
		if(useSpinner) {
			Boolean useDefaultSpinnerClass = booleanValueForBinding("defaultSpinnerClass", true);
			if(useDefaultSpinnerClass) {
				MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", "scripts/plugins/spinner/spinner.css");
			}
		}

		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}
	
	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		
		MTAjaxUpdateContainer.setUpdateContainerID(request, (String) valueForBinding("updateContainerID"));
		String droppedDraggableID = request.stringFormValueForKey(_draggableIDKeyName);
		_droppedArea = request.stringFormValueForKey("dropAreaID");

		if (canSetValueForBinding("droppedDraggableID")) {
			setValueForBinding(droppedDraggableID, "droppedDraggableID");
		}
		
		if (canSetValueForBinding("droppedObject")) {
			WOComponent page = context.page();
			Object droppedObject = MTAjaxDraggable.draggableObjectForPage(page, droppedDraggableID);
			setValueForBinding(droppedObject, "droppedObject");
		}
		
		if(canSetValueForBinding("droppedArea")) {
			setValueForBinding(_droppedArea, "droppedArea");
		}
		
		if (canGetValueForBinding("action")) {
			WOActionResults results = (WOActionResults) valueForBinding("action");
			if (results != null) {
				System.out.println("MTAjaxDroppable.handleRequest: Not quite sure what to do with non-null results yet ...");
			}
		}
		return null;
	}
}
