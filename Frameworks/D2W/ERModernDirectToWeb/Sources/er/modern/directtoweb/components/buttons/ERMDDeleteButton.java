package er.modern.directtoweb.components.buttons;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.delegates.ERDDeletionDelegate;
import er.directtoweb.delegates.ERDPageDelegate;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGuardedObjectInterface;
import er.extensions.localization.ERXLocalizer;

/**
 * Delete button for repetitions. 
 *
 * @binding object
 * @binding dataSource
 * @binding displayGroup
 * @binding d2wContext
 * 
 * @d2wKey deleteButtonLabel
 * @d2wKey classForDeleteObjButton
 * @d2wKey classForDisabledDeleteObjButton
 * @d2wKey cancelButtonLabel
 * @d2wKey classForCancelDialogButton
 * @d2wKey classForDeleteDialogButton
 * @d2wKey confirmDeleteMessage
 *
 * @author davidleber
 */
public class ERMDDeleteButton extends ERMDActionButton {
	
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ERMDDeleteButton.class);
	
	public final static String DisplayGroupObjectDeleted = "DisplayGroupObjectDeleted";
	
	public interface Keys extends ERMDActionButton.Keys {
		public static final String deleteButtonLabel = "deleteButtonLabel";
		public static final String classForDeleteObjButton = "classForDeleteObjButton";
		public static final String classForDisabledDeleteObjButton = "classForDisabledDeleteObjButton";
		public static final String objectPendingDeletion = "objectPendingDeletion";
		public static final String cancelButtonLabel = "cancelButtonLabel";
		public static final String classForCancelDialogButton = "classForCancelDialogButton";
		public static final String classForDeleteDialogButton = "classForDeleteDialogButton";
		public static final String confirmDeleteMessage = "confirmDeleteMessage";
	}
	
	private String _cancelButtonLabel;
	private String _cancelButtonClass;
	private String _deleteButtonClass;
	private String _localUpdateContainer;
	protected String _dialogMessage;
	protected String _disabledButtonClass;
	
	public ERMDDeleteButton(WOContext context) {
        super(context);
    }
	
	// ACTIONS
	
	/**
	 * Deletes the current object. Behaviour is dependent on the d2wContext useAjaxControls flag.
	 * 
	 * if true: Display an in-line confirmation dialog and update the pages main update container.
	 * if false: Take user to the confirmation page.
	 * 		
	 */
    public WOActionResults buttonAction() {
    	WOActionResults result = null;
    	if (shouldUseAjax()) {
    		EOEditingContext ec = ERXEC.newEditingContext(object().editingContext());
    		EOEnterpriseObject localObj = ERXEOControlUtilities.localInstanceOfObject(ec, object());
    		d2wContext().takeValueForKey(localObj, Keys.objectPendingDeletion);
    	} else {
    		ConfirmPageInterface nextPage = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed((String)valueForBinding("confirmDeleteConfigurationName"), session());
    		nextPage.setConfirmDelegate(new ERDDeletionDelegate(object(), dataSource(), context().page()));
    		nextPage.setCancelDelegate(new ERDPageDelegate(context().page()));
    		D2WPage d2wPage = ((D2WPage)nextPage);

    		String message = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERDTrashcan.confirmDeletionMessage", d2wContext()); 
    		nextPage.setMessage(message);
    		d2wPage.setObject(object());
    		result = (WOActionResults)nextPage;
    	}
        return result;
    }
    
    /**
     * Delete action for component button
     */
    public WOActionResults deleteAction() {
    	return deleteObjectWithFinalCommit(true);
    }
    
    /** 
     * Performs the in-line delete and purges object pending deletion from the d2wContext to hide the 
     * in-line confirmation dialog. Calls saveChanges on the parent ec if the finalCommit flag is true.
     */
    public WOActionResults deleteObjectWithFinalCommit(boolean finalCommit) {
    	EOEnterpriseObject obj = (EOEnterpriseObject)d2wContext().valueForKey(Keys.objectPendingDeletion);
        EODataSource ds = dataSource();
        
        // check whether the relationship is marked "owns destination"
        boolean isOwnsDestination = false;
        if (ds != null && ds instanceof EODetailDataSource) {
            EODetailDataSource dds = (EODetailDataSource) ds;
            EOClassDescription masterClassDescription = dds.masterClassDescription();
            isOwnsDestination = masterClassDescription
                    .ownsDestinationObjectsForRelationshipKey(dds.detailKey());
        }
        
    	try {
    	    // with EODetailDatasource, calling deleteObject
    	    // will only remove the object from the relationship
    	    ds.deleteObject(object());

    	    // for "owns destination" relationships, the following would
    	    // fail as the object will already be marked as deleted in the
    	    // parent EC
            if (!isOwnsDestination) {
                // actually delete the object in the nested EC
                obj.editingContext().deleteObject(obj);
                obj.editingContext().saveChanges();
            } 
	    	
	    	if (displayGroup() != null && displayGroup().displayedObjects().count() == 0) {
	    		displayGroup().displayPreviousBatch();
	    	}
            if (finalCommit && !ERXEOControlUtilities.isNewObject(object())) {
                // if we are editing a new object, then don't save
	    	    object().editingContext().saveChanges();
	    	}
	    	d2wContext().takeValueForKey(null, Keys.objectPendingDeletion);
	    	postDeleteNotification();
    	} catch(NSValidation.ValidationException e) {
    		parent().validationFailedWithException(e, e.object(), e.key());
    	}
    	return null;
    }
    
    /**
     * Reverts the ec, and purges the objectPendingDeletion in the d2wContext to hide the in-line 
     * confirmation dialog.
     */
    public WOActionResults cancelAction() {
    	EOEnterpriseObject obj = (EOEnterpriseObject)d2wContext().valueForKey(Keys.objectPendingDeletion);
    	obj.editingContext().revert();
    	d2wContext().takeValueForKey(null, Keys.objectPendingDeletion);
    	return null;
    }
    
    /**
     * Utility method to post the delete notification to the parent component
     */
    public void postDeleteNotification() {
    	Object obj = parentD2WPage();
    	String OBJECT_KEY = "object";
    	NSMutableDictionary<String, Object> userInfo = new NSMutableDictionary<String, Object>(obj, OBJECT_KEY);
		if (dataSource() instanceof EODetailDataSource) {
			EODetailDataSource dds = (EODetailDataSource)dataSource();
			userInfo.setObjectForKey(dds.masterObject(), OBJECT_KEY);
			userInfo.setObjectForKey(dds.detailKey(), "propertyKey");
		}
    	NSNotificationCenter.defaultCenter().postNotification(BUTTON_PERFORMED_DELETE_ACTION, obj, userInfo);
    }
	
    // OTHERS
    
    /**
     * Boolean used to hide/show the in-line confirm delete dialog.
     */
	public boolean canDelete() {
		return object() != null && object() instanceof ERXGuardedObjectInterface ? ((ERXGuardedObjectInterface)object()).canDelete() : true;
	}
	
	/**
	 * Label for the Delete button.
	 * <p>
         * Defaults to "Delete"
	 */
	public String buttonLabel() {
		if (_buttonLabel == null) {
			_buttonLabel = stringValueForBinding(Keys.deleteButtonLabel, "Delete");
		}
		return _buttonLabel;
	}

	/**
	 * Label for the Cancel button.
	 * <p>
         * Defaults to "Cancel"
	 */
    public String cancelButtonLabel() {
    	if (_cancelButtonLabel == null) {
			_cancelButtonLabel = stringValueForBinding(Keys.cancelButtonLabel, "Cancel");
		}
		return _cancelButtonLabel;
    }
    
    /**
     * CSS class for the Delete button.
     */
	public String buttonClass() {
		String result = null;
		if (canDelete() && !showDialog()) {
			result = activeButtonClass();
		} else {
			result = disabledButtonClass();
		}
		return result;
	}
	
	/**
	 * CSS class for the Delete button when active.
	 * <p>
	 * Defaults to "Button ObjButton DeleteObjButton"
	 */
	public String activeButtonClass() {
		if (_buttonClass == null) {
			_buttonClass = stringValueForBinding(Keys.classForDeleteObjButton, "Button ObjButton DeleteObjButton");
		}
		return _buttonClass;
	}
	
	/**
	 * CSS class for the delete button when disabled.
	 * <p>
	 * Defaults to "Button ObjButton DisabledObjButton DisabledDeleteObjButton"
	 */
	public String disabledButtonClass() {
		if (_disabledButtonClass == null) {
			_disabledButtonClass = stringValueForBinding(Keys.classForDisabledDeleteObjButton, "Button ObjButton DisabledObjButton DisabledDeleteObjButton");
		}
		return _disabledButtonClass;
	}
	
	/**
	 * CSS class for the in-line dialog's Cancel button.
	 * <p>
	 * Defaults to "Button DialogButton CancelDialogButton"
	 */
	public String cancelButtonClass() {
		if (_cancelButtonClass == null) {
			_cancelButtonClass = stringValueForBinding(Keys.classForCancelDialogButton, "Button DialogButton CancelDialogButton");
		}
		return _cancelButtonClass;
	}
	
	/** 
	 * CSS class for the in-line dialog's Delete button.
	 * <p>
	 * Defaults to "Button DialogButton DeleteDialogButton"
	 */
	public String deleteButtonClass() {
		if (_deleteButtonClass == null) {
			_deleteButtonClass = stringValueForBinding(Keys.classForDeleteDialogButton, "Button DialogButton DeleteDialogButton");
		}
		return _deleteButtonClass;
	}
	
    /**
     * Used to show/hide the confirmation dialog
     */
    public boolean showDialog() {
    	boolean show = object() != null && ERXEOControlUtilities.eoEquals(object(), (EOEnterpriseObject)d2wContext().valueForKey(Keys.objectPendingDeletion));
    	return show;
    }
    
    /**
     * Determines whether to use an in-line confirmation dialog with ajax behaviour or a separate
     * confirmation page.
     * 
     * Based on the value of the useAjax binding (or d2wContext key).
     */
    @Override
    public Boolean useAjax() {
    	if (_useAjax == null) {
			_useAjax = Boolean.valueOf(shouldUseAjax());
		}
		return _useAjax;
    }
	
    /**
     * Returns a unique id for this control's update container
     */
	public String localUpdateContainer() {
		if (_localUpdateContainer == null) {
			_localUpdateContainer = (String)valueForBinding("idForPropertyContainer");
			_localUpdateContainer = _localUpdateContainer + "_" + object().hashCode();
		}
		return _localUpdateContainer;
	}
	
    /**
     * String to display in the in-line confirmation dialog.
     * 
     * Obtained from the bindings or d2wContext via this key:
     * 
     * 		confirmDeleteMessage
     */
    public String dialogMessage() {
    	if (_dialogMessage == null) {
    		_dialogMessage = (String)valueForBinding(Keys.confirmDeleteMessage);
    	}
    	return _dialogMessage;
    }

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		EOEnterpriseObject eo = (EOEnterpriseObject) d2wContext().valueForKey(Keys.objectPendingDeletion);
		out.writeObject(eo);		
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		EOEnterpriseObject eo = (EOEnterpriseObject) in.readObject();
		d2wContext().takeValueForKey(eo, Keys.objectPendingDeletion);
	}
}
