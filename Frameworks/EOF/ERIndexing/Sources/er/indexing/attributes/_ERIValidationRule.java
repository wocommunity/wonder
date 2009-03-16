// $LastChangedRevision: 7719 $ DO NOT EDIT.  Make changes to ERIValidationRule.java instead.
package er.indexing.attributes;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIValidationRule extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIValidationRule";

    public interface Key {
	// Attributes
	   public static final String DEFINITION = "definition";
	   public static final String NAME = "name";

	// Relationships
	   public static final String ATTRIBUTES = "attributes";
    }

    public static class _ERIValidationRuleClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIValidationRule> {
        /* more clazz methods here */
    }

  public String definition() {
    return (String) storedValueForKey(Key.DEFINITION);
  }
  public void setDefinition(String value) {
    takeStoredValueForKey(value, Key.DEFINITION);
  }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
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
