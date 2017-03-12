package er.attachment.model;

import com.webobjects.eocontrol.EOEditingContext;

/**
 * <span class="en">
 * ERDatabaseAttachment (type "db") represents an attachment whose data
 * is stored in the database in an ERAttachmentData class.
 * </span>
 * 
 * <span class="ja">
 * ERDatabaseAttachment (type "db") はアタッチメントがデータデータベース内
 * の ERAttachmentData クラスに保存されます。
 * </span>
 * 
 * @author mschrag
 */
public class ERDatabaseAttachment extends _ERDatabaseAttachment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public static final String STORAGE_TYPE = "db";

  public ERDatabaseAttachment() {
  }

  @Override
  public void awakeFromInsertion(EOEditingContext ec) {
    super.awakeFromInsertion(ec);
    setStorageType(ERDatabaseAttachment.STORAGE_TYPE);
  }
}
