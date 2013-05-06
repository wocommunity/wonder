package er.attachment.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.ajax.AjaxUtils;
import er.attachment.model.ERAttachment;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * <p>
 * ERAttachmentFlexibleEditor is a wrapper around {@link ERAttachmentFlexibleUpload} and {@link ERAttachmentViewer}
 * It provides a one stop shop for editing a to-one relationship between a masterObject and
 * an ERAttachment. Switching back and forth between edit and view modes is handled by ajax updates.
 * </p>
 * 
 * <p>
 * There is one scenario where there is the potential for an orphaned ERAttachment and associated file. 
 * 1. User lands on an edit page for the masterObject
 * 2. User selects and successfully uploads a file for the attachment
 * 3. User leaves the masterObject edit page without saving changes
 * </p>
 * 
 * @binding masterObject (required) - Parent object owning the relationship to this attachment
 * @binding relationshipKey (required) - Name of the to-one relationship to the attachment
 * @binding injectDefaultCSS - inject the default stylesheet from the Ajax framework (defaults to true);
 * @binding id - unique identifier for this component (generated if null)
 * @binding editorEditLabel - label for the edit button (defaults to "Edit")
 * @binding editorEditButtonClass - css class for the edit button (defaults to "Button ObjButton EditObjButton")
 * @binding editorCancelLabel - label for the cancel button (defaults to "Cancel")
 * @binding editorCancelButtonClass - css class for the cancel button (defaults to "Button ObjButton CancelObjButton")
 * @binding configurationName - configuration name for configuring ERAttachment
 * @binding storageType - storage type for configuring ERAttachment
 * @binding viewShowFileName - show the attachment file name in the view mode (defaults to true)
 * @binding viewShowAttachmentLink - if viewShowFilename is true, wrap it with an ERAttachmentLink (defaults to true)
 * @binding uploadDialogHeaderText - the text of the upload header (defaults to "Edit Attachment")
 * 
 * @binding viewHeight - see: {@link ERAttachmentViewer}
 * @binding viewWidth - see: {@link ERAttachmentViewer}
 * 
 * @binding viewAllowDownload - see: {@link ERAttachmentLink}
 * 
 * @binding uploadAllowCancel - for the following see: {@link ERAttachmentFlexibleUpload}
 * @binding uploadFinishedFunction
 * @binding uploadCancelButtonClass
 * @binding uploadCancelLabel
 * @binding uploadCanceledAction
 * @binding uploadCanceledFunction
 * @binding uploadCancelingText
 * @binding uploadClearClass
 * @binding uploadFailedAction
 * @binding uploadFailedFunction
 * @binding uploadHeight
 * @binding uploadWidth
 * @binding uploadMimeType
 * @binding uploadOwnerID
 * @binding uploadRefreshTime
 * @binding uploadSelectFileButtonClass
 * @binding uploadSelectFileLabel
 * @binding uploadStartedFunction
 * 
 * @property er.attachment.[configurationName].tempFolder (optional) the temp folder to use for WOFileUploads
 * @property er.attachment.tempFolder (optional) the temp folder to use for WOFileUploads
 * @property er.attachment.[configurationName].storageType
 * @property er.attachment.storageType
 * @property er.attachment.[configurationName].width
 * @property er.attachment.width
 * @property er.attachment.[configurationName].height
 * @property er.attachment.height
 * 
 * @author david
 *
 */
