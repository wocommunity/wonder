package er.rest;

import com.webobjects.appserver.WORequest;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;

/**
 * <p>
 * ERXRestFetchSpecification provides a wrapper around fetching objects with batching, sort orderings, and (optionally)
 * qualifiers configured in the WORequest.
 * </p>
 * 
 * <p>
 * Example query string parameters:
 * </p>
 * <ul>
 * <li>sort=lastName|asc,firstName|desc</li>
 * <li>batchSize=25&batch=1</li>
 * <li>qualifier=firstName%3D'Mike'</li>
 * </ul>
 * 
 * <p>
 * Because request EOQualifiers could possibly pose a security risk, you must explicitly enable request qualifiers by
 * calling enableRequestQualifiers(baseQualifier) or by using the longer constructor that takes an optional base
 * qualifier. A base qualifier is prepended (AND'd) to whatever qualifier is passed on the query string to restrict the
 * results of the user's query.
 * </p>
 * 
 * <p>
 * An example use:
 * </p>
 * 
 * <pre>
 * public WOActionResults indexAction() throws Throwable {
 *     ERXRestFetchSpecification<Task> fetchSpec = new ERXRestFetchSpecification<Task>(Task.ENTITY_NAME, null, null, queryFilter(), Task.CREATION_DATE.descs(), 25);
 *     NSArray<Task> tasks = fetchSpec.objects(editingContext(), request());
 *     return response(editingContext(), Task.ENTITY_NAME, tasks, showFilter());
 * }
 * </pre>
 * 
 * <p>
 * In this example, we are fetching the "Task" entity, sorted by creation date, with a default batch size of 25, and
 * with request qualifiers enable (meaning, we allow users to pass in a qualifier in the query string), filtering the
 * qualifier with the ERXKeyFilter returned by the queryFilter() method. We then fetch the resulting tasks and return
 * the response to the user.
 * </p>
 * 
 * @author mschrag
 * 
 * @param <T>
 *            the type of the objects being returned
 */
public class ERXRestFetchSpecification<T extends EOEnterpriseObject> {
	// private WORequest _request;
	private String _entityName;
	private EOQualifier _defaultQualifier;
	private EOQualifier _baseQualifier;
	private ERXKeyFilter _qualifierFilter;
	private NSArray<EOSortOrdering> _defaultSortOrderings;
	private int _maxBatchSize;
	private int _defaultBatchSize;
	private boolean _requestQualifiersEnabled;

	/**
	 * Creates a new ERXRestFetchSpecification with a maximum batch size of 100, but with batching turned off by
	 * default.
	 * 
	 * @param entityName
	 *            the name of the entity being fetched
	 * @param defaultQualifier
	 *            the default qualifiers (if none are specified in the request)
	 * @param defaultSortOrderings
	 *            the default sort orderings (if none are specified in the request)
	 */
	public ERXRestFetchSpecification(String entityName, EOQualifier defaultQualifier, NSArray<EOSortOrdering> defaultSortOrderings) {
		this(entityName, defaultQualifier, defaultSortOrderings, -1);
	}

	/**
	 * Creates a new ERXRestFetchSpecification with a maximum batch size of 100. default.
	 * 
	 * @param entityName
	 *            the name of the entity being fetched
	 * @param defaultQualifier
	 *            the default qualifiers (if none are specified in the request)
	 * @param defaultSortOrderings
	 *            the default sort orderings (if none are specified in the request)
	 * @param defaultBatchSize
	 *            the default batch size (-1 to disable)
	 */
	public ERXRestFetchSpecification(String entityName, EOQualifier defaultQualifier, NSArray<EOSortOrdering> defaultSortOrderings, int defaultBatchSize) {
		_entityName = entityName;
		_defaultQualifier = defaultQualifier;
		_defaultSortOrderings = defaultSortOrderings;
		_maxBatchSize = 100;
		_defaultBatchSize = defaultBatchSize;
	}

