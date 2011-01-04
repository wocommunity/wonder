package er.luceneadaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Format;
import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSUtilities;

import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.qualifiers.ERXQualifierTraversal;

/**
 * ERLuceneAdaptorChannel takes care of the actual writing and reading.
 * 
 * @author ak
 */
public class ERLuceneAdaptorChannel extends EOAdaptorChannel {

	static Logger log = Logger.getLogger(ERLuceneAdaptorChannel.class);

	private static class LuceneQualifierTraversal extends ERXQualifierTraversal {

		NSMutableArray _qualifiers = new NSMutableArray();

		public LuceneQualifierTraversal() {
		}

		protected NSArray<EOQualifier> qualifiersForLast(NSArray original) {
			NSRange range = new NSRange(_qualifiers.count() - original.count(), original.count());
			NSArray<EOQualifier> result = _qualifiers.subarrayWithRange(range);
			_qualifiers.removeObjectsInRange(range);
			return result;
		}

		@Override
		protected boolean traverseAndQualifier(EOAndQualifier q) {
			NSArray quals = qualifiersForLast(q.qualifiers());
			_qualifiers.addObject(quals);
			return true;
		}

		@Override
		protected void visit(EOQualifierEvaluation q) {
			_qualifiers = new NSMutableArray<EOQualifier>();
			super.visit(q);
		}

		@Override
		protected boolean traverseUnknownQualifier(EOQualifierEvaluation q) {
			throw new IllegalArgumentException("Unknown qualifier: " + q);
		}

		@Override
		protected boolean traverseNotQualifier(EONotQualifier q) {
			_qualifiers.addObject(q);
			return super.traverseNotQualifier(q);
		}

		@Override
		protected boolean traverseOrQualifier(EOOrQualifier q) {
			NSArray quals = qualifiersForLast(q.qualifiers());
			_qualifiers.addObject(quals);
			return true;
		}

		@Override
		protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
			_qualifiers.addObject(q);
			return true;
		}

