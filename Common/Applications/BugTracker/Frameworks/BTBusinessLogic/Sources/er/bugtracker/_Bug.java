// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Bug.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Bug extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Bug";

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
	   public static final String ORIGINATOR = "originator";
	   public static final String OWNER = "owner";
	   public static final String PREVIOUS_OWNER = "previousOwner";
	   public static final String PRIORITY = "priority";
	   public static final String TARGET_RELEASE = "targetRelease";
	   public static final String TEST_ITEMS = "testItems";
    }

    public static class _BugClazz extends ERXGenericRecord.ERXGenericRecordClazz<Bug> {
        /* more clazz methods here */
    }

  public NSTimestamp dateModified() {
    return (NSTimestamp) storedValueForKey(Key.DATE_MODIFIED);
  }
  public void setDateModified(NSTimestamp value) {
    takeStoredValueForKey(value, Key.DATE_MODIFIED);
  }

  public NSTimestamp dateSubmitted() {
    return (NSTimestamp) storedValueForKey(Key.DATE_SUBMITTED);
  }
  public void setDateSubmitted(NSTimestamp value) {
    takeStoredValueForKey(value, Key.DATE_SUBMITTED);
  }

  public Boolean isFeatureRequest() {
    return (Boolean) storedValueForKey(Key.IS_FEATURE_REQUEST);
  }
  public void setIsFeatureRequest(Boolean value) {
    takeStoredValueForKey(value, Key.IS_FEATURE_REQUEST);
  }

  public Boolean isRead() {
    return (Boolean) storedValueForKey(Key.IS_READ);
  }
  public void setIsRead(Boolean value) {
    takeStoredValueForKey(value, Key.IS_READ);
  }

  public er.bugtracker.State state() {
    return (er.bugtracker.State) storedValueForKey(Key.STATE);
  }
  public void setState(er.bugtracker.State value) {
    takeStoredValueForKey(value, Key.STATE);
  }

  public String subject() {
    return (String) storedValueForKey(Key.SUBJECT);
  }
  public void setSubject(String value) {
    takeStoredValueForKey(value, Key.SUBJECT);
  }

  public String type() {
    return (String) storedValueForKey(Key.TYPE);
  }
  public void setType(String value) {
    takeStoredValueForKey(value, Key.TYPE);
  }

  public er.bugtracker.Component component() {
    return (er.bugtracker.Component)storedValueForKey(Key.COMPONENT);
  }
  public void setComponent(er.bugtracker.Component value) {
    takeStoredValueForKey(value, Key.COMPONENT);
  }

  public er.bugtracker.People originator() {
    return (er.bugtracker.People)storedValueForKey(Key.ORIGINATOR);
  }
  public void setOriginator(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.ORIGINATOR);
  }

  public er.bugtracker.People owner() {
    return (er.bugtracker.People)storedValueForKey(Key.OWNER);
  }
  public void setOwner(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.OWNER);
  }

  public er.bugtracker.People previousOwner() {
    return (er.bugtracker.People)storedValueForKey(Key.PREVIOUS_OWNER);
  }
  public void setPreviousOwner(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.PREVIOUS_OWNER);
  }

  public er.bugtracker.Priority priority() {
    return (er.bugtracker.Priority)storedValueForKey(Key.PRIORITY);
  }
  public void setPriority(er.bugtracker.Priority value) {
    takeStoredValueForKey(value, Key.PRIORITY);
  }

  public er.bugtracker.Release targetRelease() {
    return (er.bugtracker.Release)storedValueForKey(Key.TARGET_RELEASE);
  }
  public void setTargetRelease(er.bugtracker.Release value) {
    takeStoredValueForKey(value, Key.TARGET_RELEASE);
  }

  public NSArray<er.bugtracker.Comment> comments() {
    return (NSArray<er.bugtracker.Comment>)storedValueForKey(Key.COMMENTS);
  }
  public void addToComments(er.bugtracker.Comment object) {
      includeObjectIntoPropertyWithKey(object, Key.COMMENTS);
  }
  public void removeFromComments(er.bugtracker.Comment object) {
      excludeObjectFromPropertyWithKey(object, Key.COMMENTS);
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
