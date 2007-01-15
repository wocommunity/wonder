package er.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.ERXAsyncQueue;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXSelectorUtilities;

/**
 * Document = {
 *     index = com.foo.SomeIndexClass;
 *     store = "file://tmp/Document";
 *     type = "filed|db";
 *     buffered = false|true;
 *     properties = {
 *         foo = {
 *             store = "NO|YES|...";
 *             index = "NO|TOKENIZE|...";
 *             termVector = "NO|YES|...";
 *             analyzer = com.foo.SomeAnalyzerClass;
 *             numberformat = 0;
 *             dateformat = 0;
 *             format = com.foo.SomeFormatClass;
 *         };
 *         bar = {};
 *         baz.name = {};
 *     };
 * }
 * 
 * @author ak
 *
 */
public class ERIndex {

	private Logger log = Logger.getLogger(ERIndex.class);

	private static final String GID = "EOGlobalID";
	private static final String KEY = "ERIndexing";

	private ERXAsyncQueue _queue;
	private File _indexDirectory;
	private Analyzer _analyzer;
	private NSDictionary _attributes;
	private ERIndexModel _model;
	
	public ERIndex(ERIndexModel model, NSDictionary indexDef) {
		File indexDirectory = new File((String) indexDef.objectForKey("store"));
		_indexDirectory = indexDirectory;
		registerNotifications();
		_queue = new ERXAsyncQueue() {

			public void process(Object object) {
				index((ERIndexJob)object);
			}
		};
		_queue.setName(KEY);
		_queue.start();
		_analyzer = createAnalyzer(indexDef);
		_model = model;
	}

	protected Analyzer createAnalyzer(NSDictionary indexDef) {
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
		NSMutableDictionary attributes = new NSMutableDictionary();
		NSDictionary properties = (NSDictionary) indexDef.objectForKey("properties");
		for (Enumeration names = properties.keyEnumerator(); names.hasMoreElements();) {
			String propertyName = (String) names.nextElement();
			NSDictionary propertyDefinition = (NSDictionary) properties.objectForKey(propertyName);
			ERIndexAttribute attribute = new ERIndexAttribute(this, propertyName, propertyDefinition);
			wrapper.addAnalyzer(propertyName, attribute.analyzer());
			attributes.setObjectForKey(attribute, propertyName);
		}
		_attributes = attributes.immutableClone();
		return wrapper;
	}

	protected void registerNotifications() {
		NSNotificationCenter.defaultCenter().addObserver(this, 
				ERXSelectorUtilities.notificationSelector("_editingContextDidSaveChanges"), 
				EOEditingContext.EditingContextDidSaveChangesNotification, null);
	}

	public ERIndexModel model() {
		return _model;
	}

	private Analyzer analyzer() {
		return _analyzer;
	}

	private File indexDirectory() {
		return _indexDirectory;
	}

