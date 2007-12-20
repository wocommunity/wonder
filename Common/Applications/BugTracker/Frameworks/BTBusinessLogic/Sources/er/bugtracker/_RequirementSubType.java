// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to RequirementSubType.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _RequirementSubType extends ERXGenericRecord {

	public static final String ENTITY_NAME = "RequirementSubType";

    public interface Key {
	// Attributes
	   public static final String SUB_TYPE_DESCRIPTION = "subTypeDescription";

	// Relationships
    }

    public static class _RequirementSubTypeClazz extends ERXGenericRecord.ERXGenericRecordClazz<RequirementSubType> {
        /* more clazz methods here */
    }

  public String subTypeDescription() {
    return (String) storedValueForKey(Key.SUB_TYPE_DESCRIPTION);
  }
  public void setSubTypeDescription(String value) {
    takeStoredValueForKey(value, Key.SUB_TYPE_DESCRIPTION);
  }

}
