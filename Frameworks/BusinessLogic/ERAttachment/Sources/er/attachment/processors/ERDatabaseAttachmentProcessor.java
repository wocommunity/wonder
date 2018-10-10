package er.attachment.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSTimestamp;

import er.attachment.model.ERAttachmentData;
import er.attachment.model.ERDatabaseAttachment;
import er.extensions.foundation.ERXProperties;

/**
 * ERDatabaseAttachmentProcessor implements storing attachment data as an attribute of an EO.  
 * For more information about configuring an ERDatabaseAttachmentProcessor, see the top level documentation.
 * 
 * @property er.attachment.[configurationName].db.webPath
 * @property er.attachment.db.webPath
 * @property er.attachment.[configurationName].db.smallData
 * @property er.attachment.db.smallData
 *
 * @author mschrag
 */
public class ERDatabaseAttachmentProcessor extends ERAttachmentProcessor<ERDatabaseAttachment> {
  @Override
  public ERDatabaseAttachment _process(EOEditingContext editingContext, File uploadedFile, String recommendedFileName, String mimeType, String configurationName, String ownerID, boolean pendingDelete) throws IOException {
	  NSData data = new NSData(uploadedFile.toURI().toURL());
	  ERDatabaseAttachment attachment = _process(editingContext, data, recommendedFileName, mimeType, configurationName);

	  if (pendingDelete) {
		  uploadedFile.delete();
	  }
	  return attachment;
  }
  
  public ERDatabaseAttachment _process(EOEditingContext editingContext, NSData data, String recommendedFileName, String mimeType, String configurationName) {
    String webPath = ERXProperties.stringForKey("er.attachment." + configurationName + ".db.webPath");
    if (webPath == null) {
      webPath = ERXProperties.stringForKeyWithDefault("er.attachment.db.webPath", "/${pk}${ext}");
    }
    if (webPath == null) {
      throw new IllegalArgumentException("There is no 'er.attachment." + configurationName + ".db.webPath' or 'er.attachment.db.webPath' property set.");
    }
    else if (!webPath.startsWith("/")) {
      webPath = "/" + webPath;
    }

    boolean smallData = false;
    String smallDataStr = ERXProperties.stringForKey("er.attachment." + configurationName + ".db.smallData");
    if (smallDataStr == null) {
      smallDataStr = ERXProperties.stringForKey("er.attachment.db.smallData");
    }
    if (smallDataStr != null) {
      smallData = Boolean.parseBoolean(smallDataStr);
    }

    ERDatabaseAttachment attachment = ERDatabaseAttachment.createERDatabaseAttachment(editingContext, Boolean.TRUE, new NSTimestamp(), mimeType, recommendedFileName, Boolean.TRUE, data.length(), webPath);
    if (delegate() != null) {
      delegate().attachmentCreated(this, attachment);
    }
    try {
      attachment.setWebPath(ERAttachmentProcessor._parsePathTemplate(attachment, webPath, recommendedFileName));
      if (smallData) {
        attachment.setSmallData(data);
      }
      else {
        ERAttachmentData attachmentData = ERAttachmentData.createERAttachmentData(editingContext);
        attachmentData.setData(data);
        attachment.setAttachmentDataRelationship(attachmentData);
      }
      if (delegate() != null) {
        delegate().attachmentAvailable(this, attachment);
      }
    }
    catch (RuntimeException e) {
      attachment.delete();
      throw e;
    }
    return attachment;
  }

  @Override
  public InputStream attachmentInputStream(ERDatabaseAttachment attachment) throws FileNotFoundException {
    NSData data = attachment.smallData();
    if (data == null) {
      ERAttachmentData attachmentData = attachment.attachmentData();
      if (attachmentData != null) {
        data = attachmentData.data();
      }
    }
    InputStream attachmentInputStream;
    if (data != null) {
      attachmentInputStream = data.stream();
    }
    else {
      throw new FileNotFoundException("There was no data available for this attachment.");
    }
    return attachmentInputStream;
  }

  @Override
  public String attachmentUrl(ERDatabaseAttachment attachment, WORequest request, WOContext context) {
    return proxiedUrl(attachment, context);
  }

  @Override
  public void deleteAttachment(ERDatabaseAttachment attachment) {
    // cascade delete rules do this for us ...
  }
}
