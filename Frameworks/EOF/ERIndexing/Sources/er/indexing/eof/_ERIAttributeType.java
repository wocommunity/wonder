// $LastChangedRevision: 7719 $ DO NOT EDIT.  Make changes to ERIAttributeType.java instead.
package er.indexing.eof;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIAttributeType extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIAttributeType";

    public interface Key {
	// Attributes
	   public static final String FORMAT = "format";
	   public static final String NAME = "name";
	   public static final String TYPE = "type";

	// Relationships
	   public static final String INDEXED_ATTRIBUTES = "indexedAttributes";
    }

    public static class _ERIAttributeTypeClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIAttributeType> {
        /* more clazz methods here */
    }

  public String format() {
    return (String) storedValueForKey(Key.FORMAT);
  }
  public void setFormat(String value) {
    takeStoredValueForKey(value, Key.FORMAT);
  }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public Integer type() {
    return (Integer) storedValueForKey(Key.TYPE);
  }
  public void setType(Integer value) {
    takeStoredValueForKey(value, Key.TYPE);
  }

  public NSArray<er.indexing.eof.ERIAttribute> indexedAttributes() {
    return (NSArray<er.indexing.eof.ERIAttribute>)storedValueForKey(Key.INDEXED_ATTRIBUTES);
  }
  public void addToIndexedAttributes(er.indexing.eof.ERIAttribute object) {
      includeObjectIntoPropertyWithKey(object, Key.INDEXED_ATTRIBUTES);
  }
  public void removeFromIndexedAttributes(er.indexing.eof.ERIAttribute object) {
      excludeObjectFromPropertyWithKey(object, Key.INDEXED_ATTRIBUTES);
  }

}
