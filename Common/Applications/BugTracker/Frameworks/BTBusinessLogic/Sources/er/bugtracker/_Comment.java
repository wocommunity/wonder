// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Comment.java instead.
package er.bugtracker;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Comment extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Comment";

    public interface Key {
	// Attributes
	   public static final String DATE_SUBMITTED = "dateSubmitted";
	   public static final String TEXT_DESCRIPTION = "textDescription";

	// Relationships
	   public static final String ATTACHEMENTS = "attachements";
	   public static final String BUG = "bug";
	   public static final String ORIGINATOR = "originator";
	   public static final String PARENT = "parent";
    }

    public static class _CommentClazz extends ERXGenericRecord.ERXGenericRecordClazz<Comment> {
        /* more clazz methods here */
    }

  public NSTimestamp dateSubmitted() {
    return (NSTimestamp) storedValueForKey(Key.DATE_SUBMITTED);
  }
  public void setDateSubmitted(NSTimestamp value) {
    takeStoredValueForKey(value, Key.DATE_SUBMITTED);
  }

  public String textDescription() {
    return (String) storedValueForKey(Key.TEXT_DESCRIPTION);
  }
  public void setTextDescription(String value) {
    takeStoredValueForKey(value, Key.TEXT_DESCRIPTION);
  }

  public er.bugtracker.Bug bug() {
    return (er.bugtracker.Bug)storedValueForKey(Key.BUG);
  }
  public void setBug(er.bugtracker.Bug value) {
    takeStoredValueForKey(value, Key.BUG);
  }

  public er.bugtracker.People originator() {
    return (er.bugtracker.People)storedValueForKey(Key.ORIGINATOR);
  }
  public void setOriginator(er.bugtracker.People value) {
    takeStoredValueForKey(value, Key.ORIGINATOR);
  }

  public er.bugtracker.Comment parent() {
    return (er.bugtracker.Comment)storedValueForKey(Key.PARENT);
  }
  public void setParent(er.bugtracker.Comment value) {
    takeStoredValueForKey(value, Key.PARENT);
  }

  public NSArray<er.bugtracker.Attachement> attachements() {
    return (NSArray<er.bugtracker.Attachement>)storedValueForKey(Key.ATTACHEMENTS);
  }
  public void addToAttachements(er.bugtracker.Attachement object) {
      includeObjectIntoPropertyWithKey(object, Key.ATTACHEMENTS);
  }
  public void removeFromAttachements(er.bugtracker.Attachement object) {
      excludeObjectFromPropertyWithKey(object, Key.ATTACHEMENTS);
  }

}
