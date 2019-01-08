// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to TaskInfo.java instead.
package wowodc.eof;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;

import er.extensions.eof.*;
import er.extensions.foundation.*;
@SuppressWarnings("all")
public abstract class _TaskInfo extends er.extensions.eof.ERXGenericRecord {
    public static final String ENTITY_NAME = "TaskInfo";

    public static final String ENTITY_TABLE_NAME = "taskinfo";

    // Attributes
    public static final String KEY_DURATION = "duration";
    public static final ERXKey<Long> XKEY_DURATION = new ERXKey<Long>(KEY_DURATION);
    public static final String KEY_END_NUMBER = "endNumber";
    public static final ERXKey<Long> XKEY_END_NUMBER = new ERXKey<Long>(KEY_END_NUMBER);
    public static final String KEY_END_TIME = "endTime";
    public static final ERXKey<NSTimestamp> XKEY_END_TIME = new ERXKey<NSTimestamp>(KEY_END_TIME);
    public static final String KEY_ID = "id";
    public static final ERXKey<Integer> XKEY_ID = new ERXKey<Integer>(KEY_ID);
    public static final String KEY_START_NUMBER = "startNumber";
    public static final ERXKey<Long> XKEY_START_NUMBER = new ERXKey<Long>(KEY_START_NUMBER);
    public static final String KEY_START_TIME = "startTime";
    public static final ERXKey<NSTimestamp> XKEY_START_TIME = new ERXKey<NSTimestamp>(KEY_START_TIME);
    public static final String KEY_WORKFLOW_STATE = "workflowState";
    public static final ERXKey<String> XKEY_WORKFLOW_STATE = new ERXKey<String>(KEY_WORKFLOW_STATE);

    // External Column Names capitalized (since EOF always returns raw row keys capitalized)
    public static final String COLKEY_DURATION = "duration".toUpperCase();
    public static final String COLKEY_END_NUMBER = "endnumber".toUpperCase();
    public static final String COLKEY_END_TIME = "endtime".toUpperCase();
    public static final String COLKEY_ID = "id".toUpperCase();
    public static final String COLKEY_START_NUMBER = "startnumber".toUpperCase();
    public static final String COLKEY_START_TIME = "starttime".toUpperCase();
    public static final String COLKEY_WORKFLOW_STATE = "workflowstate".toUpperCase();

    // Relationships

  private static Logger LOG = Logger.getLogger(_TaskInfo.class);

