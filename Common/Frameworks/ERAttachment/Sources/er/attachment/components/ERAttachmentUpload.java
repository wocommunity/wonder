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
import er.attachment.processors.ERAttachmentProcessor;
import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.ERXComponentUtilities;
import er.extensions.ERXFileUtilities;

public class ERAttachmentUpload extends WOComponent {
  private String _mimeType;
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
    File tempFile = File.createTempFile("ERAttachmentUpload-", ".tmp");
    return tempFile.getAbsolutePath();
  }
  
  public ERAttachment _uploadSucceeded() throws IOException {
    String type = type();

    EOEditingContext editingContext = (EOEditingContext) valueForBinding("editingContext");
    String mimeType = _mimeType;
    if (mimeType == null) {
      mimeType = (String) valueForBinding("mimeType");
    }

    if (mimeType == null) {
      String extension = ERXFileUtilities.fileExtension(_filePath);
      ERMimeType erMimeType = ERMimeTypeManager.mimeTypeManager().mimeTypeForExtension(extension, false);
      if (erMimeType != null) {
        mimeType = erMimeType.mimeType();
      }
    }

    if (mimeType == null) {
      mimeType = "application/x-octet-stream";
    }

    File uploadedFile = new File(_finalFilePath);
    String configurationName = (String) valueForBinding("configurationName");
    ERAttachmentProcessor<ERAttachment> attachmentProcessor = ERAttachmentProcessor.processorForType(type);
    ERAttachment attachment = attachmentProcessor.process(editingContext, uploadedFile, ERXFileUtilities.fileNameFromBrowserSubmittedPath(_filePath), mimeType, configurationName);
    setValueForBinding(attachment, "attachment");
    return attachment;
  }

  public WOActionResults uploadSucceeded() throws MalformedURLException, IOException {
    ERAttachment attachment = _uploadSucceeded();
    return (WOActionResults) valueForBinding("succeededAction");
  }

  public String type() {
    return (String) valueForBinding("type");
  }
}