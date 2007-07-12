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
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSet;

import er.extensions.ERXAsyncQueue;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXFetchSpecificationBatchIterator;
import er.extensions.ERXSelectorUtilities;
import er.indexing.ERIndexJob.Command;

/**
 * 
 * <pre>{
 *   Document = {
 *     // index class to use, default is er.indexing.ERIndex
 *     index = com.foo.SomeIndexClass;
 *     // url for the index files (currently unused)
 *     store = "file://tmp/Document";
 *     // type of the index (currently unused)
 *     type = "filed|db";
 *     // if the index should be double-buffered (currently unused)
 *     buffered = false|true;
 *     // entities in this index
 *     entities = (Asset, File, Media);
 *     // properties to index, these are key paths off the objects
 *     // and are also used for the names of the index fields.
 *     // these don't need to be attributes or relationships
 *     // but can also be simple methods. In fact, if you have mutliple
 *     // entities in your index, you will need to support a common set of 
 *     // these properties
 *     properties = {
 *         someAttribute = {
 *             // if the index should be stored
 *             store = "NO|YES|COMPRESS";
 *             // if the index is tokenized
 *             index = "NO|TOKENIZED|UN_TOKENIZED|NO_NORMS";
 *             // no idea what this does. consult the lucene docs
 *             termVector = "NO|YES|WITH_POSITIONS|WITH_OFFSETS|WITH_POSITIONS_OFFSETS";
 *             // which analyzer class to use. For german stuff, you'll
 *             // use the org.apache.lucene.analysis.de.GermanAnalyzer.
 *             analyzer = com.foo.SomeAnalyzerClass;
 *             // optional formater for the value
 *             format = com.foo.SomeFormatClass;
 *             // optional number format for the value
 *             numberformat = "0";
 *             // optional date format for the value
 *             dateformat = "yyyy.mm.dd";
 *         };
 *         someRelatedObject.name = {...};
 *         someRelationship.name = {...};
 *     };
 *   }
 * }</pre>
 * 
 * @author ak
 *
 */
public class ERIndex {

	private Logger log = Logger.getLogger(ERIndex.class);

	private static final String GID = "EOGlobalID";
	private static final String KEY = "ERIndexing";

	private static ERXAsyncQueue _queue;
	private File _indexDirectory;
	private Analyzer _analyzer;
	private NSDictionary _attributes;
	private ERIndexModel _model;
	private NSSet _entities;
	
	public ERIndex(ERIndexModel model, NSDictionary indexDef) {
		synchronized (ERIndex.class) {
			if(_queue == null) {
				_queue = new ERXAsyncQueue() {

					public void process(Object object) {
						ERIndexJob job = (ERIndexJob)object;
						job.index().index(job);
					}
				};
				_queue.setName(KEY);
				_queue.start();
			}
		}
		_model = model;
		initFromDictionary(indexDef);
		registerNotifications();
	}

	public void indexAllObjects() {
		clear();
		for (Enumeration names = entities().objectEnumerator(); names.hasMoreElements();) {
			String entityName = (String) names.nextElement();
			long start = System.currentTimeMillis();
			int treshhold = 10;
			EOEditingContext ec = ERXEC.newEditingContext();
			ec.lock();
			try {
				EOFetchSpecification fs = new EOFetchSpecification(entityName, null, null);
				ERXFetchSpecificationBatchIterator iterator = new ERXFetchSpecificationBatchIterator(fs);
				iterator.setEditingContext(ec);
				while(iterator.hasNextBatch()) {
					NSArray objects = iterator.nextBatch();
					if(iterator.currentBatchIndex() % treshhold == 0) {
						ec.unlock();
						// ec.dispose();
						ec = ERXEC.newEditingContext();
						ec.lock();
						iterator.setEditingContext(ec);
					}
					addObjectsToIndex(objects);
				}
			} finally {
				ec.unlock();
			}
			log.info("Indexing " + entityName + " took: " + (System.currentTimeMillis() - start) + " ms");
		}
	}
	
