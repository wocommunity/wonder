package er.attachment.model;

import java.io.File;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

/**
 * ERS3Attachment (type = "s3") represents an attachment whose content is
 * stored on Amazon's S3 service and will be served directly from S3.  This
 * type may eventually support proxying as well, but currently only direct
 * links are enabled.
 * 
 * @author mschrag
 */
public class ERS3Attachment extends _ERS3Attachment {
  public static final String STORAGE_TYPE = "s3";
  private static Logger log = Logger.getLogger(ERS3Attachment.class);

  private File _pendingUploadFile;
  private boolean _pendingDelete;
  
  public ERS3Attachment() {
  }
  
  public void _setPendingUploadFile(File pendingUploadFile, boolean pendingDelete) {
    _pendingUploadFile = pendingUploadFile;
  }
  
  public File _pendingUploadFile() {
    return _pendingUploadFile;
  }
  
  public boolean _isPendingDelete() {
    return _pendingDelete;
  }

  @Override
  public void awakeFromInsertion(EOEditingContext ec) {
    super.awakeFromInsertion(ec);
    setStorageType(ERS3Attachment.STORAGE_TYPE);
  }
  
  /**
   * Sets the S3 location for this attachment.
   * 
   * @param bucket the S3 bucket
   * @param key the S3 key
   */
  public void setS3Location(String bucket, String key) {
    setWebPath("/" + bucket + "/" + key);
  }

  /**
   * @return the S3 bucket for this attachment.
   */
  public String bucket() {
    String[] paths = webPath().split("/");
    String bucket = paths[1];
    return bucket;
  }

  /**
   * @return the S3 key for this attachment.
   */
  public String key() {
    String[] paths = webPath().split("/");
    String key = paths[2];
    return key;
  }
}
