package er.attachment.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSData;

import er.attachment.model.ERAttachmentData;
import er.attachment.model.ERDatabaseAttachment;
import er.extensions.ERXProperties;

/**
 * ERDatabaseAttachmentProcessor implements storing attachment data as an attribute of an EO.  
 * For more information about configuring an ERDatabaseAttachmentProcessor, see the top level documentation.
 * 
 * @author mschrag
 */
public class ERDatabaseAttachmentProcessor extends ERAttachmentProcessor<ERDatabaseAttachment> {
  @Override
  public ERDatabaseAttachment _process(EOEditingContext editingContext, File uploadedFile, String recommendedFileName, String mimeType, String configurationName) throws IOException {
    String webPath = ERXProperties.stringForKey("er.attachment.db." + configurationName + ".webPath");
    if (webPath == null) {
      webPath = ERXProperties.stringForKeyWithDefault("er.attachment.db.webPath", "/${pk}${ext}");
    }
    if (webPath == null) {
      throw new IllegalArgumentException("There is no 'er.attachment.db." + configurationName + ".path' or 'er.attachment.db.path' property set.");
    }
    else if (!webPath.startsWith("/")) {
      webPath = "/" + webPath;
    }

    boolean smallData = false;
    String smallDataStr = ERXProperties.stringForKey("er.attachment.db." + configurationName + ".smallData");
    if (smallDataStr == null) {
      smallDataStr = ERXProperties.stringForKey("er.attachment.db.smallData");
    }
    if (smallDataStr != null) {
      smallData = Boolean.parseBoolean(smallDataStr);
    }

    ERDatabaseAttachment attachment = ERDatabaseAttachment.createERDatabaseAttachment(editingContext, mimeType, recommendedFileName, Boolean.TRUE, Integer.valueOf((int) uploadedFile.length()), webPath);
    try {
      attachment.setWebPath(ERAttachmentProcessor._parsePathTemplate(attachment, webPath, recommendedFileName));
      NSData data = new NSData(uploadedFile.toURL());
      if (smallData) {
        attachment.setSmallData(data);
      }
      else {
        ERAttachmentData attachmentData = ERAttachmentData.createERAttachmentData(editingContext);
        attachmentData.setData(data);
        attachment.setAttachmentDataRelationship(attachmentData);
      }
    }
    catch (IOException e) {
      attachment.delete();
      throw e;
    }
    catch (RuntimeException e) {
      attachment.delete();
      throw e;
    }
    finally {
      uploadedFile.delete();
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
  public String attachmentUrl(ERDatabaseAttachment attachment, WORequest request, WOContext context, String configurationName) {
    return proxiedUrl(attachment, context);
  }
}