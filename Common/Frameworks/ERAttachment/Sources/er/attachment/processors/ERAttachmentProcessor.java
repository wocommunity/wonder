package er.attachment.processors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.attachment.ERAttachmentRequestHandler;
import er.attachment.model.ERAttachment;
import er.attachment.model.ERDatabaseAttachment;
import er.attachment.model.ERFileAttachment;
import er.attachment.model.ERS3Attachment;
import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.ERXCrypto;
import er.extensions.ERXFileUtilities;
import er.extensions.ERXProperties;
import er.extensions.ERXValidationException;

/**
 * <p>
 * ERAttachmentProcessors provide the implementation of the communication with the
 * attachment storage method, including import, URL generation, and stream 
 * generation.
 * </p>
 * 
 * <p>
 * ERAttachmentProcessors also provide support for path template variables.  Read
 * the er.attachment package.html for more information.
 * </p>
 * 
 * @author mschrag
 *
 * @param <T> the type of ERAttachment that this processor processes
 * @property er.attachment.maxSize the maximum size of an uploaded attachment
 * @property er.attachment.[configurationName] maxSize the maximum size of an uploaded attachment
 */
public abstract class ERAttachmentProcessor<T extends ERAttachment> {
  public static final Logger log = Logger.getLogger(ERAttachmentProcessor.class);

  private static final String EXT_VARIABLE = "\\$\\{ext\\}";
  private static final String HASH_VARIABLE = "\\$\\{hash\\}";
  private static final String PK_VARIABLE = "\\$\\{pk\\}";
  private static final String FILE_NAME_VARIABLE = "\\$\\{fileName\\}";

  private static NSMutableDictionary<String, ERAttachmentProcessor<?>> _processors;

  /**
   * Returns all of the processors mapped by storageType.
   *  
   * @return all of the processors mapped by storageType
   */
  public static synchronized NSMutableDictionary<String, ERAttachmentProcessor<?>> processors() {
    if (_processors == null) {
      _processors = new NSMutableDictionary<String, ERAttachmentProcessor<?>>();
      _processors.setObjectForKey(new ERDatabaseAttachmentProcessor(), ERDatabaseAttachment.STORAGE_TYPE);
      _processors.setObjectForKey(new ERS3AttachmentProcessor(), ERS3Attachment.STORAGE_TYPE);
      _processors.setObjectForKey(new ERFileAttachmentProcessor(), ERFileAttachment.STORAGE_TYPE);
    }
    return _processors;
  }

  /**
   * Returns the processor that corresponds to the given attachment.
   * 
   * @param <T> the attachment type
   * @param attachment the attachment to lookup a processor for
   * @return the attachment's processor
   */
  public static <T extends ERAttachment> ERAttachmentProcessor<T> processorForType(T attachment) {
    return ERAttachmentProcessor.processorForType(attachment == null ? null : attachment.storageType());
  }

  /**
   * Returns the processor that corresponds to the given storage type ("s3", "db", "file", etc).
   * 
   * @param storageType the type of processor to lookup
   * @return the storage type's processor
   */
  @SuppressWarnings("unchecked")
  public static <T extends ERAttachment> ERAttachmentProcessor<T> processorForType(String storageType) {
    ERAttachmentProcessor<T> processor = (ERAttachmentProcessor<T>) ERAttachmentProcessor.processors().objectForKey(storageType);
    if (processor == null) {
      throw new IllegalArgumentException("There is no attachment processor for the type '" + storageType + "'.");
    }
    return processor;
  }

  /**
   * Adds a new attachment processor for the given storage type.
   * 
   * @param processor the processor
   * @param storageType the storage type that corresponds to the processor
   */
  public static synchronized void addAttachmentProcessorForType(ERAttachmentProcessor<?> processor, String storageType) {
    ERAttachmentProcessor.processors().setObjectForKey(processor, storageType);
  }

