// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERCStatic.java instead.
package er.corebusinesslogic;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCStatic extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERCStatic";

    public interface Key {
	// Attributes
	   public static final String KEY = "key";
	   public static final String VALUE = "value";

	// Relationships
    }

    public static class _ERCStaticClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERCStatic> {
        /* more clazz methods here */
    }

  public String key() {
    return (String) storedValueForKey(Key.KEY);
  }
  public void setKey(String value) {
    takeStoredValueForKey(value, Key.KEY);
  }

  public String value() {
    return (String) storedValueForKey(Key.VALUE);
  }
  public void setValue(String value) {
    takeStoredValueForKey(value, Key.VALUE);
  }

}
