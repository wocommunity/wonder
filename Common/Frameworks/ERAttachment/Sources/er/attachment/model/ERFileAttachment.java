package er.attachment.model;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

/**
 * ERFileAttachment (type = "file") represents an attachment whose
 * data is stored on the local filesystem.  An ERFileAttachment can
 * either be proxied or not.  If the attachment is not proxied, then
 * the backing file must be located in a folder that is reachable 
 * by your front-end webserver.  If the file is proxied, then the
 * data will be served via a custom request handler, and thus can
 * be written anywhere on the filesystem.
 * 
 * @author mschrag
 */
public class ERFileAttachment extends _ERFileAttachment {
  public static final String STORAGE_TYPE = "file";
  private static Logger log = Logger.getLogger(ERFileAttachment.class);

  public ERFileAttachment() {
  }

  @Override
  public void awakeFromInsertion(EOEditingContext editingContext) {
    super.awakeFromInsertion(editingContext);
    setStorageType(ERFileAttachment.STORAGE_TYPE);
  }
}
