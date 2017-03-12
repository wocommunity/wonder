package er.mootools.directtoweb.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * A D2W Component that wraps {@link ERAttachmentFlexibleEditor}
 * 
 * @binding object
 * @binding key
 * 
 * @d2wKey attachmentViewHeight - for the following see: {@link ERAttachmentFlexibleEditor}
 * @d2wKey attachmentViewWidth
 * @d2wKey attachmentInjectDefaultCSS
 * @d2wKey attachmentConfigurationName
 * @d2wKey attachmentEditorCancelLabel
 * @d2wKey attachmentEditorCancelLabel
 * @d2wKey attachmentEditorCancelButtonClass
 * @d2wKey attachmentEditorEditButtonClass
 * @d2wKey attachmentId
 * @d2wKey attachmentStorageType
 * @d2wKey attachmentUploadAllowCancel
 * @d2wKey attachmentUploadCancelButtonClass
 * @d2wKey attachmentUploadCancelLabel
 * @d2wKey attachmentUploadCanceledAction
 * @d2wKey attachmentUploadCanceledFunction
 * @d2wKey attachmentUploadCancelingText
 * @d2wKey attachmentUploadClearClass
 * @d2wKey attachmentUploadDialogHeaderText
 * @d2wKey attachmentUploadFailedAction
 * @d2wKey attachmentUploadFailedFunction
 * @d2wKey attachmentUploadFinishedFunction
 * @d2wKey attachmentUploadMimeType
 * @d2wKey attachmentUploadOwnerID
 * @d2wKey attachmentUploadHeight
 * @d2wKey attachmentUploadRefreshTime
 * @d2wKey attachmentUploadSelectFileButtonClass
 * @d2wKey attachmentUploadSelectFileLabel
 * @d2wKey attachmentUploadStartedFunction
 * @d2wKey attachmentUploadWidth
 * @d2wKey attachmentViewAllowDownload
 * @d2wKey attachmentViewShowAttachmentLink
 * @d2wKey attachmentViewShowFileName
 * 
 * @author dleber
 */
public class ERMTD2WEditAttachment extends ERDCustomEditComponent {

	public ERMTD2WEditAttachment(WOContext context) {
        super(context);
    }

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

}