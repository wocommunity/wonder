package er.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.appserver.WORequest;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;

/**
 * ERXRestFetchSpecification provides a wrapper around fetching objects with batching, sort orderings, and (optionally)
 * qualifiers configured in the WORequest.
 * <p>
 * Example query string parameters:
 * <ul>
 * <li>sort=lastName|asc,firstName|desc</li>
 * <li>batchSize=25&amp;batch=1 (Note that batch number is a zero-based index)</li>
 * <li>qualifier=firstName%3D'Mike'</li>
 * <li>Range=items%3D10-19 (Note that the index values for Range items are zero-based)</li>
 * </ul>
 * Because request EOQualifiers could possibly pose a security risk, you must explicitly enable request qualifiers by
 * calling enableRequestQualifiers(baseQualifier) or by using the longer constructor that takes an optional base
 * qualifier. A base qualifier is prepended (AND'd) to whatever qualifier is passed on the query string to restrict the
 * results of the user's query.
 * <p>
 * An example use:
 * <pre><code>
 * public WOActionResults indexAction() throws Throwable {
 * 	ERXRestFetchSpecification&lt;Task&gt; fetchSpec = new ERXRestFetchSpecification&lt;Task&gt;(Task.ENTITY_NAME, null, null, queryFilter(), Task.CREATION_DATE.descs(), 25);
 * 	NSArray&lt;Task&gt; tasks = fetchSpec.objects(editingContext(), options());
 * 	return response(editingContext(), Task.ENTITY_NAME, tasks, showFilter());
 * }
 * </code></pre>
 * In this example, we are fetching the "Task" entity, sorted by creation date, with a default batch size of 25, and
 * with request qualifiers enable (meaning, we allow users to pass in a qualifier in the query string), filtering the
 * qualifier with the ERXKeyFilter returned by the queryFilter() method. We then fetch the resulting tasks and return
 * the response to the user.
 * 
 * @author mschrag
 * 
 * @param <T>
 *            the type of the objects being returned
 */
public class ERXRestFetchSpecification<T extends EOEnterpriseObject> {
	private static final Pattern _rangePattern = Pattern.compile("items=(.*)-(.*)");
	
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
	 * Returns the name of the entity used in this fetch.
	 * 
	 * @return the name of the entity used in this fetch
	 */
	public String entityName() {
		return _entityName;
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
	 * @param editingContext
	 *            the editing context
	 * @param options
	 *            the current options
	 * @return the effective sort orderings
	 */
	public NSArray<EOSortOrdering> sortOrderings(EOEditingContext editingContext, NSKeyValueCoding options) {
		String sortKeysStr = (String) options.valueForKey("sort");
		if (sortKeysStr == null || sortKeysStr.length() == 0) {
			return _defaultSortOrderings;
		}
		EOEntity entity = EOUtilities.entityNamed(editingContext, _entityName);
		NSMutableArray<EOSortOrdering> sortOrderings = new NSMutableArray<>();
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

			if (_qualifierFilter != null && !_qualifierFilter.matches(new ERXKey<Object>(sortKey), ERXFilteredQualifierTraversal.typeForKeyInEntity(sortKey, entity))) {
				throw new SecurityException("You do not have access to the key path '" + sortKey + "'.");
			}
			sortOrderings.addObject(EOSortOrdering.sortOrderingWithKey(sortKey, sortDirection));
		}
		return sortOrderings;
	}

