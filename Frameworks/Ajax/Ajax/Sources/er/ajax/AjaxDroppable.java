package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.ERXComponentUtilities;
import er.extensions.components._private.ERXWOForm;

/**
 * 
 * @binding onBeforeDrop the function to execute before notifying the server of the drop
 * @binding onDrop the function to execute after notifying the server of the drop
 * @binding submit if true, drop will perform a form submit
 * @binding formName the name of the form to submit (if submit is true)
 * @binding confirmMessage if set, a confirm dialog with the given message is shown on drop. Allows cancelling a drop.
 * @binding id
 * @binding elementName
 * @binding droppedDraggableID
 * @binding action
 * @binding droppedObject
 * @binding style
 * @binding accept
 * @binding containment
 * @binding hoverclass
 * @binding overlap
 * @binding greedy
 * @binding onHover
 * @binding onComplete
 * @binding updateContainerID
 * @binding evalScripts
 * @binding disabled
 * @binding class
 *  
 * @author mschrag
 */
public class AjaxDroppable extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private String _draggableIDKeyName;
  private String _actionUrl;
  private String _elementID;

  public AjaxDroppable(WOContext _context) {
    super(_context);
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

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    _actionUrl = AjaxUtils.ajaxComponentActionUrl(context());
    _elementID = context.elementID();
    super.appendToResponse(response, context);
  }

  public NSDictionary createAjaxOptions() {
	  // PROTOTYPE OPTIONS
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
	  boolean submit = ERXComponentUtilities.booleanValueForBinding(this, "submit", false); 
	  String contextID = AjaxUtils.quote(context().contextID());
	  String elementID = AjaxUtils.quote(_elementID);
	  String droppableElementID = AjaxUtils.quote((String) valueForBinding("id"));
	  String draggableKeyName = AjaxUtils.quote(_draggableIDKeyName);
	  String updateContainerID = AjaxUtils.quote((String) valueForBinding("updateContainerID"));
	  String actionUrl = (submit && updateContainerID == null) ? null : AjaxUtils.quote(_actionUrl);
	  String form = (String) valueForBinding("formName");
	  if (submit) {
		  if (form == null) {
			  form = ERXWOForm.formName(context(), null);
			  if (form == null) {
				  throw new IllegalArgumentException("If submit is true, you must provide either a formName or your containing form must have a name.");
			  }
		  }
		  form = "document." + form;
	  }
	  String onbeforedrop = (String) valueForBinding("onBeforeDrop");
	  String ondrop = (String) valueForBinding("onDrop");
	  
	  NSMutableDictionary options = new NSMutableDictionary();
	  if (canGetValueForBinding("onComplete")) {
		  options.setObjectForKey(valueForBinding("onComplete"), "onComplete");
	  }
	  if (canGetValueForBinding("confirmMessage")) {
			options.setObjectForKey(new AjaxValue(AjaxOption.STRING, valueForBinding("confirmMessage")).javascriptValue(), "confirmMessage");
	  }
	  if (submit) {
		  AjaxSubmitButton.fillInAjaxOptions(this, this, _elementID, options);
	  }
	 
	  StringBuffer onDropBuffer = new StringBuffer();
	  onDropBuffer.append("ADP.droppedFunc(" + contextID + "," + elementID + "," + droppableElementID + "," + draggableKeyName + "," + updateContainerID + "," + actionUrl + "," + form + "," + onbeforedrop + "," + ondrop + ",");
	  AjaxOptions.appendToBuffer(options, onDropBuffer, context());
	  onDropBuffer.append(')');
	  
	  return onDropBuffer.toString();
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
