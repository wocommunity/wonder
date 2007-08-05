package er.attachment.processors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.attachment.components.ERAttachmentRequestHandler;
import er.attachment.model.ERAttachment;
import er.attachment.model.ERDatabaseAttachment;
import er.attachment.model.ERFileAttachment;
import er.attachment.model.ERS3Attachment;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.ERXCrypto;
import er.extensions.ERXFileUtilities;

public abstract class ERAttachmentProcessor<T extends ERAttachment> {
  public static final String EXT_VARIABLE = "\\$\\{ext\\}";
  public static final String HASH_VARIABLE = "\\$\\{hash\\}";
  public static final String PK_VARIABLE = "\\$\\{pk\\}";
  public static final String FILE_NAME_VARIABLE = "\\$\\{fileName\\}";

  private static NSMutableDictionary<String, ERAttachmentProcessor<?>> _processors;

  public static synchronized NSMutableDictionary<String, ERAttachmentProcessor<?>> processors() {
    if (_processors == null) {
      _processors = new NSMutableDictionary<String, ERAttachmentProcessor<?>>();
      _processors.setObjectForKey(new ERDatabaseAttachmentProcessor(), ERDatabaseAttachment.STORAGE_TYPE);
      _processors.setObjectForKey(new ERS3AttachmentProcessor(), ERS3Attachment.STORAGE_TYPE);
      _processors.setObjectForKey(new ERFileAttachmentProcessor(), ERFileAttachment.STORAGE_TYPE);
    }
    return _processors;
  }

  public static <T extends ERAttachment> ERAttachmentProcessor<T> processorForType(T attachment) {
    return ERAttachmentProcessor.processorForType(attachment == null ? null : attachment.storageType());
  }

  @SuppressWarnings("unchecked")
  public static <T extends ERAttachment> ERAttachmentProcessor<T> processorForType(String type) {
    ERAttachmentProcessor<T> processor = (ERAttachmentProcessor<T>) ERAttachmentProcessor.processors().objectForKey(type);
    if (processor == null) {
      throw new IllegalArgumentException("There is no attachment processor for the type '" + type + "'.");
    }
    return processor;
  }

  public static synchronized void addAttachmentProcessorForType(ERAttachmentProcessor<?> processor, String type) {
    ERAttachmentProcessor.processors().setObjectForKey(processor, type);
  }

  public static String _parsePathTemplate(ERAttachment attachment, String templatePath, String recommendedFileName) {
    String parsedPath = templatePath;
    String ext = ERMimeTypeManager.primaryExtension(attachment.mimeType());
    if (ext == null) {
      ext = ERXFileUtilities.fileExtension(recommendedFileName);
    }
    if (ext != null) {
      parsedPath = parsedPath.replaceAll(ERAttachmentProcessor.EXT_VARIABLE, "." + ext);
    }
    else {
      parsedPath = parsedPath.replaceAll(ERAttachmentProcessor.EXT_VARIABLE, "");
    }

    String filenameHash = ERXCrypto.shaEncode(recommendedFileName);
    StringBuffer hashPathBuffer = new StringBuffer();
    hashPathBuffer.append(filenameHash.charAt(0));
    hashPathBuffer.append('/');
    hashPathBuffer.append(filenameHash.charAt(1));
    hashPathBuffer.append('/');
    hashPathBuffer.append(filenameHash.charAt(2));
    //hashPathBuffer.append('/');
    //hashPathBuffer.append(filenameHash.substring(3));
    parsedPath = parsedPath.replaceAll(ERAttachmentProcessor.HASH_VARIABLE, hashPathBuffer.toString());

    parsedPath = parsedPath.replaceAll(ERAttachmentProcessor.FILE_NAME_VARIABLE, recommendedFileName);
    parsedPath = parsedPath.replaceAll(ERAttachmentProcessor.PK_VARIABLE, attachment.primaryKeyInTransaction());
    return parsedPath;
  }

  protected String proxiedUrl(T attachment, WOContext context) {
    String webPath = attachment.webPath();
    if (webPath.startsWith("/")) {
      webPath = webPath.substring(1);
    }
    String attachmentUrl = context.urlWithRequestHandlerKey(ERAttachmentRequestHandler.REQUEST_HANDLER_KEY, webPath, null);
    return attachmentUrl;
  }

  public abstract T process(EOEditingContext editingContext, File uploadedFile, String recommendedFileName, String mimeType, String configurationName) throws IOException;

  public abstract InputStream attachmentInputStream(T attachment) throws IOException;

  public abstract String attachmentUrl(T attachment, WORequest request, WOContext context, String configurationName);
}