// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Difficulty.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Difficulty extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Difficulty";

    public interface Key {
	// Attributes
	   public static final String DIFFICULTY_DESCRIPTION = "difficultyDescription";

	// Relationships
    }

    public static class _DifficultyClazz extends ERXGenericRecord.ERXGenericRecordClazz<Difficulty> {
        /* more clazz methods here */
    }

  public String difficultyDescription() {
    return (String) storedValueForKey(Key.DIFFICULTY_DESCRIPTION);
  }
  public void setDifficultyDescription(String value) {
    takeStoredValueForKey(value, Key.DIFFICULTY_DESCRIPTION);
  }

}
