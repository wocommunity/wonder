// $LastChangedRevision: 7683 $ DO NOT EDIT.  Make changes to ERIAttribute.java instead.
package er.indexing.attributes;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIAttribute extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIAttribute";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";
	   public static final String POSSIBLE_VALUES = "possibleValues";
	   public static final String STORAGE_TYPE = "storageType";

	// Relationships
	   public static final String INDEXED_ATTRIBUTE_TYPE = "indexedAttributeType";
	   public static final String INDEXED_ENTITY = "indexedEntity";
    }

    public static class _ERIAttributeClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIAttribute> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public ERXMutableArray possibleValues() {
    return (ERXMutableArray) storedValueForKey(Key.POSSIBLE_VALUES);
  }
  public void setPossibleValues(ERXMutableArray value) {
    takeStoredValueForKey(value, Key.POSSIBLE_VALUES);
  }

  public Integer storageType() {
    return (Integer) storedValueForKey(Key.STORAGE_TYPE);
  }
  public void setStorageType(Integer value) {
    takeStoredValueForKey(value, Key.STORAGE_TYPE);
  }

  public er.indexing.attributes.ERIAttributeType indexedAttributeType() {
    return (er.indexing.attributes.ERIAttributeType)storedValueForKey(Key.INDEXED_ATTRIBUTE_TYPE);
  }
  public void setIndexedAttributeType(er.indexing.attributes.ERIAttributeType value) {
    takeStoredValueForKey(value, Key.INDEXED_ATTRIBUTE_TYPE);
  }

  public er.indexing.attributes.ERIEntity indexedEntity() {
    return (er.indexing.attributes.ERIEntity)storedValueForKey(Key.INDEXED_ENTITY);
  }
  public void setIndexedEntity(er.indexing.attributes.ERIEntity value) {
    takeStoredValueForKey(value, Key.INDEXED_ENTITY);
  }

}