  public TaskInfo localInstanceIn(EOEditingContext editingContext) {
    TaskInfo localInstance = (TaskInfo)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  /**
   * The planned task duration in milliseconds.
   */
  public Long duration() {
    return (Long) storedValueForKey("duration");
  }
  
  /**
   * The planned task duration in milliseconds.
   */
  public void setDuration(Long value) {
    if (_TaskInfo.LOG.isDebugEnabled()) {
        _TaskInfo.LOG.debug( "updating duration from " + duration() + " to " + value);
    }
    takeStoredValueForKey(value, "duration");
  }

  /**
   * The last number in the sequence of numbers that we looked for prime numbers.
   */
  public Long endNumber() {
    return (Long) storedValueForKey("endNumber");
  }
  
  /**
   * The last number in the sequence of numbers that we looked for prime numbers.
   */
  public void setEndNumber(Long value) {
    if (_TaskInfo.LOG.isDebugEnabled()) {
        _TaskInfo.LOG.debug( "updating endNumber from " + endNumber() + " to " + value);
    }
    takeStoredValueForKey(value, "endNumber");
  }

  /**
   * The time we finished the task.
   */
  public NSTimestamp endTime() {
    return (NSTimestamp) storedValueForKey("endTime");
  }
  
  /**
   * The time we finished the task.
   */
  public void setEndTime(NSTimestamp value) {
    if (_TaskInfo.LOG.isDebugEnabled()) {
        _TaskInfo.LOG.debug( "updating endTime from " + endTime() + " to " + value);
    }
    takeStoredValueForKey(value, "endTime");
  }

  /**
   * The first number in the sequence of numbers that we looked for prime numbers.
   */
  public Long startNumber() {
    return (Long) storedValueForKey("startNumber");
  }
  
  /**
   * The first number in the sequence of numbers that we looked for prime numbers.
   */
  public void setStartNumber(Long value) {
    if (_TaskInfo.LOG.isDebugEnabled()) {
        _TaskInfo.LOG.debug( "updating startNumber from " + startNumber() + " to " + value);
    }
    takeStoredValueForKey(value, "startNumber");
  }

  /**
   * The time we started the task.
   */
  public NSTimestamp startTime() {
    return (NSTimestamp) storedValueForKey("startTime");
  }
  
  /**
   * The time we started the task.
   */
  public void setStartTime(NSTimestamp value) {
    if (_TaskInfo.LOG.isDebugEnabled()) {
        _TaskInfo.LOG.debug( "updating startTime from " + startTime() + " to " + value);
    }
    takeStoredValueForKey(value, "startTime");
  }

  public String workflowState() {
    return (String) storedValueForKey("workflowState");
  }
  
  public void setWorkflowState(String value) {
    if (_TaskInfo.LOG.isDebugEnabled()) {
        _TaskInfo.LOG.debug( "updating workflowState from " + workflowState() + " to " + value);
    }
    takeStoredValueForKey(value, "workflowState");
  }


  public static TaskInfo createTaskInfo(EOEditingContext editingContext, Long duration
, Long startNumber
, String workflowState
) {
    TaskInfo eo = (TaskInfo) EOUtilities.createAndInsertInstance(editingContext, _TaskInfo.ENTITY_NAME);
        eo.setDuration(duration);
        eo.setStartNumber(startNumber);
        eo.setWorkflowState(workflowState);
    return eo;
  }

  public static NSArray<TaskInfo> fetchAllTaskInfos(EOEditingContext editingContext) {
    return _TaskInfo.fetchAllTaskInfos(editingContext, null);
  }

  public static NSArray<TaskInfo> fetchAllTaskInfos(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _TaskInfo.fetchTaskInfos(editingContext, null, sortOrderings);
  }

  public static NSArray<TaskInfo> fetchTaskInfos(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_TaskInfo.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<TaskInfo> eoObjects = (NSArray<TaskInfo>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static TaskInfo fetchTaskInfo(EOEditingContext editingContext, String keyName, Object value) {
    return _TaskInfo.fetchTaskInfo(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static TaskInfo fetchTaskInfo(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<TaskInfo> eoObjects = _TaskInfo.fetchTaskInfos(editingContext, qualifier, null);
    TaskInfo eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (TaskInfo)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one TaskInfo that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static TaskInfo fetchRequiredTaskInfo(EOEditingContext editingContext, String keyName, Object value) {
    return _TaskInfo.fetchRequiredTaskInfo(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static TaskInfo fetchRequiredTaskInfo(EOEditingContext editingContext, EOQualifier qualifier) {
    TaskInfo eoObject = _TaskInfo.fetchTaskInfo(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no TaskInfo that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static TaskInfo localInstanceIn(EOEditingContext editingContext, TaskInfo eo) {
    TaskInfo localInstance = (eo == null) ? null : (TaskInfo)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }

    /**
     * This method is protected since they are to be called by eogenerated templates.
     * @param aKey which must be an existing valid relationship key
     * @return the qualifier for the relationship
     */
    private EOQualifier qualifierForRelationshipWithKey(String aKey)
    {
        ERXAssert.PRE.notEmpty(aKey);
        this.willRead();

        EOEntity anEntity = entity();
        EORelationship aRelationship = anEntity.relationshipNamed(aKey);

        ERXAssert.DURING.notNull(aRelationship);

        EOEditingContext anEditingContext = this.editingContext();
        EOGlobalID aGlobalID = anEditingContext.globalIDForObject(this);
        String aModelName = anEntity.model().name();
        EODatabaseContext aDatabaseContext = EOUtilities.databaseContextForModelNamed(anEditingContext,
                aModelName);
        NSDictionary aRow  = null;
        aDatabaseContext.lock();
        try {
        	aRow = aDatabaseContext.snapshotForGlobalID(aGlobalID);
		} finally {
			aDatabaseContext.unlock();
		}

        return aRelationship.qualifierWithSourceRow(aRow);
    }

    /**
     * This method is protected since they are to be called by eogenerated templates.
     * If object not saved, uses standard array count, otherwise uses database count
     *
     * @param key
     * @return count for the given relationship.
     */
    private Integer countForRelationship(String key)
    {
        if (editingContext().hasChanges()) {
            return (Integer) valueForKeyPath(key + ".@count");
        } else {
            EOQualifier qual = qualifierForRelationshipWithKey(key);

            EOEntity anEntity = entity();
            EORelationship aRelationship = entity().relationshipNamed(key);
            try {
				return ERXEOControlUtilities.objectCountWithQualifier(this.editingContext(), aRelationship
				        .destinationEntity().name(), qual);
			} catch (Exception e) {
				ToStringBuilder b = new ToStringBuilder(this);
				b.append("Failed to count relationship");
				b.append("Source Entity",anEntity);
				b.append("Source Relationship",key);

				throw new RuntimeException(b.toString(), e);
			}
        }
    }
}
