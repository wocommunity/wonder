// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to RequirementType.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _RequirementType extends ERXGenericRecord {

	public static final String ENTITY_NAME = "RequirementType";

    public interface Key {
	// Attributes
	   public static final String TYPE_DESCRIPTION = "typeDescription";

	// Relationships
    }

    public static class _RequirementTypeClazz extends ERXGenericRecord.ERXGenericRecordClazz<RequirementType> {
        /* more clazz methods here */
    }

  public String typeDescription() {
    return (String) storedValueForKey(Key.TYPE_DESCRIPTION);
  }
  public void setTypeDescription(String value) {
    takeStoredValueForKey(value, Key.TYPE_DESCRIPTION);
  }

}
