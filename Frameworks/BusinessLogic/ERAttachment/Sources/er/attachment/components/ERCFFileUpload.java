package er.attachment.components;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpException;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSTimestamp;

import er.attachment.model.ERCloudFilesAttachment;
import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.components.ERXComponent;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;

public class ERCFFileUpload extends ERXComponent {

  private String _filePath;
  private String _finalFilePath;
  private String _configurationName;

  public ERCFFileUpload(WOContext context) {
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
  
  public String configurationName() {
    return _configurationName;
  }
  
  public void setConfigurationName(String configurationName) {
    _configurationName = configurationName;
  }
  
  public String container() {
    return (String)valueForBinding("container");
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
  
  public void _uploadSucceeded() throws IOException, FilesException, HttpException {
    if (_finalFilePath == null) {
      return;
    }
            
    File uploadedFile = new File(_finalFilePath);

    try {
      cloudFilesConnection().getContainerInfo(container());
    }
    catch (FilesNotFoundException e) {
      cloudFilesConnection().createContainer(container());
    } finally {
      String mimeType = mimeType(uploadedFile.getName());
      Long fileSize = Long.valueOf(uploadedFile.length());
      
      cloudFilesConnection().storeObjectAs(container(), uploadedFile, mimeType, NSPathUtilities.lastPathComponent(_filePath));
      URL urlToFile = new URL(cloudFilesConnection().getStorageURL() + "/" + container() + "/" + NSPathUtilities.lastPathComponent(_filePath));
      EOEditingContext editingContext = (EOEditingContext) valueForBinding("editingContext");
      
      ERCloudFilesAttachment attachment = ERCloudFilesAttachment.createERCloudFilesAttachment(editingContext, true, new NSTimestamp(), mimeType, NSPathUtilities.lastPathComponent(_filePath), true, fileSize.intValue(), urlToFile.getPath());
      attachment.setCfPath(urlToFile.toExternalForm());
      setValueForBinding(attachment, "attachment");
    }
        
  }

  private String mimeType(String recommendedFileName) {
    String suggestedMimeType = null;
    String extension = ERXFileUtilities.fileExtension(recommendedFileName);

    ERMimeType erMimeType = ERMimeTypeManager.mimeTypeManager().mimeTypeForExtension(extension, false);
    if (erMimeType != null) {
      suggestedMimeType = erMimeType.mimeType();
    }

    if (suggestedMimeType == null) {
      suggestedMimeType = "application/x-octet-stream";
    }
    
    return suggestedMimeType;
  }


  @Override
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    WOActionResults results = super.invokeAction(request, context);
    if (context.wasFormSubmitted()) {
      try {
        _uploadSucceeded();
      }
      catch (IOException e) {
        throw new NSForwardException(e, "Failed to process uploaded attachment.");
      }
      catch (FilesException e) {
        throw new NSForwardException(e, "Failed to process uploaded attachment.");
      }
      catch (HttpException e) {
        throw new NSForwardException(e, "Failed to process uploaded attachment.");
      }
    }
    return results;
  }
  
  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

}