	protected void initFromDictionary(NSDictionary indexDef) {
		File indexDirectory = new File((String) indexDef.objectForKey("store"));
		_indexDirectory = indexDirectory;
		_analyzer = createAnalyzer(indexDef);
		NSArray entities = (NSArray) indexDef.objectForKey("entities");
		_entities = new NSMutableSet(entities);
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

	protected File indexDirectory() {
		return _indexDirectory;
	}
	
	protected NSSet entities() {
		return _entities;
	}

	private void index(ERIndexJob job) {
		try {
			synchronized (indexDirectory()) {
				log.info("Indexing: "  + job.command() + ": " + job.objects().count());
				boolean create = job.command() == Command.CLEAR;
				IndexModifier modifier = new IndexModifier(indexDirectory(), analyzer(), create);
				if(job.command() == Command.DELETE) {
					for (Enumeration iter = job.objects().objectEnumerator(); iter.hasMoreElements();) {
						Term term = (Term) iter.nextElement();
						modifier.deleteDocuments(term);
					}
				} else if(job.command() == Command.ADD) {
					for (Enumeration iter = job.objects().objectEnumerator(); iter.hasMoreElements();) {
						Document document = (Document) iter.nextElement();
						modifier.addDocument(document, analyzer());
					}
				} else if(job.command() == Command.CLEAR) {
					// nothing, we already cleared the index
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

			deleteObjectsFromIndex(indexableObjectsForObjects(deleted));
			updateObjectsInIndex(indexableObjectsForObjects(updated));
			addObjectsToIndex(indexableObjectsForObjects(inserted));
		}
	}

	/**
	 * Override this to respond to the editingContextDidSaveChanges notification.
	 * You would want to re-index documents for which a related tag name changed, for example.
	 * @param objects
	 * @return
	 */
	protected NSArray indexableObjectsForObjects(NSArray objects) {
		return objects;
	}
	
	private NSArray attributes() {
		return _attributes.allValues();
	}

	public NSArray attributeNames() {
		return _attributes.allKeys();
	}

	public void clear() {
		_queue.enqueue(new ERIndexJob(this, Command.CLEAR, NSArray.EmptyArray));
	}
	
	private ERIndexAttribute attributeNamed(String fieldName) {
		return (ERIndexAttribute) _attributes.objectForKey(fieldName);
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
			// log.debug(value);
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
		return handlesEntity(eo.entityName());
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
		if(deleted.count() > 0) {
			_queue.enqueue(new ERIndexJob(this, Command.DELETE, deleted));
		}
		if(added.count() > 0) {
			_queue.enqueue(new ERIndexJob(this, Command.ADD, added));
		}
	}

	public NSArray find(EOEditingContext ec, String fieldName, String queryString) {
		NSMutableArray result = new NSMutableArray();
		try {
			synchronized (indexDirectory()) {
				long start = System.currentTimeMillis();
				IndexReader reader = IndexReader.open(indexDirectory());
				Searcher searcher = new IndexSearcher(reader);
				Analyzer analyzer = attributeNamed(fieldName).analyzer();

				QueryParser parser = new QueryParser(fieldName, analyzer);
				Query query = parser.parse(queryString);
				Hits hits = searcher.search(query);
				log.info("Searched for: " + query.toString(fieldName) + " in  " + (System.currentTimeMillis() - start) + " ms");
				for (Iterator iter = hits.iterator(); iter.hasNext();) {
					Hit hit = (Hit) iter.next();
					String gidString = hit.getDocument().getField(GID).stringValue();
					EOEnterpriseObject eo = objectForGidString(ec, gidString);
					result.addObject(eo);
				}
				log.info("Returning " + result.count() + " after " + (System.currentTimeMillis() - start) + " ms");
			}
		} catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (ParseException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		return result;
	}

	public boolean handlesEntity(String name) {
		return _entities.containsObject(name);
	}

	public NSArray terms(String fieldName) {
		NSMutableSet result = new NSMutableSet();
		synchronized (indexDirectory()) {
			TermEnum terms = null;
			try {
				IndexReader reader = IndexReader.open(indexDirectory());
				terms = reader.terms(new Term(fieldName, ""));
				while (fieldName.equals(terms.term().field())) {
					result.addObject(terms.term().text());
					if (!terms.next()) {
						break;
					}
				}
			} catch (IOException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} finally {
				if(terms != null) {
					try {
						terms.close();
					} catch (IOException e) {
						throw NSForwardException._runtimeExceptionForThrowable(e);
					}
				}
			}
		}

		return result.allObjects();
	}
}