  /**
   * Parses a path template with ${ext}, ${fileName}, ${hash}, and ${pk} variables in it.  See the ERAttachment
   * top level documentation for more information.
   * 
   * @param attachment the attachment being processed
   * @param templatePath the template path definition
   * @param recommendedFileName the original file name recommended by the uploading user
   */
  protected static String _parsePathTemplate(ERAttachment attachment, String templatePath, String recommendedFileName) {
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

  /**
   * Returns a URL to the given attachment that routes via the ERAttachmentRequestHandler.
   * 
   * @param attachment the attachment to proxy
   * @param context the context
   * @return an ERAttachmentRequestHandler URL
   */
  protected String proxiedUrl(T attachment, WOContext context) {
    String webPath = attachment.webPath();
    if (webPath.startsWith("/")) {
      webPath = webPath.substring(1);
    }
    String attachmentUrl = context.urlWithRequestHandlerKey(ERAttachmentRequestHandler.REQUEST_HANDLER_KEY, webPath, null);
    return attachmentUrl;
  }

  /**
   * Processes an uploaded file, imports it into the appropriate data store, and returns an ERAttachment that
   * represents it.  uploadedFile will be deleted after the import process is complete.
   * 
   * @param editingContext the EOEditingContext to create the ERAttachment in
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the filename recommended by the user during import
   * @return an ERAttachment that represents the file
   * @throws IOException if the processing fails
   */
  public T process(EOEditingContext editingContext, File uploadedFile, String recommendedFilePath) throws IOException {
    return process(editingContext, uploadedFile, recommendedFilePath, null, null, null);
  }

  /**
   * Processes an uploaded file, imports it into the appropriate data store, and returns an ERAttachment that
   * represents it.  uploadedFile will be deleted after the import process is complete.
   * 
   * @param editingContext the EOEditingContext to create the ERAttachment in
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the filename recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * @return an ERAttachment that represents the file
   * @throws IOException if the processing fails
   */
  public T process(EOEditingContext editingContext, File uploadedFile, String recommendedFilePath, String mimeType) throws IOException {
    return process(editingContext, uploadedFile, recommendedFilePath, mimeType, null, null);
  }

  /**
   * Processes an uploaded file, imports it into the appropriate data store, and returns an ERAttachment that
   * represents it.  uploadedFile will be deleted after the import process is complete.
   * 
   * @param editingContext the EOEditingContext to create the ERAttachment in
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the filename recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * @param configurationName the name of the configuration settings to use for this processor (see top level docs) 
   * @param ownerID an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance) 
   * @return an ERAttachment that represents the file
   * @throws IOException if the processing fails
   */
  public T process(EOEditingContext editingContext, File uploadedFile, String recommendedFilePath, String mimeType, String configurationName, String ownerID) throws IOException {
    String recommendedFileName = ERXFileUtilities.fileNameFromBrowserSubmittedPath(recommendedFilePath);

    long maxSize = ERXProperties.longForKey("er.attachment." + configurationName + ".maxSize");
    if (maxSize == 0) {
      maxSize = ERXProperties.longForKeyWithDefault("er.attachment.maxSize", 0);
    }
    if (maxSize > 0 && uploadedFile.length() > maxSize) {
      uploadedFile.delete();
      ERXValidationException maxSizeExceededException = new ERXValidationException("AttachmentExceedsMaximumLengthException", uploadedFile, "size");
      maxSizeExceededException.takeValueForKey(Long.valueOf(maxSize), "maxSize");
      maxSizeExceededException.takeValueForKey(recommendedFileName, "recommendedFileName");
      throw maxSizeExceededException;
    }

    String suggestedMimeType = mimeType;
    if (suggestedMimeType == null) {
      if (suggestedMimeType == null) {
        String extension = ERXFileUtilities.fileExtension(recommendedFileName);
        ERMimeType erMimeType = ERMimeTypeManager.mimeTypeManager().mimeTypeForExtension(extension, false);
        if (erMimeType != null) {
          suggestedMimeType = erMimeType.mimeType();
        }
      }

      if (suggestedMimeType == null) {
        suggestedMimeType = "application/x-octet-stream";
      }
    }

    T attachment = _process(editingContext, uploadedFile, recommendedFileName, suggestedMimeType, configurationName, ownerID);
    attachment.setConfigurationName(configurationName);
    attachment.setOwnerID(ownerID);

    return attachment;
  }

  /**
   * Processes an uploaded file, imports it into the appropriate data store, and returns an ERAttachment that
   * represents it.  uploadedFile will be deleted after the import process is complete.
   * 
   * @param editingContext the EOEditingContext to create the ERAttachment in
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the filename recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * @param configurationName the name of the configuration settings to use for this processor (see top level docs)
   * @param ownerID an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance) 
   * @return an ERAttachment that represents the file
   * @throws IOException if the processing fails
   */
  public abstract T _process(EOEditingContext editingContext, File uploadedFile, String recommendedFileName, String mimeType, String configurationName, String ownerID) throws IOException;

  /**
   * Returns an InputStream to the data of the given attachment.
   * 
   * @param attachment the attachment to retrieve the data for
   * @return an InputStream onto the data
   * @throws IOException if the stream cannot be created
   */
  public abstract InputStream attachmentInputStream(T attachment) throws IOException;

  /**
   * Returns a URL to the attachment's data.
   * 
   * @param attachment the attachment to generate a URL for
   * @param request the current request
   * @param context the current context
   * @param configurationName the name of the configuration settings to use for this processor (see top level docs) 
   * @return a URL to the attachment's data
   */
  public abstract String attachmentUrl(T attachment, WORequest request, WOContext context);
  
  /**
   * Deletes the attachment from the data store.
   * 
   * @param attachment the attachment to delete
   * @throws IOException if the delete fails
   */
  public abstract void deleteAttachment(T attachment) throws IOException;
}