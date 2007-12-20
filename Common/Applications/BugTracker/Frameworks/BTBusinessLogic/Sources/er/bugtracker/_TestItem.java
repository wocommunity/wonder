// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to TestItem.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _TestItem extends ERXGenericRecord {

	public static final String ENTITY_NAME = "TestItem";

    public interface Key {
	// Attributes
	   public static final String COMMENTS = "comments";
	   public static final String CONTROLLED = "controlled";
	   public static final String DATE_CREATED = "dateCreated";
	   public static final String ID = "id";
	   public static final String TEXT_DESCRIPTION = "textDescription";
	   public static final String TITLE = "title";

	// Relationships
	   public static final String BUGS = "bugs";
	   public static final String COMPONENT = "component";
	   public static final String OWNER = "owner";
	   public static final String REQUIREMENTS = "requirements";
	   public static final String STATE = "state";
    }

    public static class _TestItemClazz extends ERXGenericRecord.ERXGenericRecordClazz<TestItem> {
        /* more clazz methods here */
    }

  public String comments() {
    return (String) storedValueForKey(Key.COMMENTS);
  }
  public void setComments(String value) {
    takeStoredValueForKey(value, Key.COMMENTS);
  }

  public String controlled() {
    return (String) storedValueForKey(Key.CONTROLLED);
  }
  public void setControlled(String value) {
    takeStoredValueForKey(value, Key.CONTROLLED);
  }

  public NSTimestamp dateCreated() {
    return (NSTimestamp) storedValueForKey(Key.DATE_CREATED);
  }
  public void setDateCreated(NSTimestamp value) {
    takeStoredValueForKey(value, Key.DATE_CREATED);
  }

  public Integer id() {
    return (Integer) storedValueForKey(Key.ID);
  }
  public void setId(Integer value) {
    takeStoredValueForKey(value, Key.ID);
  }

  public String textDescription() {
    return (String) storedValueForKey(Key.TEXT_DESCRIPTION);
  }
  public void setTextDescription(String value) {
    takeStoredValueForKey(value, Key.TEXT_DESCRIPTION);
  }

  public String title() {
    return (String) storedValueForKey(Key.TITLE);
  }
  public void setTitle(String value) {
    takeStoredValueForKey(value, Key.TITLE);
  }

  public er.bugtracker.Component component() {
    return (er.bugtracker.Component)storedValueForKey(Key.COMPONENT);
  }
  public void setComponent(er.bugtracker.Component value) {
    takeStoredValueForKey(value, Key.COMPONENT);
  }

  public er.bugtracker.People owner() {
    return (er.bugtracker.People)storedValueForKey(Key.OWNER);
  }
  public void setOwner(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.OWNER);
  }

  public er.bugtracker.TestItemState state() {
    return (er.bugtracker.TestItemState)storedValueForKey(Key.STATE);
  }
  public void setState(er.bugtracker.TestItemState value) {
    takeStoredValueForKey(value, Key.STATE);
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
