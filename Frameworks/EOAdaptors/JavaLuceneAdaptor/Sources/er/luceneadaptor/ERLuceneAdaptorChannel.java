package er.luceneadaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.text.ParseException;
import java.util.Date;

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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
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

import er.extensions.eof.qualifiers.ERXBetweenQualifier;
import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.qualifiers.ERXQualifierTraversal;

/**
 * ERLuceneAdaptorChannel takes care of the actual writing and reading.
 * 
 * @author ak
 */
public class ERLuceneAdaptorChannel extends EOAdaptorChannel {

	private static final String EXTERNAL_NAME_KEY = "_e";

	private static class LuceneQualifierTraversal extends ERXQualifierTraversal {

		NSMutableArray _queries;
		EOEntity _entity;

		public LuceneQualifierTraversal(EOEntity entity) {
			_entity = entity;
		}

		protected NSArray<Query> queriesForCurrent(int count) {
			NSRange range = new NSRange(_queries.count() - count, count);
			NSArray<Query> result = _queries.subarrayWithRange(range);
			_queries.removeObjectsInRange(range);
			return result;
		}

		@Override
		protected boolean traverseAndQualifier(EOAndQualifier q) {
			NSArray<Query> queries = queriesForCurrent(q.qualifiers().count());
			BooleanQuery query = new BooleanQuery();
			for (Query current : queries) {
				query.add(current, BooleanClause.Occur.MUST);
			}
			_queries.addObject(query);
			return true;
		}

		@Override
		protected boolean traverseNotQualifier(EONotQualifier q) {
			NSArray<Query> queries = queriesForCurrent(1);
			BooleanQuery query = new BooleanQuery();
			query.add(queries.lastObject(), BooleanClause.Occur.MUST_NOT);
			_queries.addObject(query);
			return true;
		}

		@Override
		protected boolean traverseOrQualifier(EOOrQualifier q) {
			NSArray<Query> queries = queriesForCurrent(q.qualifiers().count());
			DisjunctionMaxQuery query = new DisjunctionMaxQuery(queries, 0);
			_queries.addObject(query);
			return true;
		}

		@Override
		protected boolean traverseUnknownQualifier(EOQualifierEvaluation q) {
			throw new IllegalArgumentException("Unknown qualifier: " + q);
		}

