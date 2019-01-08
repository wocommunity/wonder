// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ResultItem.java instead.
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
public abstract class _ResultItem extends er.extensions.eof.ERXGenericRecord {
    public static final String ENTITY_NAME = "ResultItem";

    public static final String ENTITY_TABLE_NAME = "resultitem";

    // Attributes
    public static final String KEY_CLOSEST_FACTORIAL = "closestFactorial";
    public static final ERXKey<Long> XKEY_CLOSEST_FACTORIAL = new ERXKey<Long>(KEY_CLOSEST_FACTORIAL);
    public static final String KEY_FACTOR_NUMBER = "factorNumber";
    public static final ERXKey<Integer> XKEY_FACTOR_NUMBER = new ERXKey<Integer>(KEY_FACTOR_NUMBER);
    public static final String KEY_ID = "id";
    public static final ERXKey<Integer> XKEY_ID = new ERXKey<Integer>(KEY_ID);
    public static final String KEY_IS_FACTORIAL_PRIME = "isFactorialPrime";
    public static final ERXKey<Boolean> XKEY_IS_FACTORIAL_PRIME = new ERXKey<Boolean>(KEY_IS_FACTORIAL_PRIME);
    public static final String KEY_IS_PRIME = "isPrime";
    public static final ERXKey<Boolean> XKEY_IS_PRIME = new ERXKey<Boolean>(KEY_IS_PRIME);
    public static final String KEY_MODIFICATION_TIME = "modificationTime";
    public static final ERXKey<NSTimestamp> XKEY_MODIFICATION_TIME = new ERXKey<NSTimestamp>(KEY_MODIFICATION_TIME);
    public static final String KEY_NUMBER_TO_CHECK = "numberToCheck";
    public static final ERXKey<Long> XKEY_NUMBER_TO_CHECK = new ERXKey<Long>(KEY_NUMBER_TO_CHECK);
    public static final String KEY_TASK_INFO_ID = "taskInfoID";
    public static final ERXKey<Integer> XKEY_TASK_INFO_ID = new ERXKey<Integer>(KEY_TASK_INFO_ID);
    public static final String KEY_WORKFLOW_STATE = "workflowState";
    public static final ERXKey<String> XKEY_WORKFLOW_STATE = new ERXKey<String>(KEY_WORKFLOW_STATE);

    // External Column Names capitalized (since EOF always returns raw row keys capitalized)
    public static final String COLKEY_CLOSEST_FACTORIAL = "closestfactorial".toUpperCase();
    public static final String COLKEY_FACTOR_NUMBER = "factornumber".toUpperCase();
    public static final String COLKEY_ID = "id".toUpperCase();
    public static final String COLKEY_IS_FACTORIAL_PRIME = "isfactorialprime".toUpperCase();
    public static final String COLKEY_IS_PRIME = "isprime".toUpperCase();
    public static final String COLKEY_MODIFICATION_TIME = "modificationtime".toUpperCase();
    public static final String COLKEY_NUMBER_TO_CHECK = "numbertocheck".toUpperCase();
    public static final String COLKEY_TASK_INFO_ID = "taskinfoid".toUpperCase();
    public static final String COLKEY_WORKFLOW_STATE = "workflowstate".toUpperCase();

    // Relationships
    public static final String KEY_TASK_INFO = "taskInfo";
    public static final ERXKey<wowodc.eof.TaskInfo> XKEY_TASK_INFO = new ERXKey<wowodc.eof.TaskInfo>(KEY_TASK_INFO);

  private static Logger LOG = Logger.getLogger(_ResultItem.class);

  public ResultItem localInstanceIn(EOEditingContext editingContext) {
    ResultItem localInstance = (ResultItem)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  /**
   * The factorial result that is closest to <code>numberToChecl</code>
   */
  public Long closestFactorial() {
    return (Long) storedValueForKey("closestFactorial");
  }
  
  /**
   * The factorial result that is closest to <code>numberToChecl</code>
   */
  public void setClosestFactorial(Long value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating closestFactorial from " + closestFactorial() + " to " + value);
    }
    takeStoredValueForKey(value, "closestFactorial");
  }

  /**
   * The integer used to calculate the <code>closestFactorial</code>
   */
  public Integer factorNumber() {
    return (Integer) storedValueForKey("factorNumber");
  }
  
