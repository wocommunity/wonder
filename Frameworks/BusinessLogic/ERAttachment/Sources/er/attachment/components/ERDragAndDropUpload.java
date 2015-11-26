package er.attachment.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.attachment.model.ERAttachment;
import er.attachment.model.ERDatabaseAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * A component designed to allow drag and drop uploads of ERAttachments. 
 * Except where otherwise noted, the javascript functions have a single 
 * argument: the event.
 * 
 * @binding accept an array of accepted mimetypes. If no mimetypes are specified, all are accepted. ex. (image/png, image/jpg, text/*)
 * @binding action the action to fire when an attachment is uploaded
 * @binding attachment the uploaded attachment
 * @binding completeAllFunction a javascript function to execute when all dropped files are processed
 * @binding completeFunction a javascript function to execute after each uploaded file is processed. This function accepts two args. The first is the event. The second is the file.
 * @binding configurationName the configuration name. See the package javadocs for more info.
 * @binding disabled if true, the upload component is disabled and only displays component content
 * @binding editingContext the editing context where the ERAttachments will be created
 * @binding enterFunction a javascript function to execute when dragged files enter the drop target
 * @binding errorFunction a javascript function to execute when a file upload error occurs. This function accepts two args. The first is the event. The second is the file.
 * @binding exitFunction a javascript function to execute when dragged files exit the drop target
 * @binding overFunction a javascript function to execute when dragged files are over the drop target
 * @binding storageType the ERAttachment storage type (db, file, s3, cf, ...)
 * 
 * @author Ramsey
 *
 */
public class ERDragAndDropUpload extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(ERDragAndDropUpload.class);

	private String dropTargetID;
	
	private boolean invokeAction = false;
	
	private boolean willAccept = true;
	
	private NSArray<String> accept;
	
	public ERDragAndDropUpload(WOContext context) {
        super(context);
    }

	/**
	 * @return the dropTargetID
	 */
	public String dropTargetID() {
		if(dropTargetID == null) {
			dropTargetID = ERXWOContext.safeIdentifierName(context(), true);
		}
		return dropTargetID;
	}

	@Override
	protected NSArray<String> additionalJavascriptFiles() {
		return new NSArray<String>("js/dndupload.js");
	}
	
	@Override
	protected String _frameworkName() {
		return "ERAttachment";
	}

	/**
	 * The ajax request URL for this component.
	 * @return the post URL for the ajax post request
	 */
	public String postURL() {
		String key = WOApplication.application().ajaxRequestHandlerKey();
		return context().componentActionURL(key);
	}
	
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if(invokeAction && willAccept) {
			invokeAction = false;
			return (WOActionResults) valueForBinding("action");
		} else if (invokeAction) {
			invokeAction = false;
			willAccept = true;
			return new ERXResponse(localizer().localizedStringForKey("UnacceptableMimetype"), ERXHttpStatusCodes.BAD_REQUEST); // CHECKME 406 or 415?
		}
		return super.invokeAction(request, context);
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		NSData data = (NSData) request.formValueForKey(dropTargetID());
		if(data != null) {
			String mimetype = (String) request.formValueForKey(dropTargetID() + ".mimetype");
			if(!accept().isEmpty() && !acceptMimetype(mimetype)) {
				// reject mime types that don't match
				invokeAction = true;
				willAccept = false;
			} else {
				String filename = (String) request.formValueForKey(dropTargetID() + ".filename");
				FileOutputStream os = null;
				try {
					File uploadedFile = File.createTempFile("DragAndDropUpload-", ".tmp");
					os = new FileOutputStream(uploadedFile);
					data.writeToStream(os);
					ERAttachment upload = ERAttachmentProcessor.processorForType(storageType()).process(editingContext(), uploadedFile, filename, mimetype, configurationName(), null);
					setValueForBinding(upload, "attachment");
					invokeAction = true;
					FileUtils.deleteQuietly(uploadedFile);
				} catch (IOException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				} finally {
					if(os != null) {
						try {
							os.close();
						} catch (IOException e) {
							log.error("Error closing file stream", e);
						}
					}
				}
			}
		}
		super.takeValuesFromRequest(request, context);
	}

	/**
	 * The ERAttachment storage type for the uploaded files.
	 * 
	 * @return the storageType
	 */
	public String storageType() {
		String key = configurationName() == null
				?"er.attachment.storageType"
				:"er.attachment." + configurationName() + ".storageType";
		String defaultType = ERXProperties.stringForKeyWithDefault(key, ERDatabaseAttachment.STORAGE_TYPE);
		return valueForStringBinding("storageType", defaultType);
	}
	
	/**
	 * The ERAttachment configuration name for the uploaded files.
	 * 
	 * @return the configurationName
	 */
	public String configurationName() {
		return (String) valueForBinding("configurationName");
	}

	/**
	 * The EOEditingContext where the ERAttachment will be created
	 */
	public EOEditingContext editingContext() {
		return (EOEditingContext) valueForBinding("editingContext");
	}

	public NSArray<String> accept() {
		if(accept == null) {
			String acceptString = (String) valueForBinding("accept");
			if(acceptString == null) {
				accept = NSArray.emptyArray();
			} else {
				// plist deserialization chokes on *
				if(acceptString.indexOf('*') > -1) {
					acceptString = acceptString.replace("*", "");
				}
				accept = NSPropertyListSerialization.arrayForString(acceptString);				
			}
		}
		return accept; 
	}
	
	//TODO optimize
	public boolean acceptMimetype(String mimetype) {
		NSArray<String> wildcards = ERXQ.endsWith("toString", "/").filtered(accept());
		for(String wildcard: wildcards) {
			if(mimetype.startsWith(wildcard)) {
				return true;
			}
		}
		NSArray<String> others = ERXArrayUtilities.arrayMinusArray(accept(), wildcards);
		for(String other: others) {
			if(mimetype.equals(other)) {
				return true;
			}
		}
		return false;
	}
}
