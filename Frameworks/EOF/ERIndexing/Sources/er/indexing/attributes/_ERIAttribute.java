// $LastChangedRevision: 7719 $ DO NOT EDIT.  Make changes to ERIAttribute.java instead.
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
	   public static final String ATTRIBUTE_GROUP = "attributeGroup";
	   public static final String ATTRIBUTE_TYPE = "attributeType";
	   public static final String VALIDATION_RULES = "validationRules";
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

  public ERIStorageType storageType() {
    Number value = (Number)storedValueForKey(Key.STORAGE_TYPE);
    return (ERIStorageType)value;
  }
  public void setStorageType(ERIStorageType value) {
    takeStoredValueForKey(value, Key.STORAGE_TYPE);
  }

  public er.indexing.attributes.ERIAttributeGroup attributeGroup() {
    return (er.indexing.attributes.ERIAttributeGroup)storedValueForKey(Key.ATTRIBUTE_GROUP);
  }
  public void setAttributeGroup(er.indexing.attributes.ERIAttributeGroup value) {
    takeStoredValueForKey(value, Key.ATTRIBUTE_GROUP);
  }

  public er.indexing.attributes.ERIAttributeType attributeType() {
    return (er.indexing.attributes.ERIAttributeType)storedValueForKey(Key.ATTRIBUTE_TYPE);
  }
  public void setAttributeType(er.indexing.attributes.ERIAttributeType value) {
    takeStoredValueForKey(value, Key.ATTRIBUTE_TYPE);
  }

  public NSArray<er.indexing.attributes.ERIValidationRule> validationRules() {
    return (NSArray<er.indexing.attributes.ERIValidationRule>)storedValueForKey(Key.VALIDATION_RULES);
  }
  public void addToValidationRules(er.indexing.attributes.ERIValidationRule object) {
      includeObjectIntoPropertyWithKey(object, Key.VALIDATION_RULES);
  }
  public void removeFromValidationRules(er.indexing.attributes.ERIValidationRule object) {
      excludeObjectFromPropertyWithKey(object, Key.VALIDATION_RULES);
  }

}