  /**
   * The integer used to calculate the <code>closestFactorial</code>
   */
  public void setFactorNumber(Integer value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating factorNumber from " + factorNumber() + " to " + value);
    }
    takeStoredValueForKey(value, "factorNumber");
  }

  /**
   * http://en.wikipedia.org/wiki/Factorial_prime
   */
  public Boolean isFactorialPrime() {
    return (Boolean) storedValueForKey("isFactorialPrime");
  }
  
  /**
   * http://en.wikipedia.org/wiki/Factorial_prime
   */
  public void setIsFactorialPrime(Boolean value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating isFactorialPrime from " + isFactorialPrime() + " to " + value);
    }
    takeStoredValueForKey(value, "isFactorialPrime");
  }

  public Boolean isPrime() {
    return (Boolean) storedValueForKey("isPrime");
  }
  
  public void setIsPrime(Boolean value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating isPrime from " + isPrime() + " to " + value);
    }
    takeStoredValueForKey(value, "isPrime");
  }

  /**
   * The date and time that this record was last saved.
   */
  public NSTimestamp modificationTime() {
    return (NSTimestamp) storedValueForKey("modificationTime");
  }
  
  /**
   * The date and time that this record was last saved.
   */
  public void setModificationTime(NSTimestamp value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating modificationTime from " + modificationTime() + " to " + value);
    }
    takeStoredValueForKey(value, "modificationTime");
  }

  /**
   * For the sake of demonstration, the primary key on this entity is a compund key made up of the FK from taskInfo and the numberToCheck attribute. This PK also serves as a constraint to avoid duplicate numberToCheck values per task.
   */
  public Long numberToCheck() {
    return (Long) storedValueForKey("numberToCheck");
  }
  
  /**
   * For the sake of demonstration, the primary key on this entity is a compund key made up of the FK from taskInfo and the numberToCheck attribute. This PK also serves as a constraint to avoid duplicate numberToCheck values per task.
   */
  public void setNumberToCheck(Long value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating numberToCheck from " + numberToCheck() + " to " + value);
    }
    takeStoredValueForKey(value, "numberToCheck");
  }

  public String workflowState() {
    return (String) storedValueForKey("workflowState");
  }
  
  public void setWorkflowState(String value) {
    if (_ResultItem.LOG.isDebugEnabled()) {
        _ResultItem.LOG.debug( "updating workflowState from " + workflowState() + " to " + value);
    }
    takeStoredValueForKey(value, "workflowState");
  }

	// BEGIN Methods associated with to-one relationships
    public wowodc.eof.TaskInfo taskInfo() {
        return (wowodc.eof.TaskInfo)storedValueForKey("taskInfo");
    }

    public void setTaskInfo(wowodc.eof.TaskInfo value) {
        takeStoredValueForKey(value, "taskInfo");
    }

    public void setTaskInfoRelationship(wowodc.eof.TaskInfo value) {
        if (_ResultItem.LOG.isDebugEnabled()) {
            _ResultItem.LOG.debug("updating taskInfo from " + taskInfo() + " to " + value);
        }
        if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
            setTaskInfo(value);
        }
        else if (value == null) {
            wowodc.eof.TaskInfo oldValue = taskInfo();
            if (oldValue != null) {
                removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "taskInfo");
            }
        } else {
            addObjectToBothSidesOfRelationshipWithKey(value, "taskInfo");
        }
    }
	// END Methods associated with to-one relationships
    
    
    

  public static ResultItem createResultItem(EOEditingContext editingContext, Boolean isFactorialPrime
, Boolean isPrime
, NSTimestamp modificationTime
, Long numberToCheck
, String workflowState
, wowodc.eof.TaskInfo taskInfo) {
    ResultItem eo = (ResultItem) EOUtilities.createAndInsertInstance(editingContext, _ResultItem.ENTITY_NAME);
        eo.setIsFactorialPrime(isFactorialPrime);
        eo.setIsPrime(isPrime);
        eo.setModificationTime(modificationTime);
        eo.setNumberToCheck(numberToCheck);
        eo.setWorkflowState(workflowState);
    eo.setTaskInfoRelationship(taskInfo);
    return eo;
  }

  public static NSArray<ResultItem> fetchAllResultItems(EOEditingContext editingContext) {
    return _ResultItem.fetchAllResultItems(editingContext, null);
  }

  public static NSArray<ResultItem> fetchAllResultItems(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ResultItem.fetchResultItems(editingContext, null, sortOrderings);
  }

  public static NSArray<ResultItem> fetchResultItems(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_ResultItem.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ResultItem> eoObjects = (NSArray<ResultItem>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static ResultItem fetchResultItem(EOEditingContext editingContext, String keyName, Object value) {
    return _ResultItem.fetchResultItem(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ResultItem fetchResultItem(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ResultItem> eoObjects = _ResultItem.fetchResultItems(editingContext, qualifier, null);
    ResultItem eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (ResultItem)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ResultItem that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ResultItem fetchRequiredResultItem(EOEditingContext editingContext, String keyName, Object value) {
    return _ResultItem.fetchRequiredResultItem(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ResultItem fetchRequiredResultItem(EOEditingContext editingContext, EOQualifier qualifier) {
    ResultItem eoObject = _ResultItem.fetchResultItem(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ResultItem that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ResultItem localInstanceIn(EOEditingContext editingContext, ResultItem eo) {
    ResultItem localInstance = (eo == null) ? null : (ResultItem)EOUtilities.localInstanceOfObject(editingContext, eo);
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
