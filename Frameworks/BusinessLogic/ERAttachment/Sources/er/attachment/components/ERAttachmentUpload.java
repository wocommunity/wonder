package er.attachment.components;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSForwardException;

import er.attachment.model.ERAttachment;
import er.attachment.model.ERDatabaseAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;

/**
 * <p>
 * ERAttachmentUpload provides a very simple wrapper around either a WOFileUpload
 * or an AjaxFileUpload component (depending on the value of the "ajax" binding).  
 * When the upload is successfully completed, this component will automatically
 * process the attachment.  It is not necessary to use this component -- it's 
 * only to make the process slightly easier.  If you want to use your own
 * existing file upload setup, in your completion action, you can simply call:
 * </p>
 * 
 * <code>
 * ERAttachment attachment = ERAttachmentProcessor.processorForType(storageType).process(editingContext, fileUploadFinalFilePath, fileUploadFilePath, mimeType, configurationName, ownerID);
 * </code>
 *
 * <p>
 * Note that for the attachment binding, you do not create the attachment instance and
 * pass it in. The attachment processor inside of ERAttachmentUpload creates an appropriate 
 * attachment instance for you (using the editing context you provide) and simply binds it 
 * back to you when the upload is complete.
 * </p>
 * 
 * @author mschrag
 * @binding attachment the binding to store the newly created attachment in
 * @binding editingContext the editing context to create the attachment in
 * @binding storageType the type of attachment to create, i.e. "s3", "db", or "file" -- defaults to "db" (or the value of er.attachment.storageType)
 * @binding mimeType (optional) the mime type of the upload (will be guessed by extension if not set)
 * @binding ajax (optional) if true, AjaxFileUpload is used, if false WOFileUpload is used
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding ownerID (optional) a string ID of the "owner" of this attachment (Person.primaryKey for instance)
 * @binding width (optional) the desired width of the attachment 
 * @binding height (optional) the desired height of the attachment 
 * @binding others all AjaxFileUpload bindings are proxied
 * @binding cleanup (optional) if true, the old attachment binding value will be deleted 
 * @binding class (optional) the class attribute of the file input
 * @binding style (optional) the style attribute of the file input
 * 
 * @property er.attachment.[configurationName].tempFolder the temp folder to use for WOFileUploads for a specific configuration. Default is er.attachment.tempFolder value.
 * @property er.attachment.tempFolder the temp folder to use for WOFileUploads. If not set, temporary dir will be set from File.createTempFile().
 * @property er.attachment.[configurationName].storageType
 * @property er.attachment.storageType
 * @property er.attachment.[configurationName].width
 * @property er.attachment.width
 * @property er.attachment.[configurationName].height
 * @property er.attachment.height
 *
 */
public class ERAttachmentUpload extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private String _filePath;
  private String _finalFilePath;

  public ERAttachmentUpload(WOContext context) {
    super(context);
  }

  public void setFilePath(String filePath) {
    _filePath = filePath;
  }

  public String filePath() {
    return _filePath;
  }

  public void setFinalFilePath(String finalFilePath) {
    _finalFilePath = finalFilePath;
  }

  public String finalFilePath() {
    return _finalFilePath;
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public boolean ajax() {
    return ERXComponentUtilities.booleanValueForBinding(this, "ajax");
  }

  @Override
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    WOActionResults results = super.invokeAction(request, context);
    if (context.wasFormSubmitted() && !ajax()) {
      try {
        _uploadSucceeded();
        valueForBinding("succeededAction");
      }
      catch (IOException e) {
        throw new NSForwardException(e, "Failed to process uploaded attachment.");
      }
    }
    return results;
  }

  public String tempFilePath() throws IOException {
    String configurationName = (String) valueForBinding("configurationName");
    String tempFolderPath = ERXProperties.stringForKey("er.attachment." + configurationName + ".tempFolder");
    if (tempFolderPath == null) {
      tempFolderPath = ERXProperties.stringForKey("er.attachment.tempFolder");
    }
    
    String fileExtension = ERXFileUtilities.fileExtension(_filePath);
    if (fileExtension == null) {
    	fileExtension = "tmp";
    }
    fileExtension = "." + fileExtension;
    
    File tempFile;
    if (tempFolderPath != null) {
      File tempFolder = new File(tempFolderPath);
      tempFile = File.createTempFile("ERAttachmentUpload-", fileExtension, tempFolder);
    }
    else {
      tempFile = File.createTempFile("ERAttachmentUpload-", fileExtension);
    }
    return tempFile.getAbsolutePath();
  }

  public ERAttachment _uploadSucceeded() throws IOException {
    if (_finalFilePath == null) {
      return null;
    }
    
    String configurationName = (String) valueForBinding("configurationName");
    
    String storageType = (String) valueForBinding("storageType");
    if (storageType == null) {
      storageType = ERXProperties.stringForKey("er.attachment." + configurationName + ".storageType");
      if (storageType == null) {
        storageType = ERXProperties.stringForKeyWithDefault("er.attachment.storageType", ERDatabaseAttachment.STORAGE_TYPE);
      }
    }
    
    EOEditingContext editingContext = (EOEditingContext) valueForBinding("editingContext");
    File uploadedFile = new File(_finalFilePath);

    String mimeType = (String) valueForBinding("mimeType");
    
    String ownerID = (String) valueForBinding("ownerID");

    int width = ERXValueUtilities.intValueWithDefault(valueForBinding("width"), -1);
    if (width == -1) {
    	width = ERXProperties.intForKeyWithDefault("er.attachment." + configurationName + ".width", -1);
    	if (width == -1) {
    		width = ERXProperties.intForKeyWithDefault("er.attachment.width", -1);
    	}
    }
    int height = ERXValueUtilities.intValueWithDefault(valueForBinding("height"), -1);
    if (height == -1) {
    	height = ERXProperties.intForKeyWithDefault("er.attachment." + configurationName + ".height", -1);
    	if (height == -1) {
    		height = ERXProperties.intForKeyWithDefault("er.attachment.height", -1);
    	}
    }
    
    ERAttachment attachment = ERAttachmentProcessor.processorForType(storageType).process(editingContext, uploadedFile, _filePath, mimeType, width, height, configurationName, ownerID);
    if (ERXComponentUtilities.booleanValueForBinding(this, "cleanup", false)) {
      ERAttachment oldAttachment = (ERAttachment) valueForBinding("attachment");
      if (oldAttachment != null) {
        oldAttachment.delete();
      }
    }
    
    setValueForBinding(attachment, "attachment");
    return attachment;
  }

  public WOActionResults uploadSucceeded() throws MalformedURLException, IOException {
    _uploadSucceeded();
    return (WOActionResults) valueForBinding("succeededAction");
  }
}
