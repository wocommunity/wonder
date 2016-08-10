package wowodc.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXUnmodeledToManyRelationship;

public class TaskInfo extends _TaskInfo {
	// The first saved workflow state is after initial creation and prime check has been performed
	public static String WORKFLOW_PROCESSING_PRIMES = "Processing Primes";
	
	// The first saved workflow state is after initial creation and prime check has been performed
	public static String WORKFLOW_PRIME_CHECKED = "Primes Processed";
	
	// The next saved state is the transient state while we are performing the factorial check
	public static String WORKFLOW_CHECKING_FACTORIAL = "Processing Factorials";
	
	// The next saved state is the completed all processing state while we are performing the factorial check
	public static String WORKFLOW_PROCESSING_COMPLETE = "Factorials Processed";
	
	private static NSArray<String> WORKFLOW_STATES = new NSArray( new String[] {
			WORKFLOW_PROCESSING_PRIMES,
			WORKFLOW_PRIME_CHECKED,
			WORKFLOW_CHECKING_FACTORIAL,
			WORKFLOW_PROCESSING_COMPLETE
	});
	
	@Override
	public void awakeFromInsertion(EOEditingContext editingContext) {
		super.awakeFromInsertion(editingContext);
		setWorkflowState(WORKFLOW_PROCESSING_PRIMES);
	}
	
	
	/**
	 * @return the rate of processing numbers 
	 */
	public Integer processingRate() {
		Long duration = duration();
		if (duration() == null) {
			return null;
		}
		long count = countResultItems();
		
		long result = count / (duration.longValue() / 1000l);
		
		return Integer.valueOf((int)result);
	}
	
	/**
	 * @return count of prime numbers found by this task
	 */
	public Integer countPrimes() {
		EOQualifier qPrime = ResultItem.XKEY_IS_PRIME.eq(Boolean.TRUE);
		EOQualifier qTaskInfo = ResultItem.XKEY_TASK_INFO.eq(this);
		
		return ERXEOControlUtilities.objectCountWithQualifier(editingContext(), ResultItem.ENTITY_NAME, ERXQ.and(qPrime, qTaskInfo));
	}
	
	public boolean isReadyForFactorialProcessing() {
		return (workflowState().equals(WORKFLOW_PRIME_CHECKED));
	}
	
	public boolean isFactorialProcessingDone() {
		return (workflowState().equals(WORKFLOW_PROCESSING_COMPLETE));
	}
	
	public double percentagePrimes() {
		double countPrimes = countPrimes().doubleValue();
		double countItems = countResultItems().doubleValue();
		
		return (countPrimes * 100) / countItems;
		
	}
	
	/**
	 * @return count of prime numbers found by this task
	 */
	public Integer countFactorialPrimes() {
		EOQualifier qFactorialPrime = ResultItem.XKEY_IS_FACTORIAL_PRIME.eq(Boolean.TRUE);
		EOQualifier qTaskInfo = ResultItem.XKEY_TASK_INFO.eq(this);
		
		return ERXEOControlUtilities.objectCountWithQualifier(editingContext(), ResultItem.ENTITY_NAME, ERXQ.and(qFactorialPrime, qTaskInfo));
	}
	
	
	// One-sided relationship resultItems
	// We avoid modeling here since
	//	1) For modifying large relationships, EOF will get quite slow when updating the relationship.
	//		For example in creating a relationship of about 12,000, the task was twice as fast with un-modeled to-many
	//	2) For our multi-threaded example, EOF will be unreliable with many threads changing the relationships at the same time.
	//			
	
	private ERXUnmodeledToManyRelationship<TaskInfo, ResultItem> _resultItemsRelationship;

	// Lazily initialize the helper class
	private ERXUnmodeledToManyRelationship<TaskInfo, ResultItem> resultItemsRelationship() {
		if (_resultItemsRelationship == null) {
			_resultItemsRelationship = new ERXUnmodeledToManyRelationship<TaskInfo, ResultItem>(this,
							ResultItem.ENTITY_NAME, ResultItem.XKEY_TASK_INFO);
		}
		return _resultItemsRelationship;
	}
	
	public Integer countResultItems() {
		return resultItemsRelationship().countObjects();
	}

	public EOQualifier qualifierForResultItems() {
		return resultItemsRelationship().qualifierForObjects();
	}

	public NSArray<ResultItem> resultItems() {
		return resultItemsRelationship().objects();
	}

	public ERXFetchSpecification<ResultItem> fetchSpecificationForResultItems() {
		return resultItemsRelationship().fetchSpecificationForObjects();
	}

	public NSArray<ResultItem> resultItems(EOQualifier qualifier) {
		return resultItemsRelationship().objects(qualifier);
	}

	public NSArray<ResultItem> resultItems(EOQualifier qualifier, boolean fetch) {
		return resultItemsRelationship().objects(qualifier, null, fetch);
	}

	public NSArray<ResultItem> resultItems(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		return resultItemsRelationship().objects(qualifier, sortOrderings, fetch);
	}

	public void addToResultItemsRelationship(ResultItem object) {
		resultItemsRelationship().addToObjectsRelationship(object);
	}

	public void removeFromResultItemsRelationship(ResultItem object) {
		resultItemsRelationship().removeFromObjectsRelationship(object);
	}

	public void deleteResultItemsRelationship(ResultItem object) {
		resultItemsRelationship().deleteObjectRelationship(object);
	}

	public void deleteAllResultItemsRelationships() {
		resultItemsRelationship().deleteAllObjectsRelationships();
	}
}
