// $LastChangedRevision: 7683 $ DO NOT EDIT.  Make changes to ERIAttributeType.java instead.
package er.indexing.attributes;

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
	   public static final String VALUE_TYPE = "valueType";

	// Relationships
	   public static final String ATTRIBUTES = "attributes";
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

  public er.indexing.attributes.ERIValueType valueType() {
    Number value = (Number)storedValueForKey(Key.VALUE_TYPE);
    return (er.indexing.attributes.ERIValueType)value;
  }
  public void setValueType(er.indexing.attributes.ERIValueType value) {
    takeStoredValueForKey(value, Key.VALUE_TYPE);
  }

  public NSArray<er.indexing.attributes.ERIAttribute> attributes() {
    return (NSArray<er.indexing.attributes.ERIAttribute>)storedValueForKey(Key.ATTRIBUTES);
  }
  public void addToAttributes(er.indexing.attributes.ERIAttribute object) {
      includeObjectIntoPropertyWithKey(object, Key.ATTRIBUTES);
  }
  public void removeFromAttributes(er.indexing.attributes.ERIAttribute object) {
      excludeObjectFromPropertyWithKey(object, Key.ATTRIBUTES);
  }

}
