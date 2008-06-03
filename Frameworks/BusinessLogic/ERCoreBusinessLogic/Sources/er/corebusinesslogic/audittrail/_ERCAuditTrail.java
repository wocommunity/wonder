// $LastChangedRevision$ DO NOT EDIT.  Make changes to ERCAuditTrail.java instead.
package er.corebusinesslogic.audittrail;

import er.extensions.eof.ERXGenericRecord;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCAuditTrail extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERCAuditTrail";

    public interface Key {
	// Attributes
	   public static final String GID = "gid";
	   public static final String IS_DELETED = "isDeleted";

	// Relationships
	   public static final String ENTRIES = "entries";
    }

    public static class _ERCAuditTrailClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERCAuditTrail> {
        /* more clazz methods here */
    }

  public er.extensions.eof.ERXKeyGlobalID gid() {
    return (er.extensions.eof.ERXKeyGlobalID) storedValueForKey(Key.GID);
  }
  public void setGid(er.extensions.eof.ERXKeyGlobalID value) {
    takeStoredValueForKey(value, Key.GID);
  }

  public Boolean isDeleted() {
    return (Boolean) storedValueForKey(Key.IS_DELETED);
  }
  public void setIsDeleted(Boolean value) {
    takeStoredValueForKey(value, Key.IS_DELETED);
  }

  public NSArray<er.corebusinesslogic.audittrail.ERCAuditTrailEntry> entries() {
    return (NSArray<er.corebusinesslogic.audittrail.ERCAuditTrailEntry>)storedValueForKey(Key.ENTRIES);
  }
  public void addToEntries(er.corebusinesslogic.audittrail.ERCAuditTrailEntry object) {
      includeObjectIntoPropertyWithKey(object, Key.ENTRIES);
  }
  public void removeFromEntries(er.corebusinesslogic.audittrail.ERCAuditTrailEntry object) {
      excludeObjectFromPropertyWithKey(object, Key.ENTRIES);
  }

}
