package er.attachment.processors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSData;

import er.attachment.model.ERAttachmentData;
import er.attachment.model.ERDatabaseAttachment;
import er.extensions.ERXProperties;

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

    ERAttachmentData attachmentData = ERAttachmentData.createERAttachmentData(editingContext);
    ERDatabaseAttachment attachment = ERDatabaseAttachment.createERDatabaseAttachment(editingContext, recommendedFileName, Boolean.TRUE, Integer.valueOf((int) uploadedFile.length()), webPath, attachmentData);
    try {
      attachment.setWebPath(ERAttachmentProcessor._parsePathTemplate(attachment, webPath, recommendedFileName));
      attachmentData.setData(new NSData(uploadedFile.toURL()));
    }
    catch (IOException e) {
      attachment.delete();
      throw e;
    }
    catch (RuntimeException e) {
      attachment.delete();
      throw e;
    }

    return attachment;
  }

  @Override
  public InputStream attachmentInputStream(ERDatabaseAttachment attachment) {
    return attachment.attachmentData().data().stream();
  }

  @Override
  public String attachmentUrl(ERDatabaseAttachment attachment, WORequest request, WOContext context, String configurationName) {
    return proxiedUrl(attachment, context);
  }
}