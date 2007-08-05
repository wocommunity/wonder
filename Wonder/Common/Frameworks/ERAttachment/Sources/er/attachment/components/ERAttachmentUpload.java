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
import er.extensions.ERXComponentUtilities;
import er.extensions.ERXProperties;

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
 * @author mschrag
 * @binding attachment the binding to store the newly created attachment in
 * @binding editingContext the editing context to create the attachment in
 * @binding storageType the type of attachment to create, i.e. "s3", "db", or "file" -- defaults to "db" (or the value of er.attachment.storageType)
 * @binding mimeType (optional) the mime type of the upload (will be guessed by extension if not set)
 * @binding ajax (optional) if true, AjaxFileUpload is used, if false WOFileUpload is used
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding ownerID (optional) a string ID of the "owner" of this attachment (Person.primaryKey for instance)
 * @binding others all AjaxFileUpload bindings are proxied
 * 
 * @property er.attachment.[configurationName].tempFolder (optional) the temp folder to use for WOFileUploads
 * @property er.attachment.tempFolder (optional) the temp folder to use for WOFileUploads
 */
public class ERAttachmentUpload extends WOComponent {
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
    if (context._wasFormSubmitted() && !ajax()) {
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
    
    File tempFile;
    if (tempFolderPath != null) {
      File tempFolder = new File(tempFolderPath);
      tempFile = File.createTempFile("ERAttachmentUpload-", ".tmp", tempFolder);
    }
    else {
      tempFile = File.createTempFile("ERAttachmentUpload-", ".tmp");
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
    
    ERAttachment attachment = ERAttachmentProcessor.processorForType(storageType).process(editingContext, uploadedFile, _filePath, mimeType, configurationName, ownerID);
    setValueForBinding(attachment, "attachment");
    return attachment;
  }

  public WOActionResults uploadSucceeded() throws MalformedURLException, IOException {
    ERAttachment attachment = _uploadSucceeded();
    return (WOActionResults) valueForBinding("succeededAction");
  }
}