	private void index(ERIndexJob job) {
		try {
			synchronized (indexDirectory()) {
				IndexModifier modifier = new IndexModifier(indexDirectory(), analyzer(), true);
				for (Enumeration iter = job.deleted().objectEnumerator(); iter.hasMoreElements();) {
					Term term = (Term) iter.nextElement();
					modifier.deleteDocuments(term);
				}
				for (Enumeration iter = job.added().objectEnumerator(); iter.hasMoreElements();) {
					Document document = (Document) iter.nextElement();
					modifier.addDocument(document, analyzer());
				}
				modifier.flush();
				modifier.close();
			}
		} catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public void _editingContextDidSaveChanges(NSNotification n) {
		EOEditingContext ec = (EOEditingContext) n.object();
		if(ec.parentObjectStore() == ec.rootObjectStore()) {
			NSArray inserted = (NSArray) n.userInfo().objectForKey("inserted");
			NSArray updated = (NSArray) n.userInfo().objectForKey("updated");
			NSArray deleted = (NSArray) n.userInfo().objectForKey("deleted");

			deleteObjectsFromIndex(deleted);
			updateObjectsInIndex(updated);
			addObjectsToIndex(inserted);
		}
	}

	private NSArray attributes() {
		return _attributes.allValues();
	}

	public NSArray attributeNames() {
		return _attributes.allKeys();
	}
	
	protected NSArray addedDocumentsForObjects(NSArray objects) {
		NSMutableArray documents = new NSMutableArray();

		for(Enumeration e = objects.objectEnumerator(); e.hasMoreElements(); ) {
			EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
			if(handlesObject(eo)) {
				Document doc = createDocument(eo);
				if(doc != null) {
					documents.addObject(doc);
				}
			}
		}
		return documents;
	}

	protected Document createDocument(EOEnterpriseObject eo) {
		Document doc = new Document();
		NSArray attributes = attributes();
		for (Enumeration iter = attributes.objectEnumerator(); iter.hasMoreElements();) {
			ERIndexAttribute info = (ERIndexAttribute) iter.nextElement();
			String key = info.name();
			Object value = eo.valueForKeyPath(key);
			String stringValue = info.formatValue(value);
			if(stringValue != null) {
				Field field = new Field(key, stringValue, 
						info.store(), info.index(), info.termVector());
				doc.add(field);
			}
		}
		String pk = gidStringForObject(eo);
		doc.add(new Field(GID, pk, Field.Store.YES, Field.Index.UN_TOKENIZED));
		return doc;
	}

	protected NSArray deletedTermsForObjects(NSArray objects) {
		NSMutableArray terms = new NSMutableArray();
		Term term;
		for(Enumeration e = objects.objectEnumerator(); e.hasMoreElements(); ) {
			EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
			if(handlesObject(eo)) {
				term = createTerm(eo);
				if(term != null) {
					terms.addObject(term);
				}
			}
		}
		return terms;
	}

	protected boolean handlesObject(EOEnterpriseObject eo) {
		return true;
	}

	protected Term createTerm(EOEnterpriseObject eo) {
		Term term = null;
		String pk = gidStringForObject(eo);
		term = new Term(GID, pk);
		return term;
	}

	protected String gidStringForObject(EOEnterpriseObject eo) {
		String pk = ERXEOControlUtilities.primaryKeyStringForObject(eo);
		return eo.entityName() + ":" + pk;
	}

	protected EOEnterpriseObject objectForGidString(EOEditingContext ec, String gidString) {
		String s[] = gidString.split(":", 2);
		EOGlobalID gid = ERXEOControlUtilities.globalIDForString(ec, s[0], s[1]);
		return ec.faultForGlobalID(gid, ec);
	}

	protected void addObjectsToIndex(NSArray objects) {
		NSArray added = addedDocumentsForObjects(objects);
		addJob(added, NSArray.EmptyArray);
	}

	protected void updateObjectsInIndex(NSArray objects) {
		NSArray deleted = deletedTermsForObjects(objects);
		NSArray added = addedDocumentsForObjects(objects);
		addJob(added, deleted);
	}

	protected void deleteObjectsFromIndex(NSArray objects) {
		NSArray deleted = deletedTermsForObjects(objects);
		addJob(NSArray.EmptyArray, deleted);
	}

	protected void addJob(NSArray added, NSArray deleted) {
		_queue.enqueue(new ERIndexJob(added, deleted));
	}

	public NSArray find(EOEditingContext ec, String field, String queryString) {
		NSMutableArray result = new NSMutableArray();
		try {
			synchronized (indexDirectory()) {
				IndexReader reader = IndexReader.open(indexDirectory());
				Searcher searcher = new IndexSearcher(reader);
				Analyzer analyzer = analyzer();

				QueryParser parser = new QueryParser(field, analyzer);
				Query query = parser.parse(queryString);
				Hits hits = searcher.search(query);
				log.info("Searched for: " + query.toString(field));
				for (Iterator iter = hits.iterator(); iter.hasNext();) {
					Hit hit = (Hit) iter.next();
					String gidString = hit.getDocument().getField(GID).stringValue();
					EOEnterpriseObject eo = objectForGidString(ec, gidString);
					result.addObject(eo);
				}
			}
		} catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (ParseException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		return result;
	}

}