	/**
	 * Creates a new ERXRestFetchSpecification with a maximum batch size of 100 and with request qualifiers enabled.
	 * default.
	 * 
	 * @param entityName
	 *            the name of the entity being fetched
	 * @param defaultQualifier
	 *            the default qualifiers (if none are specified in the request)
	 * @param baseQualifier
	 *            the base qualifier (see enableRequestQualifiers)
	 * @param qualifierFilter
	 *            the key filter to apply against the query qualifier
	 * @param defaultSortOrderings
	 *            the default sort orderings (if none are specified in the request)
	 * @param defaultBatchSize
	 *            the default batch size (-1 to disable)
	 */
	public ERXRestFetchSpecification(String entityName, EOQualifier defaultQualifier, EOQualifier baseQualifier, ERXKeyFilter qualifierFilter, NSArray<EOSortOrdering> defaultSortOrderings, int defaultBatchSize) {
		_entityName = entityName;
		_defaultQualifier = defaultQualifier;
		_defaultSortOrderings = defaultSortOrderings;
		_maxBatchSize = 100;
		_defaultBatchSize = defaultBatchSize;
		enableRequestQualifiers(baseQualifier, qualifierFilter);
	}

	/**
	 * Returns the maximum batch size (defaults to 100).
	 * 
	 * @return the maximum batch size
	 */
	public int maxBatchSize() {
		return _maxBatchSize;
	}

	/**
	 * Sets the maximum batch size.
	 * 
	 * @param maxBatchSize
	 *            the maximum batch size
	 */
	public void setMaxBatchSize(int maxBatchSize) {
		_maxBatchSize = maxBatchSize;
	}

	/**
	 * Returns the default batch size (defaults to -1 = off).
	 * 
	 * @return the default batch size
	 */
	public int defaultBatchSize() {
		return _defaultBatchSize;
	}

	/**
	 * Sets the default batch size
	 * 
	 * @param defaultBatchSize
	 *            the default batch size
	 */
	public void setDefaultBatchSize(int defaultBatchSize) {
		_defaultBatchSize = defaultBatchSize;
	}

	/**
	 * Enables qualifiers in the request, but will be AND'd to the given base qualifier (in case you need to perform
	 * security restrictions)
	 * 
	 * @param baseQualifier
	 *            the base qualifier to and with
	 * @param qualifierFilter
	 *            the key filter to apply against the query qualifier
	 */
	public void enableRequestQualifiers(EOQualifier baseQualifier, ERXKeyFilter qualifierFilter) {
		_baseQualifier = baseQualifier;
		_qualifierFilter = qualifierFilter;
		_requestQualifiersEnabled = true;
	}

	/**
	 * Returns the effective sort orderings.
	 * 
	 * @param request
	 *            the current request
	 * @return the effective sort orderings
	 */
	public NSArray<EOSortOrdering> sortOrderings(WORequest request) {
		String sortKeysStr = request.stringFormValueForKey("sort");
		if (sortKeysStr == null || sortKeysStr.length() == 0) {
			return _defaultSortOrderings;
		}
		NSMutableArray<EOSortOrdering> sortOrderings = new NSMutableArray<EOSortOrdering>();
		for (String sortKeyStr : sortKeysStr.split(",")) {
			String[] sortAttributes = sortKeyStr.split("\\|");
			String sortKey = sortAttributes[0];

			NSSelector sortDirection;
			if (sortAttributes.length == 2) {
				if (sortAttributes[1].equalsIgnoreCase("asc")) {
					sortDirection = EOSortOrdering.CompareCaseInsensitiveAscending;
				}
				else if (sortAttributes[1].equalsIgnoreCase("desc")) {
					sortDirection = EOSortOrdering.CompareCaseInsensitiveDescending;
				}
				else {
					sortDirection = EOSortOrdering.CompareCaseInsensitiveAscending;
				}
			}
			else {
				sortDirection = EOSortOrdering.CompareCaseInsensitiveAscending;
			}

			sortOrderings.addObject(EOSortOrdering.sortOrderingWithKey(sortKey, sortDirection));
		}
		return sortOrderings;
	}

