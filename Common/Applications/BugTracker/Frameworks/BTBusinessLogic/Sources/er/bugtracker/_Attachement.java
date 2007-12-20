// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Attachement.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Attachement extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Attachement";

    public interface Key {
	// Attributes
	   public static final String FILE_NAME = "fileName";
	   public static final String MIME_TYPE = "mimeType";

	// Relationships
	   public static final String COMMENT = "comment";
    }

    public static class _AttachementClazz extends ERXGenericRecord.ERXGenericRecordClazz<Attachement> {
        /* more clazz methods here */
    }

  public String fileName() {
    return (String) storedValueForKey(Key.FILE_NAME);
  }
  public void setFileName(String value) {
    takeStoredValueForKey(value, Key.FILE_NAME);
  }

  public String mimeType() {
    return (String) storedValueForKey(Key.MIME_TYPE);
  }
  public void setMimeType(String value) {
    takeStoredValueForKey(value, Key.MIME_TYPE);
  }

  public er.bugtracker.Comment comment() {
    return (er.bugtracker.Comment)storedValueForKey(Key.COMMENT);
  }
  public void setComment(er.bugtracker.Comment value) {
    takeStoredValueForKey(value, Key.COMMENT);
  }

}
