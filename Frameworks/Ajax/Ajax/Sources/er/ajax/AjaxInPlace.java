package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.components._private.ERXWOForm;

/**
 * AjaxInPlace is a generalization of the AjaxInPlaceEditor. To use this component, you must wrap an ERXWOTemplate named
 * "view" and an ERXWOTemplate named "edit". <br />
 * For instance:<br />
 * <br />
 * HTML:
 * 
 * <pre>
 *   &lt;webobject name = &quot;ExampleInPlace&quot;&gt;
 *      &lt;webobject name = &quot;View&quot;&gt;View: &lt;webobject name = &quot;Value&quot;/&gt;&lt;/webobject&gt;
 *      &lt;webobject name = &quot;Edit&quot;&gt;Edit: &lt;webobject name = &quot;ValueField&quot;/&gt;&lt;/webobject&gt;
 *   &lt;/webobject&gt;
 * </pre>
 * 
 * WOD:
 * 
 * <pre>
 *   ExampleInPlace : AjaxInPlace {
 *   }
 *   
 *   View : ERXWOTemplate {
 *      templateName = &quot;view&quot;;
 *   }
 *   
 *   Value : WOString {
 *      value = value;
 *   }
 *   
 *   Edit : ERXWOTemplate {
 *      templateName = &quot;edit&quot;;
 *   }
 *   
 *   ValueField : WOTextField {
 *      value = value;
 *   }
 * </pre>
 * 
 * @binding class the class used on the top container
 * @binding id the id used on various parts of this component
 * 
 * @binding saveLabel the label to show on the save button
 * @binding saveAction the action to invoke on save
 * @binding saveUpdateContainerID by default save updates the container specified in "id", but you can override that with this binding  
 * @binding saveClass the class of the save button
 * @binding canSave if true, the results are saved; if false, the user is not allowed to leave edit mode
 * @binding onSaveClick the action to fire when save is clicked
 * @binding onSaveSuccess the javascript function to execute after a successful save
 * @binding onSaveFailure the javascript function to execute after a failed save
 * @binding onSaving the javascript action to fire when saving
 * @binding button if true, the save action is a button; if false, it's a link 
 * @binding submitOnSave if true, the save button is an AjaxSubmitButton; if false, it's an AjaxUpdateLink (which will not actually submit a form -- you would have to do some work here)
 * // PROTOTYPE FUNCTIONS
 * @binding saveInsertion the insertion function to use on save
 * @binding saveInsertionDuration the duration of the before and after insertion animation (if using insertion) 
 * @binding saveBeforeInsertionDuration the duration of the before insertion animation (if using insertion) 
 * @binding saveAfterInsertionDuration the duration of the after insertion animation (if using insertion)
 * 
 * @binding cancelLabel the label to show on the cancel button
 * @binding cancelAction the action to invoke on cancel
 * @binding cancelUpdateContainerID by default cancel updates the container specified in "id", but you can override that with this binding  
 * @binding cancelClass the class of the cancel button
 * @binding onCancelClick the action to fire when cancel is clicked
 * @binding onCancelSuccess the javascript function to execute after a successful cancel
 * @binding onCancelFailure the javascript function to execute after a failed cancel
 * @binding onCancelling the javascript action to fire when cancelling
 * // PROTOTYPE FUNCTIONS
 * @binding cancelInsertion the insertion function to use on cancel
 * @binding cancelInsertionDuration the duration of the before and after insertion animation (if using insertion) 
 * @binding cancelBeforeInsertionDuration the duration of the before insertion animation (if using insertion) 
 * @binding cancelAfterInsertionDuration the duration of the after insertion animation (if using insertion)
 * 
 * @binding editClass the class of the div that you click on to trigger edit mode (yes this name sucks)
 * @binding formClass the class of the form around the edit view
 * @binding canEdit if true, edit mode is entered; if false, view mode remains active
 * @binding editOnly if true, edit mode is locked on (and save controls don't show if it's in a parent form); if false, you can switch between edit and view mode
 * @binding onEditClick the action to fire when edit mode is triggered
 * @binding onEditSuccess the javascript function to execute after a successful edit
 * @binding onEditFailure the javascript function to execute after a failed edit
 * @binding onEditing the javascript action to fire when editing mode is loading
 * // PROTOTYPE FUNCTIONS
 * @binding editInsertion the insertion function to use on edit
 * @binding editInsertionDuration the duration of the before and after insertion animation (if using insertion) 
 * @binding editBeforeInsertionDuration the duration of the before insertion animation (if using insertion) 
 * @binding editAfterInsertionDuration the duration of the after insertion animation (if using insertion)
 * 
 * @binding onRefreshComplete the javascript function to execute after refreshing the container
 * @binding disabled whether or not edit mode should be disabled
 * 
 * @binding manualControl if true, it is up to you to provide click-to-edit, save, and cancel controls
 * @binding manualViewControl if true, it is up to you to provide click-to-edit controls
 * @binding manualEditControl if true, it is up to you to provide save and cancel controls
 * 
 * @binding style the style of the top level container
 * @binding elementName the name of the container element (defaults to "div")
 * @binding formSerializer the name of the javascript function to call to serialize the form
 * 
 * @binding onsubmit pass through onsubmit to form
 * 
 * @author mschrag
 */
