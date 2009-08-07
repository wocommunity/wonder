package er.rest;

import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.eof.ERXQ;

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
 * Because request EOQualifiers could possibly expose a security risk, you must explicitly enable request qualifiers by
 * calling enableRequestQualifiers(baseQualifier).
 * </p>
 * 
 * @author mschrag
 * 
 * @param <T>
 *            the type of the objects being returned
 */
public class ERXRestFetchSpecification<T extends EOEnterpriseObject> {
	private WORequest _request;
	private String _entityName;
	private EOQualifier _defaultQualifier;
	private EOQualifier _baseQualifier;
	private NSArray<EOSortOrdering> _defaultSortOrderings;
	private int _maxBatchSize;
	private int _defaultBatchSize;
	private boolean _requestQualifiersEnabled;

	/**
	 * Creates a new ERXRestFetchSpecification with a maximum batch size of 100, but with batching turned off by
	 * default.
	 * 
	 * @param request
	 *            the current request
	 * @param entityName
	 *            the name of the entity being fetched
	 * @param defaultQualifier
	 *            the default qualifiers (if none are specified in the request)
	 * @param defaultSortOrderings
	 *            the default sort orderings (if none are specified in the request)
	 */
	public ERXRestFetchSpecification(WORequest request, String entityName, EOQualifier defaultQualifier, NSArray<EOSortOrdering> defaultSortOrderings) {
		this(request, entityName, defaultQualifier, defaultSortOrderings, -1);
	}

	/**
	 * Creates a new ERXRestFetchSpecification with a maximum batch size of 100.
	 * default.
	 * 
	 * @param request
	 *            the current request
	 * @param entityName
	 *            the name of the entity being fetched
	 * @param defaultQualifier
	 *            the default qualifiers (if none are specified in the request)
	 * @param defaultSortOrderings
	 *            the default sort orderings (if none are specified in the request)
	 * @param defaultBatchSize
	 *            the default batch size (-1 to disable)
	 */
	public ERXRestFetchSpecification(WORequest request, String entityName, EOQualifier defaultQualifier, NSArray<EOSortOrdering> defaultSortOrderings, int defaultBatchSize) {
		_request = request;
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
	 * @param request
	 *            the current request
	 * @param entityName
	 *            the name of the entity being fetched
	 * @param defaultQualifier
	 *            the default qualifiers (if none are specified in the request)
	 * @param baseQualifier
	 *            the base qualifier (see enableRequestQualifiers)
	 * @param defaultSortOrderings
	 *            the default sort orderings (if none are specified in the request)
	 * @param defaultBatchSize
	 *            the default batch size (-1 to disable)
	 */
	public ERXRestFetchSpecification(WORequest request, String entityName, EOQualifier defaultQualifier, EOQualifier baseQualifier, NSArray<EOSortOrdering> defaultSortOrderings, int defaultBatchSize) {
		_request = request;
		_entityName = entityName;
		_defaultQualifier = defaultQualifier;
		_defaultSortOrderings = defaultSortOrderings;
		_maxBatchSize = 100;
		_defaultBatchSize = defaultBatchSize;
		enableRequestQualifiers(baseQualifier);
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
	 */
	public void enableRequestQualifiers(EOQualifier baseQualifier) {
		_baseQualifier = baseQualifier;
		_requestQualifiersEnabled = true;
	}

	/**
	 * Returns the effective sort orderings.
	 * 
	 * @return the effective sort orderings
	 */
	public NSArray<EOSortOrdering> sortOrderings() {
		String sortKeysStr = _request.stringFormValueForKey("sort");
		if (sortKeysStr == null || sortKeysStr.length() == 0) {
			return _defaultSortOrderings;
		}
		NSMutableArray<EOSortOrdering> sortOrderings = new NSMutableArray<EOSortOrdering>();
		for (String sortKeyStr : sortKeysStr.split(",")) {
			String[] sortAttributes = sortKeyStr.split("|");
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
	 * @return the effective qualifier
	 */
	public EOQualifier qualifier() {
		EOQualifier qualifier;
		if (!_requestQualifiersEnabled) {
			qualifier = _defaultQualifier;
		}
		else {
			String qualifierStr = _request.stringFormValueForKey("qualifier");
			if (qualifierStr == null || qualifierStr.length() == 0) {
				qualifier = _defaultQualifier;
			}
			else {
				qualifier = EOQualifier.qualifierWithQualifierFormat(qualifierStr, null);
				if (qualifier == null) {
					qualifier = _baseQualifier;
				}
				else if (_baseQualifier != null) {
					qualifier = ERXQ.and(_baseQualifier, qualifier);
				}
			}
		}
		return qualifier;
	}

	/**
	 * Returns the effective batch number.
	 * 
	 * @return the effective batch number
	 */
	public int batchNumber() {
		int batchNumber;
		String batchNumberStr = _request.stringFormValueForKey("batch");
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
	 * @return the effective batch size
	 */
	public int batchSize() {
		int batchSize;
		String batchSizeStr = _request.stringFormValueForKey("batchSize");
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
	 * @return the fetch objects
	 */
	@SuppressWarnings("unchecked")
	public NSArray<T> objectsInEditingContext(EOEditingContext editingContext) {
		NSArray<EOSortOrdering> sortOrderings = sortOrderings();
		EOQualifier qualifier = qualifier();
		int batchSize = batchSize();

		EOFetchSpecification fetchSpec = new EOFetchSpecification(_entityName, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);

		NSArray<T> results;
		if (batchSize == -1) {
			results = editingContext.objectsWithFetchSpecification(fetchSpec);
		}
		else {
			int batchNumber = batchNumber();
			ERXFetchSpecificationBatchIterator batchIterator = new ERXFetchSpecificationBatchIterator(fetchSpec, editingContext, batchSize);
			results = batchIterator.batchWithIndex(batchNumber);
		}
		return results;
	}
}