public class ERAttachmentFlexibleEditor extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	protected final Logger log = Logger.getLogger(getClass());
	
	public static interface Keys {
		public static final String masterObject = "masterObject";
		public static final String relationshipKey = "relationshipKey";
		public static final String viewAllowDownload = "viewAllowDownload";
		public static final String viewShowFileName = "viewShowFileName";
		public static final String viewShowAttachmentLink = "viewShowAttachmentLink";
		public static final String uploadStartedFunction = "uploadStartedFunction";
		public static final String uploadFinishedFunction = "uploadFinishedFunction";
		public static final String uploadSucceededAction = "uploadSucceededAction";
		public static final String uploadAllowCancel = "uploadAllowCancel";
		public static final String uploadCancelLabel = "uploadCancelLabel";
		public static final String injectDefaultCSS = "injectDefaultCSS";
		public static final String editorEditButtonClass = "editorEditButtonClass";
		public static final String editorCancelButtonClass = "editorCancelButtonClass";
		public static final String editorEditLabel = "editorEditLabel";
		public static final String editorCancelLabel = "editorCancelLabel";
		public static final String uploadDialogHeaderText = "uploadDialogHeaderText";
	}
	
	private String _id;
	private ERAttachment _newAttachment;
	private String _uploadCancelLabel;
	private String _editorCancelButtonClass;
	private String _editorEditButtonClass;
	private String _editorEditLabel;
	private String _editorCancelLabel;
	private String _uploadDialogHeaderText;
	
	private boolean _showUpload;
	
    public ERAttachmentFlexibleEditor(WOContext context) {
        super(context);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	if (ERXComponentUtilities.booleanValueForBinding(this, Keys.injectDefaultCSS, true)) {
    		AjaxUtils.addStylesheetResourceInHead(context, response, "default_ajaxupload.css");
    	}
    	AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    	AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
    	AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
    	AjaxUtils.addScriptResourceInHead(context, response, "ajaxupload.js");
    }

    // ACTIONS
	
    /**
     * Action bound to the edit button
     * 
     * @return null
     */
	public WOActionResults editAttachment() {
		_showUpload = true;
		return null;
	}
	
	/**
	 * Action bound to the cancel button
	 * 
	 * @return null
	 */
	public WOActionResults cancelEdit() {
		_showUpload = false;
		return null;
	}
	
	/**
	 * Action called when an upload succeeds
	 * 
	 * @return results of the 
	 */
	public WOActionResults uploadSucceededAction() {

		EOEditingContext workingEC = masterObject().editingContext();
		ERAttachment existing = (ERAttachment)masterObject().valueForKey(relationshipKey());
		if (existing != null) {
			workingEC.deleteObject(existing.localInstanceIn(workingEC));
		}
		masterObject().addObjectToBothSidesOfRelationshipWithKey(newAttachment(), relationshipKey());
		
		_showUpload = false;
		return (WOActionResults)valueForBinding(Keys.uploadSucceededAction);
	}

	public WOActionResults finishedAction() {
		return null;
	}
	
	// STATE
	
	/**
	 * Controls whether the upload component is shown
	 * 
	 * @return boolean
	 */
	public boolean showUpload() {
		return viewerAttachment() == null || _showUpload;
	}
	

	// FOR UPLOADER
	
	public ERAttachment newAttachment() {
		return _newAttachment;
	}

	public void setNewAttachment(ERAttachment a) {
		_newAttachment = a;
	}
	
	/**
	 * EOEditingContext for the uploaded attachment
	 * 
	 * @return EOEditingContext
	 */
	public EOEditingContext attachmentEC() {
		return masterObject().editingContext();
	}
	
	public boolean allowCancel() {
		return booleanValueForBinding(Keys.uploadAllowCancel, true);
	}

	/**
	 * The label to apply to the upload components cancel button
	 * 
	 * Defaults to "Cancel Upload"
	 * 
	 * @return String the uploadCancelLabel
	 */
	public String uploadCancelLabel() {
		if (_uploadCancelLabel == null) {
			_uploadCancelLabel = stringValueForBinding(Keys.uploadCancelLabel, "Cancel Upload");
		}
		return _uploadCancelLabel;
	}
	
	// FOR VIEWER
	
	/**
	 * The masterObject's attachment
	 * 
	 * @return ERAttachment
	 */
	public ERAttachment viewerAttachment() {
		return (ERAttachment)masterObject().valueForKey(relationshipKey());
	}

	/**
	 * Controls whether the file name should be displayed
	 * 
	 * Defaults to true
	 * 
	 * @return boolean
	 */
	public boolean showFileName() {
		return booleanValueForBinding(Keys.viewShowFileName, true);
	}
	
	/**
	 * Controls whether the file name should be shown as a link
	 * 
	 * Defaults to true
	 * 
	 * @return boolean
	 */
	public boolean showLink() {
		boolean showAttachment = booleanValueForBinding(Keys.viewShowAttachmentLink, true);
		boolean isNew = viewerAttachment().isNewObject();
		return showAttachment && !isNew;
	}
	
	public boolean allowDownload() {
		return booleanValueForBinding(Keys.viewAllowDownload, true);
	}
	
	// FOR EDITOR

	/**
	 * The css class for the main edit button
	 * 
	 * Defaults to "Button ObjButton EditObjButton"
	 * 
	 * @return the editorEditButtonClass
	 */
	public String editorEditButtonClass() {
		if (_editorEditButtonClass == null) {
			_editorEditButtonClass = stringValueForBinding(Keys.editorEditButtonClass, "Button ObjButton EditObjButton");
		}
		return _editorEditButtonClass;
	}

	/**
	 * The css class for the main cancel button
	 * 
	 * Defaults to "Button ObjButton CancelObjButton"
	 * 
	 * @return the editorCancelButtonClass
	 */
	public String editorCancelButtonClass() {
		if (_editorCancelButtonClass == null) {
			_editorCancelButtonClass = stringValueForBinding(Keys.editorCancelButtonClass, "Button ObjButton CancelObjButton");
		}
		return _editorCancelButtonClass;
	}
	
	/**
	 * The label for the main edit button
	 * 
	 * Defaults to "Edit"
	 * 
	 * @return the editorEditLabel
	 */
	public String editorEditLabel() {
		if (_editorEditLabel == null) {
			_editorEditLabel = stringValueForBinding(Keys.editorEditLabel, "Edit");
		}
		return _editorEditLabel;
	}

	/**
	 * The label for the main cancel button
	 * 
	 * Defaults to "Cancel"
	 * 
	 * @return the _editorCancelLabel
	 */
	public String editorCancelLabel() {
		if (_editorCancelLabel == null) {
			_editorCancelLabel = stringValueForBinding(Keys.editorCancelLabel, "Cancel");
		}
		return _editorCancelLabel;
	}

	/**
	 * The text to display in the header of the edit box
	 * 
	 * @return the uploadDialogHeaderText
	 */
	public String uploadDialogHeaderText() {
		if (_uploadDialogHeaderText == null) {
			_uploadDialogHeaderText = stringValueForBinding(Keys.uploadDialogHeaderText, "Edit Attachment");
		}
		return _uploadDialogHeaderText;
	}
	
	// GENERIC ACCESSORS
	
	/**
	 * Getter for the masterObject
	 * 
	 * @return the masterObject
	 */
	public EOEnterpriseObject masterObject() {
		return (EOEnterpriseObject)valueForBinding(Keys.masterObject);
	}
	

	/**
	 * Getter for the relationhipKey
	 * 
	 * @return the relationshipKey
	 */
	public String relationshipKey() {
		return stringValueForBinding(Keys.relationshipKey);
	}
	
	/**
	 * Should the component inject the default css in the head of the page
	 * 
	 * @return boolean
	 */
	public boolean injectDefaultCSS() {
		return booleanValueForBinding(Keys.injectDefaultCSS, true);
	}

	// AJAX SUPPORT
	
	/**
	 * Base unique identifier, used to create the other id's in the component
	 * 
	 * @return String
	 */
    public String id() {
    	if (_id == null) {
			_id = stringValueForBinding("id", ERXWOContext.safeIdentifierName(context(), true));
		}
		return _id;
    }
    
    /**
     * Unique identifier for the main update container
     * 
     * @return String
     */
	public String updateContainerID() {
		return "AFEUC" + id();
	}

	/**
	 * Unique identifier for the cancel button wrapper div
	 * 
	 * @return String
	 */
	public String cancelButtonWrapperID() {
		return "AECB" + id();
	}
	
	/**
	 * Function to update the main update container
	 * 
	 * @return String
	 */
	public String refreshContainerFunction() {
		return updateContainerID() + "Update();";
	}
	
	/**
	 * Function called when the upload finishes
	 * 
	 * @return String
	 */
	public String uploadFinishedFunction() {
		String finishedFunction = stringValueForBinding(Keys.uploadFinishedFunction, "");
		String result = "function(e) { " + refreshContainerFunction() + " " + finishedFunction + " }";
		if (log.isDebugEnabled()) log.debug(result);
		return result;
	}
	
	/**
	 * Function called when the upload starts
	 * 
	 * @return String
	 */
	public String startedFunction() {
		String startedFunction = stringValueForBinding(Keys.uploadStartedFunction, "");
		String result =  "function(e) { $('" + cancelButtonWrapperID() + "').hide(); " + startedFunction + "}";
		if (log.isDebugEnabled()) log.debug(result);
		return result;
	}

}