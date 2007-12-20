// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Release.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Release extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Release";

    public interface Key {
	// Attributes
	   public static final String DATE_DUE = "dateDue";
	   public static final String IS_OPEN = "isOpen";
	   public static final String NAME = "name";

	// Relationships
	   public static final String BUGS = "bugs";
	   public static final String REQUIREMENTS = "requirements";
    }

    public static class _ReleaseClazz extends ERXGenericRecord.ERXGenericRecordClazz<Release> {
        /* more clazz methods here */
    }

  public NSTimestamp dateDue() {
    return (NSTimestamp) storedValueForKey(Key.DATE_DUE);
  }
  public void setDateDue(NSTimestamp value) {
    takeStoredValueForKey(value, Key.DATE_DUE);
  }

  public Boolean isOpen() {
    return (Boolean) storedValueForKey(Key.IS_OPEN);
  }
  public void setIsOpen(Boolean value) {
    takeStoredValueForKey(value, Key.IS_OPEN);
  }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public NSArray<er.bugtracker.Bug> bugs() {
    return (NSArray<er.bugtracker.Bug>)storedValueForKey(Key.BUGS);
  }
  public void addToBugs(er.bugtracker.Bug object) {
      includeObjectIntoPropertyWithKey(object, Key.BUGS);
  }
  public void removeFromBugs(er.bugtracker.Bug object) {
      excludeObjectFromPropertyWithKey(object, Key.BUGS);
  }

  public NSArray<er.bugtracker.Requirement> requirements() {
    return (NSArray<er.bugtracker.Requirement>)storedValueForKey(Key.REQUIREMENTS);
  }
  public void addToRequirements(er.bugtracker.Requirement object) {
      includeObjectIntoPropertyWithKey(object, Key.REQUIREMENTS);
  }
  public void removeFromRequirements(er.bugtracker.Requirement object) {
      excludeObjectFromPropertyWithKey(object, Key.REQUIREMENTS);
  }

}
