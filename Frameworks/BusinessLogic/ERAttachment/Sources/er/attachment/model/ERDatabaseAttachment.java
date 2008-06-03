package er.attachment.model;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

/**
 * ERDatabaseAttachment (type "db") represents an attachment whose data
 * is stored in the database in an ERAttachmentData class.
 * 
 * @author mschrag
 */
public class ERDatabaseAttachment extends _ERDatabaseAttachment {
  public static final String STORAGE_TYPE = "db";
  private static Logger log = Logger.getLogger(ERDatabaseAttachment.class);

  public ERDatabaseAttachment() {
  }

  @Override
  public void awakeFromInsertion(EOEditingContext ec) {
    super.awakeFromInsertion(ec);
    setStorageType(ERDatabaseAttachment.STORAGE_TYPE);
  }
}
