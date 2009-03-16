// $LastChangedRevision: 7719 $ DO NOT EDIT.  Make changes to ERIAttributeGroup.java instead.
package er.indexing.attributes;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIAttributeGroup extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIAttributeGroup";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";

	// Relationships
	   public static final String ATTRIBUTES = "attributes";
	   public static final String CHILDREN = "children";
	   public static final String PARENT = "parent";
    }

    public static class _ERIAttributeGroupClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIAttributeGroup> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public er.indexing.attributes.ERIAttributeGroup parent() {
    return (er.indexing.attributes.ERIAttributeGroup)storedValueForKey(Key.PARENT);
  }
  public void setParent(er.indexing.attributes.ERIAttributeGroup value) {
    takeStoredValueForKey(value, Key.PARENT);
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

  public NSArray<er.indexing.attributes.ERIAttributeGroup> children() {
    return (NSArray<er.indexing.attributes.ERIAttributeGroup>)storedValueForKey(Key.CHILDREN);
  }
  public void addToChildren(er.indexing.attributes.ERIAttributeGroup object) {
      includeObjectIntoPropertyWithKey(object, Key.CHILDREN);
  }
  public void removeFromChildren(er.indexing.attributes.ERIAttributeGroup object) {
      excludeObjectFromPropertyWithKey(object, Key.CHILDREN);
  }

}
