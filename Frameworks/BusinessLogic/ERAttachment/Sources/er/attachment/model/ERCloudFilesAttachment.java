package er.attachment.model;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.upload.ERRemoteAttachment;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXProperties;

/**
 * ERS3Attachment (type = "cf") represents an attachment whose content is stored on RackSpace's CloudFiles service and will be served directly from CloudFiles.
 * 
 * @author probert
 */
public class ERCloudFilesAttachment extends _ERCloudFilesAttachment implements ERRemoteAttachment {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(ERCloudFilesAttachment.class);
  public static final String STORAGE_TYPE = "cf";

  private File _pendingUploadFile;
  private boolean _pendingDelete;

  public void _setPendingUploadFile(File pendingUploadFile, boolean pendingDelete) {
    _pendingUploadFile = pendingUploadFile;
    _pendingDelete = pendingDelete;
  }

  public File _pendingUploadFile() {
    return _pendingUploadFile;
  }

  public boolean _isPendingDelete() {
    return _pendingDelete;
  }
  
  @Override
  public void didCopyFromChildInEditingContext(ERXGenericRecord originalEO, EOEditingContext childEditingContext) {
    super.didCopyFromChildInEditingContext(originalEO, childEditingContext);
    _setPendingUploadFile(((ERS3Attachment) originalEO)._pendingUploadFile(), false);
  }
  
  @Override
  public void awakeFromInsertion(EOEditingContext ec) {
    super.awakeFromInsertion(ec);
    setStorageType(ERCloudFilesAttachment.STORAGE_TYPE);
  }

  /**
   * @return the CloudFiles container for this attachment.
   */
  public String container() {
    String[] paths = webPath().split("/");
    String bucket = paths[paths.length - 2];
    return bucket;
  }

  /**
   * @return the CloudFiles key for this attachment.
   */
  public String key() {
    String[] paths = webPath().split("/");
    String key = paths[paths.length - 1];
    return key;
  }

  public String accessKeyID() {
    String accessKeyID = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".cf.apiAccessKey");
    if (accessKeyID == null) {
      accessKeyID = ERXProperties.decryptedStringForKey("er.attachment.cf.apiAccessKey");
    }
    if (accessKeyID == null) {
      throw new IllegalArgumentException("There is no 'er.attachment." + configurationName() + ".cf.apiAccessKey' or 'er.attachment.cf.apiAccessKey' property set.");
    }
    return accessKeyID;
  }
  
  public String username() {
    String username = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".cf.username");
    if (username == null) {
      username = ERXProperties.decryptedStringForKey("er.attachment.cf.username");
    }
    if (username == null) {
      throw new IllegalArgumentException("There is no 'er.attachment." + configurationName() + ".cf.username' or 'er.attachment.cf.username' property set.");
    }
    return username;
  }
  
  public String authUrl() {
    String authUrl = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".cf.authUrl");
    if (authUrl == null) {
      authUrl = ERXProperties.decryptedStringForKeyWithDefault("er.attachment.cf.authUrl", "https://auth.api.rackspacecloud.com/v1.0");
    }
    return authUrl;
  }
  
  public int connectionTimeOut() {
    String connectionTimeOut = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".cf.connectionTimeOut");
    if (connectionTimeOut == null) {
      connectionTimeOut = ERXProperties.decryptedStringForKeyWithDefault("er.attachment.cf.connectionTimeOut", "5000");
    }
    return Integer.valueOf(connectionTimeOut);
  }
  
  public String acl() {
    return "private";
  }
  
  public FilesClient cloudFilesConnection() {
    FilesClient conn = new FilesClient(username(), accessKeyID(), authUrl(), null, connectionTimeOut());
    try {
      conn.login();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (HttpException e) {
      e.printStackTrace();
    }
    return conn;
  }

}