	/**
	 * Returns the effective qualifier.
	 * 
	 * @param request
	 *            the current request
	 * @return the effective qualifier
	 */
	public EOQualifier qualifier(EOEditingContext editingContext, WORequest request) {
		EOQualifier qualifier;
		if (!_requestQualifiersEnabled) {
			qualifier = _defaultQualifier;
		}
		else {
			String qualifierStr = request.stringFormValueForKey("qualifier");
			if (qualifierStr == null || qualifierStr.length() == 0) {
				if (_baseQualifier == null) {
					qualifier = _defaultQualifier;
				}
				else {
					qualifier = ERXQ.and(_baseQualifier, _defaultQualifier);
				}
			}
			else {
				qualifier = EOQualifier.qualifierWithQualifierFormat(qualifierStr, null);
				if (qualifier == null) {
					qualifier = _baseQualifier;
				}
				else {
					EOEntity entity = EOUtilities.entityNamed(editingContext, _entityName);
					ERXFilteredQualifierTraversal.checkQualifierForEntityWithFilter(qualifier, entity, _qualifierFilter);
					if (_baseQualifier != null) {
						qualifier = ERXQ.and(_baseQualifier, qualifier);
					}
				}
			}
		}
		return qualifier;
	}

	/**
	 * Returns the effective batch number.
	 * 
	 * @param request
	 *            the current request
	 * @return the effective batch number
	 */
	public int batchNumber(WORequest request) {
		int batchNumber;
		String batchNumberStr = request.stringFormValueForKey("batch");
		if (batchNumberStr == null) {
			batchNumber = 0;
		}
		else {
			batchNumber = Integer.parseInt(batchNumberStr);
		}
		return batchNumber;
	}

	/**
	 * Returns the effective batch size.
	 * 
	 * @param request
	 *            the current request
	 * @return the effective batch size
	 */
	public int batchSize(WORequest request) {
		int batchSize;
		String batchSizeStr = request.stringFormValueForKey("batchSize");
		if (batchSizeStr == null) {
			batchSize = _defaultBatchSize;
		}
		else {
			batchSize = Math.min(Integer.parseInt(batchSizeStr), _maxBatchSize);
		}
		return batchSize;
	}

	/**
	 * Fetches the objects into the given editing context with the effective attributes of this fetch specification.
	 * 
	 * @param editingContext
	 *            the editing context to fetch into
	 * @param request
	 *            the current request
	 * @return the fetch objects
	 */
	@SuppressWarnings("unchecked")
	public NSArray<T> objects(EOEditingContext editingContext, WORequest request) {
		NSArray<EOSortOrdering> sortOrderings = sortOrderings(request);
		EOQualifier qualifier = qualifier(editingContext, request);
		int batchSize = batchSize(request);

		EOFetchSpecification fetchSpec = new EOFetchSpecification(_entityName, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);

		NSArray<T> results;
		if (batchSize <= 0) {
			results = editingContext.objectsWithFetchSpecification(fetchSpec);
		}
		else {
			int batchNumber = batchNumber(request);
			ERXFetchSpecificationBatchIterator batchIterator = new ERXFetchSpecificationBatchIterator(fetchSpec, editingContext, batchSize);
			results = batchIterator.batchWithIndex(batchNumber);
		}
		return results;
	}

	/**
	 * Applies the effective attributes of this fetch specification to the given array, filtering, sorting, and cutting
	 * into batches accordingly.
	 * 
	 * @param objects
	 *            the objects to filter
	 * @param editingContext
	 *            the editing context to evaluate the qualifer filter with
	 * @param request
	 *            the current request
	 * @return the filtered objects
	 */
	public NSArray<T> objects(NSArray<T> objects, EOEditingContext editingContext, WORequest request) {
		NSArray<EOSortOrdering> sortOrderings = sortOrderings(request);
		EOQualifier qualifier = qualifier(editingContext, request);
		int batchSize = batchSize(request);

		NSArray<T> results = ERXS.sorted(ERXQ.filtered(objects, qualifier), sortOrderings);
		if (batchSize > 0) {
			int batchNumber = batchNumber(request);
			int offset = batchNumber * batchSize;
			int length = batchSize;
			if (offset >= results.count()) {
				results = NSArray.<T> emptyArray();
			}
			else {
				NSRange range;
				if (offset + length > results.count()) {
					range = new NSRange(offset, results.count() - offset);
				}
				else {
					range = new NSRange(offset, length);
				}
				results = objects.subarrayWithRange(range);
			}
		}
		return results;
	}
}
