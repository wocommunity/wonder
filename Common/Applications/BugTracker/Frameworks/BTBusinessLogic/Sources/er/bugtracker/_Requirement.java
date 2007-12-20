// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Requirement.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Requirement extends er.bugtracker.Bug {

	public static final String ENTITY_NAME = "Requirement";

    public interface Key {
	// Attributes
	   public static final String DATE_MODIFIED = "dateModified";
	   public static final String DATE_SUBMITTED = "dateSubmitted";
	   public static final String IS_FEATURE_REQUEST = "isFeatureRequest";
	   public static final String IS_READ = "isRead";
	   public static final String STATE = "state";
	   public static final String SUBJECT = "subject";
	   public static final String TYPE = "type";

	// Relationships
	   public static final String COMMENTS = "comments";
	   public static final String COMPONENT = "component";
	   public static final String DIFFICULTY = "difficulty";
	   public static final String ORIGINATOR = "originator";
	   public static final String OWNER = "owner";
	   public static final String PREVIOUS_OWNER = "previousOwner";
	   public static final String PRIORITY = "priority";
	   public static final String REQUIREMENT_SUB_TYPE = "requirementSubType";
	   public static final String REQUIREMENT_TYPE = "requirementType";
	   public static final String TARGET_RELEASE = "targetRelease";
	   public static final String TEST_ITEMS = "testItems";
    }

    public static class _RequirementClazz extends Bug.BugClazz {
        /* more clazz methods here */
    }

  public er.bugtracker.Difficulty difficulty() {
    return (er.bugtracker.Difficulty)storedValueForKey(Key.DIFFICULTY);
  }
  public void setDifficulty(er.bugtracker.Difficulty value) {
    takeStoredValueForKey(value, Key.DIFFICULTY);
  }

  public er.bugtracker.RequirementSubType requirementSubType() {
    return (er.bugtracker.RequirementSubType)storedValueForKey(Key.REQUIREMENT_SUB_TYPE);
  }
  public void setRequirementSubType(er.bugtracker.RequirementSubType value) {
    takeStoredValueForKey(value, Key.REQUIREMENT_SUB_TYPE);
  }

  public er.bugtracker.RequirementType requirementType() {
    return (er.bugtracker.RequirementType)storedValueForKey(Key.REQUIREMENT_TYPE);
  }
  public void setRequirementType(er.bugtracker.RequirementType value) {
    takeStoredValueForKey(value, Key.REQUIREMENT_TYPE);
  }



}