	/**
	 * Returns the effective qualifier.
	 * 
	 * @param editingContext
	 *            the editing context
	 * @param options
	 *            the current options
	 * @return the effective qualifier
	 */
	public EOQualifier qualifier(EOEditingContext editingContext, NSKeyValueCoding options) {
		EOQualifier qualifier;
		if (!_requestQualifiersEnabled) {
			qualifier = _defaultQualifier;
		}
		else {
			String qualifierStr = (String) options.valueForKey("qualifier");
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
	 * @param options
	 *            the current options
	 * @return the effective batch number
	 */
	public int batchNumber(NSKeyValueCoding options) {
		int batchNumber;
		String batchNumberStr = (String) options.valueForKey("batch");
		if (batchNumberStr == null) {
			batchNumber = 0;
		}
		else {
			batchNumber = Integer.parseInt(batchNumberStr);
		}
		return batchNumber;
	}

	/**
	 * Returns the range of this fetch.
	 * 
	 * @param options
	 *            the current options
	 * @return the effective batch number
	 */
	public NSRange range(NSKeyValueCoding options) {
		NSRange range = null;
		
		String rangeStr = (String)options.valueForKey("Range");
		if (rangeStr != null) {
			Matcher rangeMatcher = _rangePattern.matcher(rangeStr);
			if (rangeMatcher.matches()) {
				int start = Integer.parseInt(rangeMatcher.group(1));
				int length = Integer.parseInt(rangeMatcher.group(2)) - start + 1;
				range = new NSRange(start, length);
			}
		}
		else {
			int batchNumber = batchNumber(options);
			int batchSize = batchSize(options);
			if (batchSize > 0) {
				range = new NSRange(batchNumber * batchSize, batchSize);
			}
		}
		
		return range;
	}

	/**
	 * Returns the effective batch size.
	 * 
	 * @param options
	 *            the current options
	 * @return the effective batch size
	 */
	public int batchSize(NSKeyValueCoding options) {
		int batchSize;
		String batchSizeStr = (String) options.valueForKey("batchSize");
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
	 * @param options
	 *            the current options
	 * @return the fetch objects
	 */
	@SuppressWarnings("unchecked")
	public Results<T> results(EOEditingContext editingContext, NSKeyValueCoding options) {
		Results<T> results;
		NSArray<EOSortOrdering> sortOrderings = sortOrderings(editingContext, options);
		EOQualifier qualifier = qualifier(editingContext, options);

		EOFetchSpecification fetchSpec = new EOFetchSpecification(_entityName, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);

		NSArray<T> objects;
		NSRange range = range(options);
		if (range == null) {
			objects = editingContext.objectsWithFetchSpecification(fetchSpec);
			results = new Results<>(objects, 0, -1, objects.count());
		}
		else {
			ERXFetchSpecificationBatchIterator<T> batchIterator = new ERXFetchSpecificationBatchIterator<>(fetchSpec, editingContext, range.length());
			objects = batchIterator.batchWithRange(range);
			results = new Results<>(objects, range.location(), range.length(), batchIterator.count());
		}
		return results;
	}

	/**
	 * Fetches the objects into the given editing context with the effective attributes of this fetch specification.
	 * 
	 * @param editingContext
	 *            the editing context to fetch into
	 * @param options
	 *            the current options
	 * @return the fetch objects
	 */
	public NSArray<T> objects(EOEditingContext editingContext, NSKeyValueCoding options) {
		Results<T> results = results(editingContext, options);
		return results == null ? null : results.objects();
	}

	/**
	 * Applies the effective attributes of this fetch specification to the given array, filtering, sorting, and cutting
	 * into batches accordingly.
	 * 
	 * @param objects
	 *            the objects to filter
	 * @param editingContext
	 *            the editing context to evaluate the qualifer filter with
	 * @param options
	 *            the current options
	 * @return the filtered objects
	 */
	public NSArray<T> objects(NSArray<T> objects, EOEditingContext editingContext, NSKeyValueCoding options) {
		NSArray<EOSortOrdering> sortOrderings = sortOrderings(editingContext, options);
		EOQualifier qualifier = qualifier(editingContext, options);
		int batchSize = batchSize(options);

		NSArray<T> results = ERXS.sorted(ERXQ.filtered(objects, qualifier), sortOrderings);
		if (batchSize > 0) {
			int batchNumber = batchNumber(options);
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
				results = results.subarrayWithRange(range);
			}
		}
		return results;
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
	public NSArray<T> objects(EOEditingContext editingContext, WORequest request) {
		return objects(editingContext, new ERXRequestFormValues(request));
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
		return objects(objects, editingContext, new ERXRequestFormValues(request));
	}

	/**
	 * Encapsulates the results of a fetch along with some fetch metadata.
	 * 
	 * @author mschrag
	 *
	 * @param <T> the type of the result
	 */
	public static class Results<T> {
		private NSArray<T> _objects;
		private int _startIndex;
		private int _batchSize;
		private int _total;

		/**
		 * Constructs a new Results object.
		 * 
		 * @param objects the objects in the result
		 * @param startIndex the start index of the fetch
		 * @param batchSize the size of the batch
		 * @param totalCount the total number of objects
		 */
		public Results(NSArray<T> objects, int startIndex, int batchSize, int totalCount) {
			_objects = objects;
			_startIndex = startIndex;
			_batchSize = batchSize;
			_total = totalCount;
		}
		
		/**
		 * Returns the objects from this batch.
		 * 
		 * @return the objects from this batch
		 */
		public NSArray<T> objects() {
			return _objects;
		}
		
		/**
		 * Returns the start index of the fetch.
		 * 
		 * @return the start index of the fetch
		 */
		public int startIndex() {
			return _startIndex;
		}
		
		/**
		 * Returns the batch size.
		 * 
		 * @return the batch size
		 */
		public int batchSize() {
			return _batchSize;
		}
		
		/**
		 * Returns the total count of the results.
		 * 
		 * @return the total count of the results
		 */
		public int totalCount() {
			return _total;
		}
	}
}
