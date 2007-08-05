package er.attachment.model;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

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
