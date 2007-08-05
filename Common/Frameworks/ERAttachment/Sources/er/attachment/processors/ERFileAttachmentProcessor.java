package er.attachment.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.model.ERFileAttachment;
import er.extensions.ERXFileUtilities;
import er.extensions.ERXProperties;

/**
 * ERFileAttachmentProcessor implements storing attachments as files on the system that are either served
 * via a proxy request handler or directly by the webserver.  For more information about configuring
 * an ERFileAttachmentProcessor, see the top level documentation.
 * 
 * @author mschrag
 */
public class ERFileAttachmentProcessor extends ERAttachmentProcessor<ERFileAttachment> {
  @Override
  public ERFileAttachment _process(EOEditingContext editingContext, File uploadedFile, String recommendedFileName, String mimeType, String configurationName) throws IOException {
    boolean proxy = true;
    String proxyStr = ERXProperties.stringForKey("er.attachment.file." + configurationName + ".proxy");
    if (proxyStr == null) {
      proxyStr = ERXProperties.stringForKey("er.attachment.file.proxy");
    }
    if (proxyStr != null) {
      proxy = Boolean.parseBoolean(proxyStr);
    }

    boolean overwrite = false;
    String overwriteStr = ERXProperties.stringForKey("er.attachment.file." + configurationName + ".overwrite");
    if (overwriteStr == null) {
      overwriteStr = ERXProperties.stringForKey("er.attachment.file.overwrite");
    }
    if (overwriteStr != null) {
      overwrite = Boolean.parseBoolean(overwriteStr);
    }

    String filesystemPath = ERXProperties.stringForKey("er.attachment.file." + configurationName + ".filesystemPath");
    if (filesystemPath == null) {
      filesystemPath = ERXProperties.stringForKey("er.attachment.file.filesystemPath");
    }
    if (filesystemPath == null) {
      throw new IllegalArgumentException("There is no 'er.attachment.file." + configurationName + ".filesystemPath' or 'er.attachment.file.filesystemPath' property set.");
    }
    if (!filesystemPath.contains("${")) {
      filesystemPath = filesystemPath + "/attachments/${hash}/${pk}${ext}";
    }

    String webPath = ERXProperties.stringForKey("er.attachment.file." + configurationName + ".webPath");
    if (webPath == null) {
      webPath = ERXProperties.stringForKey("er.attachment.file.webPath");
    }
    if (webPath == null) {
      if (!proxy) {
        throw new IllegalArgumentException("There is no 'er.attachment.file." + configurationName + ".webPath' or 'er.attachment.file.webPath' property set.");
      }
      webPath = "/${pk}${ext}";
    }
    else if (!webPath.startsWith("/")) {
      webPath = "/" + webPath;
    }

    ERFileAttachment attachment = ERFileAttachment.createERFileAttachment(editingContext, mimeType, recommendedFileName, Boolean.valueOf(proxy), Integer.valueOf((int) uploadedFile.length()), webPath);
    try {
      webPath = ERAttachmentProcessor._parsePathTemplate(attachment, webPath, recommendedFileName);
      filesystemPath = ERAttachmentProcessor._parsePathTemplate(attachment, filesystemPath, recommendedFileName);

      File desiredFilesystemPath = new File(filesystemPath);
      File actualFilesystemPath = ERXFileUtilities.reserveUniqueFile(desiredFilesystemPath, overwrite);

      ERXFileUtilities.renameTo(uploadedFile, actualFilesystemPath);

      String desiredFileName = desiredFilesystemPath.getName();
      String actualFileName = actualFilesystemPath.getName();
      // in case the name was not unique and changed, we need to update webPath ...
      webPath = webPath.replace(desiredFileName + "$", actualFileName);

      attachment.setWebPath(webPath);
      attachment.setFilesystemPath(actualFilesystemPath.getAbsolutePath());
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
  public InputStream attachmentInputStream(ERFileAttachment attachment) throws IOException {
    return new FileInputStream(new File(attachment.filesystemPath()));
  }
  
  @Override
  public String attachmentUrl(ERFileAttachment attachment, WORequest request, WOContext context, String configurationName) {
    String attachmentUrl;
    if (!attachment.proxied().booleanValue()) {
      attachmentUrl = attachment.webPath();
    }
    else {
      attachmentUrl = proxiedUrl(attachment, context);
    }
    return attachmentUrl;
  }
}