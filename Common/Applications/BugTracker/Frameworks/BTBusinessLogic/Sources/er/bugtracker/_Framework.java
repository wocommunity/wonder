// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Framework.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Framework extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Framework";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";
	   public static final String ORDERING = "ordering";
	   public static final String OWNED_SINCE = "ownedSince";

	// Relationships
	   public static final String OWNER = "owner";
    }

    public static class _FrameworkClazz extends ERXGenericRecord.ERXGenericRecordClazz<Framework> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public Integer ordering() {
    return (Integer) storedValueForKey(Key.ORDERING);
  }
  public void setOrdering(Integer value) {
    takeStoredValueForKey(value, Key.ORDERING);
  }

  public NSTimestamp ownedSince() {
    return (NSTimestamp) storedValueForKey(Key.OWNED_SINCE);
  }
  public void setOwnedSince(NSTimestamp value) {
    takeStoredValueForKey(value, Key.OWNED_SINCE);
  }

  public er.bugtracker.People owner() {
    return (er.bugtracker.People)storedValueForKey(Key.OWNER);
  }
  public void setOwner(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.OWNER);
  }

}
