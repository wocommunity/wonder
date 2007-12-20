// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERCPreference.java instead.
package er.corebusinesslogic;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCPreference extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERCPreference";

    public interface Key {
	// Attributes
	   public static final String KEY = "key";
	   public static final String USER_ID = "userID";
	   public static final String VALUE = "value";

	// Relationships
    }

    public static class _ERCPreferenceClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERCPreference> {
        /* more clazz methods here */
    }

  public String key() {
    return (String) storedValueForKey(Key.KEY);
  }
  public void setKey(String value) {
    takeStoredValueForKey(value, Key.KEY);
  }

  public Integer userID() {
    return (Integer) storedValueForKey(Key.USER_ID);
  }
  public void setUserID(Integer value) {
    takeStoredValueForKey(value, Key.USER_ID);
  }

  public String value() {
    return (String) storedValueForKey(Key.VALUE);
  }
  public void setValue(String value) {
    takeStoredValueForKey(value, Key.VALUE);
  }

}
