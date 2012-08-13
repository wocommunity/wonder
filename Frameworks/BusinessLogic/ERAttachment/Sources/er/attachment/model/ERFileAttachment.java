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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
