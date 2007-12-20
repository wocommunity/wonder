// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Component.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Component extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Component";

    public interface Key {
	// Attributes
	   public static final String TEXT_DESCRIPTION = "textDescription";

	// Relationships
	   public static final String BUGS = "bugs";
	   public static final String CHILDREN = "children";
	   public static final String OWNER = "owner";
	   public static final String PARENT = "parent";
	   public static final String REQUIREMENTS = "requirements";
	   public static final String TEST_ITEMS = "testItems";
    }

    public static class _ComponentClazz extends ERXGenericRecord.ERXGenericRecordClazz<Component> {
        /* more clazz methods here */
    }

  public String textDescription() {
    return (String) storedValueForKey(Key.TEXT_DESCRIPTION);
  }
  public void setTextDescription(String value) {
    takeStoredValueForKey(value, Key.TEXT_DESCRIPTION);
  }

  public er.bugtracker.People owner() {
    return (er.bugtracker.People)storedValueForKey(Key.OWNER);
  }
  public void setOwner(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.OWNER);
  }

  public er.bugtracker.Component parent() {
    return (er.bugtracker.Component)storedValueForKey(Key.PARENT);
  }
  public void setParent(er.bugtracker.Component value) {
    takeStoredValueForKey(value, Key.PARENT);
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

  public NSArray<er.bugtracker.Component> children() {
    return (NSArray<er.bugtracker.Component>)storedValueForKey(Key.CHILDREN);
  }
  public void addToChildren(er.bugtracker.Component object) {
      includeObjectIntoPropertyWithKey(object, Key.CHILDREN);
  }
  public void removeFromChildren(er.bugtracker.Component object) {
      excludeObjectFromPropertyWithKey(object, Key.CHILDREN);
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

  public NSArray<er.bugtracker.TestItem> testItems() {
    return (NSArray<er.bugtracker.TestItem>)storedValueForKey(Key.TEST_ITEMS);
  }
  public void addToTestItems(er.bugtracker.TestItem object) {
      includeObjectIntoPropertyWithKey(object, Key.TEST_ITEMS);
  }
  public void removeFromTestItems(er.bugtracker.TestItem object) {
      excludeObjectFromPropertyWithKey(object, Key.TEST_ITEMS);
  }

}
