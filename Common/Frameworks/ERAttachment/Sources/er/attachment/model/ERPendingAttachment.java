package er.attachment.model;

import java.io.File;

import er.extensions.ERXFileUtilities;

/**
 * ERPendingAttachment is just a convenience wrapper for tracking 
 * uploaded file state for later passing into an attachment processor.
 * 
 * @author mschrag
 */
public class ERPendingAttachment {
  private File _uploadedFile;
  private String _recommendedFilePath;
  private String _mimeType;
  private String _configurationName;
  private String _ownerID;
  private boolean _pendingDelete;

  /**
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   */
  public ERPendingAttachment(File uploadedFile) {
    this(uploadedFile, uploadedFile.getName(), null, null, null);
  }

  /**
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath) {
    this(uploadedFile, recommendedFilePath, null, null, null);
  }

  /**
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType) {
    this(uploadedFile, recommendedFilePath, mimeType, null, null);
  }

  /**
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * @param configurationName the name of the configuration settings to use for this processor (see top level docs) 
   * @param ownerID an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance) 
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType, String configurationName, String ownerID) {
    _uploadedFile = uploadedFile;
    if (recommendedFilePath == null) {
      _recommendedFilePath = _uploadedFile.getName();
    }
    else {
      _recommendedFilePath = recommendedFilePath;
    }
    _mimeType = mimeType;
    _configurationName = configurationName;
    _ownerID = ownerID;
    _pendingDelete = true;
  }

  /**
   * Returns the uploaded temporary file (which will be deleted at the end).
   * 
   * @return the uploaded temporary file
   */
  public File uploadedFile() {
    return _uploadedFile;
  }

  /**
   * Sets the uploaded temporary file (which will be deleted at the end).
   * 
   * @param uploadedFile the uploaded temporary file
   */
  public void setUploadedFile(File uploadedFile) {
    _uploadedFile = uploadedFile;
  }

  /**
   * Returns the path recommended by the user during import.
   * 
   * @return the path recommended by the user during import
   */
  public String recommendedFilePath() {
    return _recommendedFilePath;
  }

  /**
   * Sets the path recommended by the user during import.
   * 
   * @param recommendedFilePath the path recommended by the user during import
   */
  public void setRecommendedFilePath(String recommendedFilePath) {
    _recommendedFilePath = recommendedFilePath;
  }

  /**
   * Returns the cleansed file name recommended by the user during import.
   * 
   * @return the cleansed file name recommended by the user during import
   */
  public String recommendedFileName() {
    return ERXFileUtilities.fileNameFromBrowserSubmittedPath(_recommendedFilePath);
  }

  /**
   * Returns the mime type (or null if there isn't an explicit one) for this file.
   * 
   * @return the mime type (or null if there isn't an explicit one) for this file
   */
  public String mimeType() {
    return _mimeType;
  }

  /**
   * Sets the mime type (or null if there isn't an explicit one) for this file.
   * 
   * @param mimeType the mime type (or null if there isn't an explicit one) for this file
   */
  public void setMimeType(String mimeType) {
    _mimeType = mimeType;
  }

  /**
   * Returns the name of the configuration settings to use for this upload.
   * 
   * @return the name of the configuration settings to use for this upload
   */
  public String configurationName() {
    return _configurationName;
  }

  /**
   * Sets the name of the configuration settings to use for this upload.
   * 
   * @param configurationName the name of the configuration settings to use for this upload
   */
  public void setConfigurationName(String configurationName) {
    _configurationName = configurationName;
  }

  /**
   * Returns the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance).
   * 
   * @return the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance)
   */
  public String ownerID() {
    return _ownerID;
  }

  /**
   * Sets the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance).
   * 
   * @param ownerID the arbitrary string that represents the ID of the "owner" of this attachment
   */
  public void setOwnerID(String ownerID) {
    _ownerID = ownerID;
  }
  
  /**
   * Sets whether or not this attachment should be deleted after import.
   * 
   * @param pendingDelete whether or not this attachment should be deleted after import
   */
  public void setPendingDelete(boolean pendingDelete) {
    _pendingDelete = pendingDelete;
  }
  
  /**
   * Returns whether or not this attachment should be deleted after import.
   * @return whether or not this attachment should be deleted after import
   */
  public boolean isPendingDelete() {
    return _pendingDelete;
  }

  @Override
  public String toString() {
    return "[ERPendingAttachment: file = " + _uploadedFile + "]";
  }
}
