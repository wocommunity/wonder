// $LastChangedRevision$ DO NOT EDIT.  Make changes to Tag.java instead.
package er.indexing.example.eof;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Tag extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Tag";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";

	// Relationships
    }

    public static class _TagClazz extends ERXGenericRecord.ERXGenericRecordClazz<Tag> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

}
