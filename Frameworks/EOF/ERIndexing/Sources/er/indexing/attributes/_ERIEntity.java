// $LastChangedRevision: 7683 $ DO NOT EDIT.  Make changes to ERIEntity.java instead.
package er.indexing.attributes;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIEntity extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIEntity";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";

	// Relationships
	   public static final String INDEXED_ATTRIBUTES = "indexedAttributes";
    }

    public static class _ERIEntityClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIEntity> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public NSArray<er.indexing.attributes.ERIAttribute> indexedAttributes() {
    return (NSArray<er.indexing.attributes.ERIAttribute>)storedValueForKey(Key.INDEXED_ATTRIBUTES);
  }
  public void addToIndexedAttributes(er.indexing.attributes.ERIAttribute object) {
      includeObjectIntoPropertyWithKey(object, Key.INDEXED_ATTRIBUTES);
  }
  public void removeFromIndexedAttributes(er.indexing.attributes.ERIAttribute object) {
      excludeObjectFromPropertyWithKey(object, Key.INDEXED_ATTRIBUTES);
  }

}
