// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERCLogEntry.java instead.
package er.corebusinesslogic;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCLogEntry extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERCLogEntry";

    public interface Key {
	// Attributes
	   public static final String CREATED = "created";
	   public static final String TEXT = "text";
	   public static final String USER_ID = "userID";

	// Relationships
    }

    public static class _ERCLogEntryClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERCLogEntry> {
        /* more clazz methods here */
    }

  public NSTimestamp created() {
    return (NSTimestamp) storedValueForKey(Key.CREATED);
  }
  public void setCreated(NSTimestamp value) {
    takeStoredValueForKey(value, Key.CREATED);
  }

  public String text() {
    return (String) storedValueForKey(Key.TEXT);
  }
  public void setText(String value) {
    takeStoredValueForKey(value, Key.TEXT);
  }

  public Integer userID() {
    return (Integer) storedValueForKey(Key.USER_ID);
  }
  public void setUserID(Integer value) {
    takeStoredValueForKey(value, Key.USER_ID);
  }

}
