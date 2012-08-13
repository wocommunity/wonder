package er.attachment.components;

import com.webobjects.appserver.WOContext;

import er.ajax.AjaxFlexibleFileUpload;

/**
 * <p>
 * ERAttachmentFlexibleUpload provides a very simple wrapper around an AjaxFlexibleUpload
 * unlike {@link ERAttachmentUpload}, this component always uses ajax behaviour.
 * 
 * When the upload is successfully completed, this component will automatically
 * process the attachment.  See the notes in {@link ERAttachmentUpload} for details on how to 
 * process an attachment in your own components.
 * 
 * <p>
 * Note that for the attachment binding, you do not create the attachment instance and
 * pass it in. The attachment processor inside of ERAttachmentFlexibleUpload creates an appropriate 
 * attachment instance for you (using the editing context you provide) and simply binds it 
 * back to you when the upload is complete.
 * </p>
 * 
 * @author mschrag
 * @author dleber
 * 
 * @binding attachment the binding to store the newly created attachment in
 * @binding editingContext the editing context to create the attachment in
 * @binding storageType the type of attachment to create, i.e. "s3", "db", or "file" -- defaults to "db" (or the value of er.attachment.storageType)
 * @binding mimeType (optional) the mime type of the upload (will be guessed by extension if not set)
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding ownerID (optional) a string ID of the "owner" of this attachment (Person.primaryKey for instance)
 * @binding width (optional) the desired width of the attachment 
 * @binding height (optional) the desired height of the attachment 
 * @binding others all AjaxFileUpload bindings are proxied
 * @binding cleanup (optional) if true, the old attachment binding value will be deleted 
 * 
 * @binding allowCancel - for the following see: {@link AjaxFlexibleFileUpload} 
 * @binding cancelLabel
 * @binding canceledAction
 * @binding canceledFunction
 * @binding cancelingText
 * @binding failedAction
 * @binding failedFunction
 * @binding finishedAction
 * @binding finishedFunction
 * @binding refreshTime
 * @binding startedFunction
 * @binding succeededFunction
 * @binding autoSubmit
 * @binding injectDefaultCSS
 * @binding selectFileButtonClass
 * @binding selectFileLabel
 * @binding succeededAction
 * @binding cancelButtonClass
 * @binding clearButtonClass
 * @binding clearUploadProgressOnSuccess
 * 
 * @property er.attachment.[configurationName].tempFolder (optional) the temp folder to use for WOFileUploads
 * @property er.attachment.tempFolder (optional) the temp folder to use for WOFileUploads
 * @property er.attachment.[configurationName].storageType
 * @property er.attachment.storageType
 * @property er.attachment.[configurationName].width
 * @property er.attachment.width
 * @property er.attachment.[configurationName].height
 * @property er.attachment.height
 */
public class ERAttachmentFlexibleUpload extends ERAttachmentUpload {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
    public ERAttachmentFlexibleUpload(WOContext context) {
        super(context);
    }
    
    @Override
    // heck, we always be ajax'n!
    public boolean ajax() {
    	return true;
    }
    
}