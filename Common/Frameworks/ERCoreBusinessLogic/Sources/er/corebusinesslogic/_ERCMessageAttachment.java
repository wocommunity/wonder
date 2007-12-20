// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERCMessageAttachment.java instead.
package er.corebusinesslogic;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCMessageAttachment extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERCMessageAttachment";

    public interface Key {
	// Attributes
	   public static final String FILE_PATH = "filePath";
	   public static final String MIME_TYPE = "mimeType";

	// Relationships
	   public static final String MAIL_MESSAGE = "mailMessage";
    }

    public static class _ERCMessageAttachmentClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERCMessageAttachment> {
        /* more clazz methods here */
    }

  public String filePath() {
    return (String) storedValueForKey(Key.FILE_PATH);
  }
  public void setFilePath(String value) {
    takeStoredValueForKey(value, Key.FILE_PATH);
  }

  public String mimeType() {
    return (String) storedValueForKey(Key.MIME_TYPE);
  }
  public void setMimeType(String value) {
    takeStoredValueForKey(value, Key.MIME_TYPE);
  }

  public er.corebusinesslogic.ERCMailMessage mailMessage() {
    return (er.corebusinesslogic.ERCMailMessage)storedValueForKey(Key.MAIL_MESSAGE);
  }
  public void setMailMessage(er.corebusinesslogic.ERCMailMessage value) {
    takeStoredValueForKey(value, Key.MAIL_MESSAGE);
  }

}
