package er.attachment.model;

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

  public ERS3Attachment() {
  }

  @Override
  public void awakeFromInsertion(EOEditingContext ec) {
    super.awakeFromInsertion(ec);
    setStorageType(ERS3Attachment.STORAGE_TYPE);
  }
}