		@Override
		protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
			Query query = null;
			String key = _entity.attributeNamed(q.key()).columnName();
			IndexAttribute attr = new IndexAttribute(_entity.attributeNamed(key));
			if (q instanceof ERXBetweenQualifier) {
				ERXBetweenQualifier between = (ERXBetweenQualifier) q;
				Object min = between.minimumValue();
				Object max = between.maximumValue();
				query = new TermRangeQuery(key, attr.asLuceneValue(min), attr.asLuceneValue(max), false, false);
			} else if(q.selector().equals(EOQualifier.QualifierOperatorGreaterThan)) {
				query = new TermRangeQuery(key, attr.asLuceneValue(q.value()), null, false, false);
			} else if(q.selector().equals(EOQualifier.QualifierOperatorGreaterThanOrEqualTo)) {
				query = new TermRangeQuery(key, attr.asLuceneValue(q.value()), null, true, false);
			} else if(q.selector().equals(EOQualifier.QualifierOperatorLessThan)) {
				query = new TermRangeQuery(key, null, attr.asLuceneValue(q.value()), false, false);
			} else if(q.selector().equals(EOQualifier.QualifierOperatorLessThanOrEqualTo)) {
				query = new TermRangeQuery(key, null, attr.asLuceneValue(q.value()), false, true);
			} else if(q.selector().equals(EOQualifier.QualifierOperatorCaseInsensitiveLike) || q.selector().equals(EOQualifier.QualifierOperatorLike)) {
				String value = q.value().toString();
				if(q.selector().equals(EOQualifier.QualifierOperatorLike)) {
					value = value.toLowerCase();
				}
				int star = value.indexOf('*');
				if(star >= 0) {
					if(star < value.length() - 1) {
						query = new WildcardQuery(new Term(key, value));
					} else {
						query = new PrefixQuery(new Term(key, value.substring(0, star)));
					}
				} else if(value.contains(" ")) {
					MultiPhraseQuery multi = new MultiPhraseQuery();
					query = multi;
					String parts[] = value.split(" +");
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						multi.add(new Term(key, part));
					}
				} else {
					query = new TermQuery(new Term(key, value));
				}
			} else {
				query = new TermQuery(new Term(key, attr.asLuceneValue(q.value())));
			}
			_queries.addObject(query);
			return true;
		}

		@Override
		protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
			throw new IllegalArgumentException("Unknown qualifier: " + q);
		}

		@Override
		public void traverse(EOQualifierEvaluation q, boolean postOrder) {
			_queries = new NSMutableArray<Query>();
			super.traverse(q, true);
		}

		public Query query() {
			BooleanQuery q = new BooleanQuery();
			q.add(new TermQuery(new Term(EXTERNAL_NAME_KEY, _entity.externalName())), BooleanClause.Occur.MUST);
			q.add((Query) _queries.lastObject(), BooleanClause.Occur.MUST);
			return q;
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

		private String _columnName;

		private TermVector _termVector;

		private Store _store;

		private Index _index;

		private Analyzer _analyzer;

		private Format _format;

		private EOAttribute _attribute;

		@SuppressWarnings("deprecation")
		public IndexAttribute(EOAttribute attribute) {
			_attribute = attribute;
			NSDictionary dict = attribute.userInfo() != null ? attribute.userInfo() : NSDictionary.emptyDictionary();
			_columnName = attribute.columnName();
			boolean isClassProperty = _attribute.entity().classPropertyNames().contains(_attribute.name());
			boolean isDataProperty = _attribute.className().endsWith("NSData");
			boolean isStringProperty = _attribute.className().endsWith("String");
			_termVector = (TermVector) classValue(dict, "termVector", TermVector.class, isClassProperty && !isDataProperty && isStringProperty ? "YES" : "NO");
			_store = (Store) classValue(dict, "store", Store.class, "YES");
			_index = (Index) classValue(dict, "index", Index.class, isClassProperty && !isDataProperty && isStringProperty ? "ANALYZED" : "NOT_ANALYZED");
			String analyzerClass = (String) dict.objectForKey("analyzer");
			if (analyzerClass == null && _columnName.matches("\\w+_(\\w+)")) {
				String locale = _columnName.substring(_columnName.lastIndexOf('_') + 1).toLowerCase();
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

		public String columnName() {
			return _columnName;
		}

		public Analyzer analyzer() {
			return _analyzer;
		}

		public String asLuceneValue(Object value) {
			if (_format != null) {
				return _format.format(value);
			}
			if(value == null) {
				return null;
			}
			if (attribute().valueType() != null) {
				char valueType = attribute().valueType().charAt(0);
				switch (valueType) {
				case 'i':
					return NumericUtils.intToPrefixCoded(((Number) value).intValue());
				case 'b':
					return NumericUtils.longToPrefixCoded(((Number) value).longValue());
				case 'l':
					return NumericUtils.longToPrefixCoded(((Number) value).longValue());
				case 'd':
					return NumericUtils.doubleToPrefixCoded(((Number) value).doubleValue());
				case 'B':
					return NumericUtils.doubleToPrefixCoded(((Number) value).doubleValue());
				}
			}
			if (value instanceof Date) {
				return DateTools.dateToString((Date) value, Resolution.MILLISECOND);
			} else if (value instanceof NSData) {
				return NSPropertyListSerialization.stringFromPropertyList(value);
			} else if (value instanceof NSArray) {
				return ((NSArray) value).componentsJoinedByString(" ");
			}
			return value.toString();
		}

		public Object asEOFValue(String value) {
			try {
				if (_format != null) {
					return _format.parseObject(value);
				}
				if(value == null) {
					return null;
				}
				if (attribute().valueType() != null) {
					char valueType = attribute().valueType().charAt(0);
					switch (valueType) {
					case 'i':
						return Integer.valueOf(NumericUtils.prefixCodedToInt(value));
					case 'b':
						return BigInteger.valueOf(NumericUtils.prefixCodedToLong(value));
					case 'l':
						return Long.valueOf(NumericUtils.prefixCodedToLong(value));
					case 'd':
						return Double.valueOf(NumericUtils.prefixCodedToDouble(value));
					case 'B':
						return BigDecimal.valueOf(NumericUtils.prefixCodedToDouble(value));
					}
				}
				if (attribute().className().contains("NSTimestamp")) {
					return new NSTimestamp(DateTools.stringToDate(value));
				} else if (attribute().className().contains("NSData")) {
					return new NSData((NSData) NSPropertyListSerialization.propertyListFromString(value));
				} else if (attribute().className().contains("NSArray")) {
					return NSArray.componentsSeparatedByString(value, " ");
				}
				return value.toString();
			} catch (ParseException ex) {
				throw NSForwardException._runtimeExceptionForThrowable(ex);
			}
		}

		public Field valueToField(Document doc, Object value) {
			String stringValue = asLuceneValue(value);
			Field field = doc.getField(columnName());
			if (value != null) {
				if (field == null) {
					field = new Field(columnName(), stringValue, store(), index(), termVector());
				}
				field.setValue(stringValue);
			} else {
				field = null;
			}
			if (field != null) {
				field.setValue(stringValue);
			}
			return field;
		}

		public EOAttribute attribute() {
			return _attribute;
		}
	}

	private NSArray<EOAttribute> _attributes;
	private NSArray<IndexAttribute> _indexAttributes;
	private EOEntity _entity;
	private int _fetchIndex;
	private boolean _open;
	private IndexSearcher _searcher;
	private boolean _fetchInProgress = false;
	private TopDocs _fetchedDocs;

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
		if (_searcher == null/*
							 * || !adaptorContext().adaptor().indexReader().isCurrent ()
							 */) {
			_searcher = new IndexSearcher(adaptorContext().adaptor().indexReader());
		}
		return _searcher;
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
		_fetchedDocs = null;
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
				int docId = _fetchedDocs.scoreDocs[_fetchIndex++].doc;
				Document doc = searcher().doc(docId);
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
		return _fetchIndex < _fetchedDocs.totalHits;
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
	public void selectAttributes(NSArray attributesToFetch, EOFetchSpecification fs, boolean shouldLock, EOEntity entity) {
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
			Query query = null;
			Sort sort = null;
			
			if(fs.hints() != null) {
				query = (Query) fs.hints().objectForKey(ERLuceneAdaptor.QUERY_HINTS);
				sort = (Sort) fs.hints().objectForKey(ERLuceneAdaptor.SORT_HINTS);
			}
			if(query == null) {
				query = queryForQualifier(fs.qualifier(), entity);
			}
			if(sort == null) {
				sort = sortForSortOrderings(fs.sortOrderings());
			}
			int fetchLimit = fs.fetchLimit() > 0 ? fs.fetchLimit() : Integer.MAX_VALUE;
			if (sort != null) {
				_fetchedDocs = searcher.search(query, null, fetchLimit, sort);
			} else {
				_fetchedDocs = searcher.search(query, fetchLimit);
			}
		} catch (EOGeneralAdaptorException e) {
			cancelFetch();
			throw e;
		} catch (Throwable e) {
			cancelFetch();
			throw new ERLuceneAdaptorException("Failed to fetch '" + entity.name() + "' with fetch specification '" + fs + "': " + e.getMessage(), e);
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
			String name = info.columnName();
			String value = doc.get(name);
			Term term = new Term(name);
			term = term.createTerm(value);
			return term;
		}
		return null;
	}

	private void fillWithDictionary(Document doc, NSDictionary row, EOEntity entity) {
		for (IndexAttribute info : attributesForEntity(entity)) {
			Object value = row.objectForKey(info.attribute().columnName());
			if (value != null) {
				if(value == NSKeyValueCoding.NullValue) {
					value = null;
				}
				Field field = info.valueToField(doc, value);
				if (field != null) {
					doc.add(field);
				}
			}
		}
	}

	@Override
	public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
		try {
			IndexSearcher searcher = searcher();
			Query query = queryForQualifier(qualifier, entity);
			TopDocs fetchedDocs = searcher.search(query, Integer.MAX_VALUE);
			int count = fetchedDocs.totalHits;
			for (int i = 0; i < count; i++) {
				int docId = fetchedDocs.scoreDocs[i].doc;
				Document doc = searcher.doc(docId);
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
			doc.add(new Field(EXTERNAL_NAME_KEY, entity.externalName(), Store.NO, Index.NOT_ANALYZED));
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
			Query query = queryForQualifier(qualifier, entity);
			TopDocs fetchedDocs = searcher.search(query, Integer.MAX_VALUE);
			int count = fetchedDocs.totalHits;
			for (int i = 0; i < count; i++) {
				int docId = fetchedDocs.scoreDocs[i].doc;
				Document doc = searcher.doc(docId);
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

	/**
	 * Convenience method to create a Lucene query from an EOF qualifier.
	 * @param qualifier
	 * @param entity
	 */
	public static Query queryForQualifier(EOQualifier qualifier, EOEntity entity) {
		if(qualifier == null) {
			return new TermQuery(new Term(EXTERNAL_NAME_KEY, entity.externalName()));
		}
		LuceneQualifierTraversal traverser = new LuceneQualifierTraversal(entity);
		traverser.traverse(qualifier, true);
		Query query = traverser.query();
		return query;
	}


	/**
	 * Convenience method to create a Lucene sort from an EOF sort ordering array.
	 * @param sortOrderings
	 */
	public static Sort sortForSortOrderings(NSArray<EOSortOrdering> sortOrderings) {
		Sort sort = null;
		if (sortOrderings != null && sortOrderings.count() > 0) {
			NSMutableArray<SortField> fields = new NSMutableArray<SortField>(sortOrderings.count());
			for (EOSortOrdering s : sortOrderings) {
				String name = s.key();
				NSSelector sel = s.selector();
				boolean reverse = sel.equals(EOSortOrdering.CompareDescending) || sel.equals(EOSortOrdering.CompareCaseInsensitiveDescending);
				SortField sf = new SortField(name, SortField.DOC, reverse);
				fields.addObject(sf);
			}
			if (fields.count() > 0) {
				sort = new Sort();
				sort.setSort(fields.toArray(new SortField[] {}));
			}
		}
		return sort;
	}
}