		@Override
		protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
			throw new IllegalArgumentException("Unknown qualifier: " + q);
		}

		public Query query() {
			//TermQuery query = new TermQuery(new Term("", "vessn*"));
			NumericRangeQuery query = NumericRangeQuery.newIntRange("userCount", Integer.valueOf(300), Integer.valueOf(2000), true, true);
			//query.add();
			return query;
		}
	}

	/**
	 * Morphs EO values to lucene values.
	 * 
	 * @author ak
	 * 
	 */
	protected static class IndexAttribute {
		private static String[] NAMES = new String[] { "Arabic", "Brazilian", "CJK", "Chinese", "Czech", "German", "Greek", "Persian", "French", "Dutch", "Russian", "Thai" };
		private static String[] CODES = new String[] { "ar", "br", "cjk", "cn", "cz", "de", "el", "fa", "fr", "nl", "ru", "th" };
		private static NSDictionary<String, String> LOCALES = new NSDictionary<String, String>(NAMES, CODES);

		String _name;

		TermVector _termVector;

		Store _store;

		Index _index;

		Analyzer _analyzer;

		Format _format;

		EOAttribute _attribute;

		public IndexAttribute(EOAttribute attribute) {
			_attribute = attribute;
			NSDictionary dict = attribute.userInfo() != null ? attribute.userInfo() : NSDictionary.emptyDictionary();
			_name = attribute.columnName();
			boolean isClassProperty = _attribute.entity().classPropertyNames().contains(_attribute.name());
			boolean isDataProperty = _attribute.className().contains("NSData");
			boolean isStringProperty = _attribute.className().contains("String");
			_termVector = (TermVector) classValue(dict, "termVector", TermVector.class, isClassProperty && !isDataProperty && isStringProperty? "YES" : "NO");
			_store = (Store) classValue(dict, "store", Store.class, "YES");
			_index = (Index) classValue(dict, "index", Index.class, isClassProperty && !isDataProperty && isStringProperty ? "ANALYZED" : "NOT_ANALYZED");
			String analyzerClass = (String) dict.objectForKey("analyzer");
			if (analyzerClass == null && _name.matches("\\w+_(\\w+)")) {
				String locale = _name.substring(_name.lastIndexOf('_') + 1).toLowerCase();
				analyzerClass = LOCALES.objectForKey(locale);
				if (analyzerClass != null) {
					analyzerClass = ERXPatcher.classForName("org.apache.lucene.analysis." + locale + "." + analyzerClass).getName();
				}
			}
			if (analyzerClass == null) {
				analyzerClass = StandardAnalyzer.class.getName();
			}
			Class c = ERXPatcher.classForName(analyzerClass);
			_analyzer = (Analyzer) _NSUtilities.instantiateObject(c, new Class[] { Version.class }, new Object[] { Version.LUCENE_20 }, true, false);

			_format = (Format) create((String) dict.objectForKey("format"));
			String numberFormat = (String) dict.objectForKey("numberformat");
			if (numberFormat != null) {
				_format = new NSNumberFormatter(numberFormat);
			}
			String dateformat = (String) dict.objectForKey("dateformat");
			if (dateformat != null) {
				_format = new NSTimestampFormatter(dateformat);
			}
		}

		private Object create(String className) {
			if (className != null) {
				try {
					Class c = ERXPatcher.classForName(className);
					return c.newInstance();
				} catch (InstantiationException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				} catch (IllegalAccessException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}
			return null;
		}

		private Object classValue(NSDictionary dict, String key, Class c, String defaultValue) {
			Object result;
			String code = (String) dict.objectForKey(key);
			if (code == null) {
				code = defaultValue;
			}
			result = ERXKeyValueCodingUtilities.classValueForKey(c, code);
			return result;
		}

		public TermVector termVector() {
			return _termVector;
		}

		public Index index() {
			return _index;
		}

		public Store store() {
			return _store;
		}

		public String name() {
			return _name;
		}

		public Analyzer analyzer() {
			return _analyzer;
		}

		public String asLuceneValue(Object value) {
			if (_format != null) {
				return _format.format(value);
			}
			if (value instanceof Number) {
				if (value instanceof Double) {
					return NumericUtils.doubleToPrefixCoded(((Number) value).doubleValue());
				} else if (value instanceof Long) {
					return NumericUtils.longToPrefixCoded(((Number) value).longValue());
				} else if (value instanceof BigDecimal) {
					return NumericUtils.doubleToPrefixCoded(((Number) value).doubleValue());
				}
				return NumericUtils.intToPrefixCoded(((Number) value).intValue());
			} else if (value instanceof Date) {
				return DateTools.dateToString((Date) value, Resolution.MILLISECOND);
			} else if (value instanceof NSData) {
				return NSPropertyListSerialization.stringFromPropertyList(value);
			} else if (value instanceof NSArray) {
				return ((NSArray) value).componentsJoinedByString(" ");
			}
			return (value != null ? value.toString() : null);
		}

		public Object asEOFValue(String value) {
			try {
				if (_format != null) {
					return _format.parseObject(value);
				}

				if (attribute().className().contains("NSTimestamp")) {
					return new NSTimestamp(DateTools.stringToDate(value));
				} else if (attribute().className().contains("NSData")) {
					return new NSData((NSData)NSPropertyListSerialization.propertyListFromString(value));
				} else if (attribute().className().contains("NSArray")) {
					return NSArray.componentsSeparatedByString(value, " ");
				} else {
					String valueType = attribute().valueType();
					if ("b".equals(valueType)) {
						return Integer.valueOf(NumericUtils.prefixCodedToInt(value));
					} else if ("i".equals(valueType)) {
						return Integer.valueOf(NumericUtils.prefixCodedToInt(value));
					} else if ("l".equals(valueType)) {
						return Long.valueOf(NumericUtils.prefixCodedToLong(value));
					} else if ("d".equals(valueType)) {
						return Double.valueOf(NumericUtils.prefixCodedToDouble(value));
					} else if ("B".equals(valueType)) {
						return BigDecimal.valueOf(NumericUtils.prefixCodedToDouble(value));
					}
				}
				return (value != null ? value.toString() : null);
			} catch (ParseException ex) {
				throw NSForwardException._runtimeExceptionForThrowable(ex);
			}
		}

		public Field valueToField(Object value) {
			String stringValue = asLuceneValue(value);
			if (stringValue != null) {
				Field field = new Field(name(), stringValue, store(), index(), termVector());
				return field;
			}
			return null;
		}

		public EOAttribute attribute() {
			return _attribute;
		}
	}

	private NSArray<EOAttribute> _attributes;
	private NSArray<IndexAttribute> _indexAttributes;
	private EOEntity _entity;
	private int _fetchIndex;
	private int _fetchCount;
	private boolean _open;
	private IndexSearcher _searcher;
	private boolean _fetchInProgress = false;

	public ERLuceneAdaptorChannel(ERLuceneAdaptorContext context) {
		super(context);
		_fetchIndex = -1;
	}

	private NSArray<IndexAttribute> attributesForEntity(EOEntity entity) {
		return attributesForAttributes(entity.attributesToFetch());
	}

	private NSArray<IndexAttribute> attributesForAttributes(NSArray<EOAttribute> attributes) {
		NSMutableArray result = new NSMutableArray<IndexAttribute>(attributes.count());
		for (EOAttribute attribute : attributes) {
			result.addObject(new IndexAttribute(attribute));
		}
		return result;
	}

	public IndexWriter writer() {
		return adaptorContext().writer();
	}

	public IndexSearcher searcher() throws CorruptIndexException, IOException {
		if (_searcher == null/* || !adaptorContext().adaptor().indexReader().isCurrent()*/) {
			_searcher = new IndexSearcher(adaptorContext().adaptor().indexReader());
		}
		return _searcher;
	}

	@Deprecated
	public NSDictionary primaryKeyForNewRowWithEntity(EOEntity entity) {
		return adaptorContext()._newPrimaryKey(null, entity);
	}

	@Override
	public ERLuceneAdaptorContext adaptorContext() {
		return (ERLuceneAdaptorContext) super.adaptorContext();
	}

	@Override
	public NSArray<EOAttribute> attributesToFetch() {
		return _attributes;
	}

	@Override
	public void cancelFetch() {
		reset();
	}

	private void reset() {
		_fetchInProgress = false;
		_fetchCount = -1;
		_fetchIndex = -1;
		_entity = null;
		_searcher = null;
		_attributes = null;
		_indexAttributes = null;
	}

	@Override
	public void closeChannel() {
		_open = false;
	}

	@Override
	public NSArray describeResults() {
		return _attributes;
	}

	@Override
	public NSArray describeTableNames() {
		return NSArray.EmptyArray;
	}

	@Override
	public EOModel describeModelWithTableNames(NSArray anArray) {
		return null;
	}

	@Override
	public void evaluateExpression(EOSQLExpression anExpression) {
		throw new UnsupportedOperationException("ERLuceneAdaptorChannel.evaluateExpression");
	}

	@Override
	public void executeStoredProcedure(EOStoredProcedure aStoredProcedure, NSDictionary someValues) {
		throw new UnsupportedOperationException("ERLuceneAdaptorChannel.executeStoredProcedure");
	}

	@Override
	public NSMutableDictionary fetchRow() {
		if (!_fetchInProgress) {
			return null;
		}
		NSMutableDictionary row = null;
		if (hasMoreRowsToReturn()) {
			try {
				Document doc = searcher().doc(_fetchIndex++);
				EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(_entity.name());
				NSMutableDictionary dict = cd._newDictionaryForProperties();
				for (IndexAttribute attr : _indexAttributes) {
					String name = attr.attribute().name();
					String columnName = attr.attribute().columnName();
					Field field = doc.getField(columnName);
					Object value = null;
					if (field != null) {
						if (field.isBinary()) {
							value = new NSData(field.getBinaryValue());
						} else {
							String stringValue = field.stringValue();
							value = attr.asEOFValue(stringValue);
						}
						dict.setObjectForKey(value, name);
					} else {
						dict.setObjectForKey(NSKeyValueCoding.NullValue, name);
					}
				}
				row = dict;
			} catch (CorruptIndexException e) {
				throw new ERLuceneAdaptorException("Failed to fetch row: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new ERLuceneAdaptorException("Failed to fetch row: " + e.getMessage(), e);
			}
		}
		_fetchInProgress = hasMoreRowsToReturn();
		return row;
	}

	private boolean hasMoreRowsToReturn() {
		return _fetchIndex < _fetchCount;
	}

	@Override
	public boolean isFetchInProgress() {
		return _fetchInProgress;
	}

	@Override
	public boolean isOpen() {
		return _open;
	}

	@Override
	public void openChannel() {
		if (!_open) {
			_open = true;
		}
	}

	@Override
	public NSDictionary returnValuesForLastStoredProcedureInvocation() {
		throw new UnsupportedOperationException("ERLuceneAdaptorChannel.returnValuesForLastStoredProcedureInvocation");
	}

	@Override
	public void selectAttributes(NSArray attributesToFetch, EOFetchSpecification fetchSpecification, boolean shouldLock, EOEntity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("null entity.");
		}
		if (attributesToFetch == null) {
			throw new IllegalArgumentException("null attributes.");
		}
		_fetchInProgress = true;
		_entity = entity;
		_searcher = null;
		setAttributesToFetch(attributesToFetch);

		try {
			_fetchIndex = 0;
			IndexSearcher searcher = searcher();
			Query query = queryForQualifier(fetchSpecification.qualifier());
			int fetchLimit = fetchSpecification.fetchLimit() > 0 ? fetchSpecification.fetchLimit() : Integer.MAX_VALUE;
			fetchLimit = 5;
			Sort sort = null;
			if (fetchSpecification.sortOrderings().count() > 0) {
				NSMutableArray<SortField> fields = new NSMutableArray<SortField>(fetchSpecification.sortOrderings().count());
				for (EOSortOrdering s : (NSArray<EOSortOrdering>) fetchSpecification.sortOrderings()) {
					String name = s.key();
					NSSelector sel = s.selector();
					boolean reverse = sel.equals(EOSortOrdering.CompareDescending) || sel.equals(EOSortOrdering.CompareCaseInsensitiveDescending);
					SortField sf = new SortField(name, 0, reverse);
					fields.addObject(sf);
				}
				if(fields.count() > 0) {
					sort = new Sort();
					sort.setSort(fields.toArray(new SortField[]{}));
				}
			} else {
				searcher.search(query, new QueryWrapperFilter(query), fetchLimit);
			}
			if(sort != null) {
				searcher.search(query, new QueryWrapperFilter(query), fetchLimit, sort);
			} else {
				searcher.search(query, new QueryWrapperFilter(query), fetchLimit);
			}
			_fetchCount = searcher.maxDoc();
		} catch (EOGeneralAdaptorException e) {
			cancelFetch();
			throw e;
		} catch (Throwable e) {
			cancelFetch();
			throw new ERLuceneAdaptorException("Failed to fetch '" + entity.name() + "' with fetch specification '" + fetchSpecification + "': " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setAttributesToFetch(NSArray attributesToFetch) {
		if (attributesToFetch == null) {
			throw new IllegalArgumentException("ERLuceneAdaptorChannel.setAttributesToFetch: null attributes.");
		}
		_attributes = attributesToFetch;
		_indexAttributes = attributesForAttributes(attributesToFetch);
	}

	private Term termForDocument(Document doc, EOEntity entity) {
		for (IndexAttribute info : attributesForAttributes(entity.primaryKeyAttributes())) {
			String name = info.name();
			String value = doc.get(name);
			Term term = new Term(name);
			term = term.createTerm(value);
			return term;
		}
		return null;
	}

	private Query queryForQualifier(EOQualifier qualifier) {
		if (qualifier == null && false) {
			return new MatchAllDocsQuery();
		}
		/*
		 * if(true) { FilteredQuery query = new FilteredQuery(null, new
		 * Filter()); query.add(new Term("content", "vessn*")); return query;
		 * 
		 * }
		 */

		LuceneQualifierTraversal traverser = new LuceneQualifierTraversal();
		traverser.traverse(qualifier);
		Query query = traverser.query();
		return query;
	}

	private void fillWithDictionary(Document doc, NSDictionary row, EOEntity entity) {
		for (IndexAttribute info : attributesForEntity(entity)) {
			Object value = row.objectForKey(info.attribute().name());

			Field field = info.valueToField(value);
			if (field != null) {
				doc.add(field);
			}
		}
	}

	@Override
	public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
		try {
			IndexSearcher searcher = searcher();
			Query query = queryForQualifier(qualifier);
			searcher.search(query, Integer.MAX_VALUE);
			int count = searcher.maxDoc();
			for (int i = 0; i < count; i++) {
				Document doc = searcher.doc(i);
				fillWithDictionary(doc, updatedRow, entity);
				Term term = termForDocument(doc, entity);
				writer().updateDocument(term, doc);
			}
			return count;
		} catch (EOGeneralAdaptorException e) {
			throw e;
		} catch (Throwable e) {
			throw new ERLuceneAdaptorException("Failed to update '" + entity.name() + "' row " + updatedRow + " with qualifier " + qualifier + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void insertRow(NSDictionary row, EOEntity entity) {
		try {
			Document doc = new Document();
			fillWithDictionary(doc, row, entity);
			writer().addDocument(doc);
		} catch (EOGeneralAdaptorException e) {
			throw e;
		} catch (Throwable e) {
			throw new ERLuceneAdaptorException("Failed to insert '" + entity.name() + "' with row " + row + ": " + e.getMessage(), e);
		}
	}

	@Override
	public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
		try {
			IndexSearcher searcher = searcher();
			Query query = queryForQualifier(qualifier);
			searcher.search(query, null, Integer.MAX_VALUE);
			int count = searcher.maxDoc();
			for (int i = 0; i < count; i++) {
				Document doc = searcher.doc(i);
				Term term = termForDocument(doc, entity);
				writer().deleteDocuments(term);
			}
			return count;
		} catch (EOGeneralAdaptorException e) {
			throw e;
		} catch (Throwable e) {
			throw new ERLuceneAdaptorException("Failed to delete '" + entity.name() + "' with qualifier " + qualifier + ": " + e.getMessage(), e);
		}
	}
}
