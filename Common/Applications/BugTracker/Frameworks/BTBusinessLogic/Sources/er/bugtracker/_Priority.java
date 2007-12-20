// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Priority.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Priority extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Priority";

    public interface Key {
	// Attributes
	   public static final String SORT_ORDER = "sortOrder";
	   public static final String TEXT_DESCRIPTION = "textDescription";

	// Relationships
    }

    public static class _PriorityClazz extends ERXGenericRecord.ERXGenericRecordClazz<Priority> {
        /* more clazz methods here */
    }

  public Integer sortOrder() {
    return (Integer) storedValueForKey(Key.SORT_ORDER);
  }
  public void setSortOrder(Integer value) {
    takeStoredValueForKey(value, Key.SORT_ORDER);
  }

  public String textDescription() {
    return (String) storedValueForKey(Key.TEXT_DESCRIPTION);
  }
  public void setTextDescription(String value) {
    takeStoredValueForKey(value, Key.TEXT_DESCRIPTION);
  }

}
