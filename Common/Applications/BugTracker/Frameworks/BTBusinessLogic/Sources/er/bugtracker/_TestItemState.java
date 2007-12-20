// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to TestItemState.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _TestItemState extends ERXGenericRecord {

	public static final String ENTITY_NAME = "TestItemState";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";
	   public static final String SORT_ORDER = "sortOrder";

	// Relationships
    }

    public static class _TestItemStateClazz extends ERXGenericRecord.ERXGenericRecordClazz<TestItemState> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public Integer sortOrder() {
    return (Integer) storedValueForKey(Key.SORT_ORDER);
  }
  public void setSortOrder(Integer value) {
    takeStoredValueForKey(value, Key.SORT_ORDER);
  }

}