public class AjaxInPlace extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public static final Logger log = Logger.getLogger(AjaxInPlace.class);
  
	private boolean _editing;
	private String _id;
	private boolean _changingToEdit;
	private boolean _changingToView;
	private boolean _alreadyInForm;

	public AjaxInPlace(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String elementName() {
		String elementName = (String) valueForBinding("elementName");
		if (elementName == null) {
			elementName = "div";
		}
		return elementName;
	}
	
	public String id() {
		if (_id == null) {
			if (hasBinding("id")) {
				_id = (String) valueForBinding("id");
			}
			else {
				_id = ERXWOContext.safeIdentifierName(context(), false);
			}
		}
		return _id;
	}
	
	@Override
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		_alreadyInForm = context().isInForm();
		WOActionResults results = super.invokeAction(aRequest, aContext);
		// MS: see appendToResponse
		_id = null;
		return results;
	}
	
	@Override
	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		_alreadyInForm = context().isInForm();
		super.takeValuesFromRequest(aRequest, aContext);
		// MS: see appendToResponse
		_id = null;
	}

	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		_alreadyInForm = context().isInForm();
		super.appendToResponse(aResponse, aContext);
		// MS: id was being cached, but if the structure of the page changes,
		// it can cache too aggressively.  We really only care that the id
		// is cached for the duration of a single R-R loop.  When we're done,
		// toss the value so it can be recalculated properly the next time.
		_id = null;
		_changingToEdit = false;
		_changingToView = false;
	}
	
	public String saveUpdateContainerID() {
		String saveUpdateContainerID = null;
		if (hasBinding("saveUpdateContainerID")) {
			saveUpdateContainerID = (String)valueForBinding("saveUpdateContainerID");
		}
		if (saveUpdateContainerID == null) {
			saveUpdateContainerID = id();
		}
		return saveUpdateContainerID;
	}
	
	public String cancelUpdateContainerID() {
		String cancelUpdateContainerID = null;
		if (hasBinding("cancelUpdateContainerID")) {
			cancelUpdateContainerID = (String)valueForBinding("cancelUpdateContainerID");
		}
		if (cancelUpdateContainerID == null) {
			cancelUpdateContainerID = id();
		}
		return cancelUpdateContainerID;
	}
	
	public String saveLabel() {
	  String saveLabel = (String)valueForBinding("saveLabel");
	  if (saveLabel == null) {
	    saveLabel = "save";
	  }
	  return saveLabel;
	}
	
	public String formName() {
	  String formName = null;
	  if (_alreadyInForm) {
	    formName = ERXWOForm.formName(context(), null);
	    if (formName == null) {
	      AjaxInPlace.log.warn(id() + " is already inside of a form, but that form has no name, so AjaxInPlace can't work properly.");
	      formName = "SetTheParentFormName";
	    }
	  }
	  else {
	    formName = id();
	  }
	  return formName;
	}
	
  public boolean button() {
    return ERXComponentUtilities.booleanValueForBinding("button", true, _keyAssociations, parent());
  }

	public boolean submitOnSave() {
		return ERXComponentUtilities.booleanValueForBinding("submitOnSave", true, _keyAssociations, parent());
	}

	public boolean linkOnSave() {
		return !submitOnSave();
	}
	
	public boolean disableForm() {
	  return _alreadyInForm || linkOnSave();
	}

	public String updateFunctionName() {
		return id() + "Update();";
	}

	public String editFunctionName() {
		return id() + "Edit";
	}

	public String editFunctionCall() {
		String editFunctionCall = null;
		if (!disabled()) {
			editFunctionCall = editFunctionName() + "()";
		}
		return editFunctionCall;
	}

	public String saveFunctionName() {
		return id() + "Save";
	}

	public String saveFunctionCall() {
		return saveFunctionName() + "()";
	}

	public String cancelFunctionName() {
		return id() + "Cancel";
	}

	public String cancelFunctionCall() {
		return cancelFunctionName() + "()";
	}

	public boolean manualControl() {
		boolean manualControl = false;
		if (hasBinding("manualControl")) {
			manualControl = ERXComponentUtilities.booleanValueForBinding(this, "manualControl");
		}
		return manualControl;
	}

	public boolean manualEditControl() {
		boolean manualEditControl = manualControl();
		if (!manualEditControl && hasBinding("manualEditControl")) {
			manualEditControl = ERXComponentUtilities.booleanValueForBinding(this, "manualEditControl");
		}
		if (_alreadyInForm && editOnly()) {
		  manualEditControl = true;
		}
		return manualEditControl;
	}

	public boolean manualViewControl() {
		boolean manualViewControl = manualControl();
		if (!manualViewControl && hasBinding("manualViewControl")) {
			manualViewControl = ERXComponentUtilities.booleanValueForBinding(this, "manualViewControl");
		}
		return manualViewControl;
	}

	public boolean disabled() {
		boolean disabled = false;
		if (hasBinding("disabled")) {
			disabled = ERXComponentUtilities.booleanValueForBinding(this, "disabled");
		}
		return disabled;
	}

	public boolean editOnly() {
	  return ERXComponentUtilities.booleanValueForBinding(this, "editOnly");
	}
	
	public boolean editing() {
		if (hasBinding("editing")) {
			_editing = ERXComponentUtilities.booleanValueForBinding(this, "editing");
		}
		return !disabled() && (_editing || editOnly());
	}

	public boolean canEdit() {
		return (!hasBinding("canEdit") || ERXComponentUtilities.booleanValueForBinding(this, "canEdit"));
	}

	public boolean canSave() {
		return (!hasBinding("canSave") || ERXComponentUtilities.booleanValueForBinding(this, "canSave"));
	}
	
	public boolean changingToEdit() {
		return _changingToEdit;
	}
	
	public boolean changingToView() {
		return _changingToView;
	}

	public void setEditing(boolean editing) {
		if (canSetValueForBinding("editing")) {
			setValueForBinding(Boolean.valueOf(editing), "editing");
			if (hasBinding("editing")) {
				editing = ERXComponentUtilities.booleanValueForBinding(this, "editing");
			}
		}
		if (_editing != editing) {
			_editing = editing;
			if (_editing) {
				_changingToEdit = true;
			}
			else {
				_changingToView = true;
			}
		}
	}

	public String cleanupFunction() {
		//<script>AjaxInPlace.cleanupEdit('sheetSetHeaderWrapperSave', 'sheetSetHeaderWrapperCancel');</script>
		//<script>AjaxInPlace.cleanupEdit('sheetSetHeaderWrapper');</script>
		
		return null;
	}
	public WOActionResults startEditing() {
		if (canEdit()) {
			setEditing(true);
			valueForBinding("editAction");	// ignore results
		}
		return null;
	}

	public WOActionResults save() {
		// check to see if we can save before firing the action (for permissions)
		WOActionResults results = null;
		boolean canSave = canSave();
		if (canSave) {
			if (hasBinding("saveAction")) {
				results = (WOActionResults) valueForBinding("saveAction");
				canSave = canSave();
			}
			// check to see if we can save after firing the action (in case validation failed or something)
			if (canSave && !editOnly()) {
				setEditing(false);
			}
		}
		// ignore results
		return results;
	}

	public WOActionResults cancel() {
		WOActionResults results = (WOActionResults) valueForBinding("cancelAction");
		if (!editOnly()) {
			setEditing(false);
		}
		// ignore results
		return results;
	}
}
