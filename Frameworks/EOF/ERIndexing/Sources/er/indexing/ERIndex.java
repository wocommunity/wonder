package er.indexing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Parameter;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.concurrency.ERXAsyncQueue;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKeyGlobalID;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.indexing.storage.ERIDirectory;

/**
 * 
 * <pre>
 * {
 *   Document = {
 *     // index class to use, default is er.indexing.ERIndex
 *     index = com.foo.SomeIndexClass;
 *     // url for the index files
 *     store = &quot;file://tmp/Document&quot;;
 *     // if the index should be double-buffered (currently unused)
 *     buffered = false|true;
 *     // entities in this index
 *     entities = (Asset, File, Media);
 *     // properties to index, these are key paths off the objects
 *     // and are also used for the names of the index fields.
 *     // these don't need to be attributes or relationships
 *     // but can also be simple methods. In fact, if you have multiple
 *     // entities in your index, you will need to support a common set of 
 *     // these properties
 *     properties = {
 *         someAttribute = {
 *             // if the index should be stored
 *             store = &quot;NO|YES|COMPRESS&quot;;
 *             // if the index is tokenized
 *             index = &quot;NO|TOKENIZED|UN_TOKENIZED|NO_NORMS&quot;;
 *             // no idea what this does. consult the lucene docs
 *             termVector = &quot;NO|YES|WITH_POSITIONS|WITH_OFFSETS|WITH_POSITIONS_OFFSETS&quot;;
 *             // which analyzer class to use. For german stuff, you'll
 *             // use the org.apache.lucene.analysis.de.GermanAnalyzer.
 *             analyzer = com.foo.SomeAnalyzerClass;
 *             // optional formater for the value
 *             format = com.foo.SomeFormatClass;
 *             // optional number format for the value
 *             numberformat = &quot;0&quot;;
 *             // optional date format for the value
 *             dateformat = &quot;yyyy.mm.dd&quot;;
 *         };
 *         someRelatedObject.name = {...};
 *         someRelationship.name = {...};
 *     };
 *   }
 * }
 * </pre>
 * 
 * @author ak
 * 
 */
public class ERIndex {

    private Logger log;

    public static String IndexingStartedNotification = "ERIndexingStartedNotification";

    public static String IndexingEndedNotification = "ERIndexingEndedNotification";

    public static String IndexingFailedNotification = "ERIndexingFailedNotification";

    private static final String GID = "EOGlobalID";

    private static final String KEY = "ERIndexing";

    private static ERXAsyncQueue<Transaction> _queue;

    public class IndexDocument implements NSKeyValueCoding {

        private Document _document;

        private NSMutableDictionary<String, String> _values = new NSMutableDictionary<String, String>();

        public IndexDocument(Document document) {
            _document = document;
        }

        public void takeValueForKey(Object value, String key) {
            _document.getField(key).setValue((String) key);
        }

        public Object valueForKey(String key) {
            return _document.get(key);
        }

        public Document document() {
            return _document;
        }

        public void save() {
            // TODO Auto-generated method stub

        }

        public void revert() {
            // TODO Auto-generated method stub

        }

        public void delete() {
            // TODO Auto-generated method stub

        }

    }

    private class IndexAttribute {
        String _name;

        TermVector _termVector;

        Store _store;

        Index _index;

        Analyzer _analyzer;

        Format _format;

        ERIndex _model;

        IndexAttribute(ERIndex index, String name, NSDictionary dict) {
            _name = name;
            _termVector = (TermVector) classValue(dict, "termVector", TermVector.class, "YES");
            _store = (Store) classValue(dict, "store", Store.class, "NO");
            _index = (Index) classValue(dict, "index", Index.class, "TOKENIZED");
            String analyzerClass = (String) dict.objectForKey("analyzer");
            if (analyzerClass == null) {
                analyzerClass = StandardAnalyzer.class.getName();
            }
            _analyzer = (Analyzer) create(analyzerClass);
            if (_analyzer == null && name.matches("\\w+_(\\w+)")) {
                // String locale = name.substring(name.lastIndexOf('_') + 1);
            }
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

        public String formatValue(Object value) {
            if (_format != null) {
                return _format.format(value);
            }
            if (value instanceof Number) {
                return NumberTools.longToString(((Number) value).longValue());
            }
            if (value instanceof Date) {
                return DateTools.dateToString((Date) value, Resolution.MILLISECOND);
            }
            if (value instanceof NSArray) {
                return ((NSArray) value).componentsJoinedByString(" ");
            }
            return (value != null ? value.toString() : null);
        }
    }

    protected static class Command extends Parameter {

        protected Command(String name) {
            super(name);
        }

        protected static Command ADD = new Command("ADD");

        protected static Command DELETE = new Command("DELETE");
    }

    private class Job {

        private Command _command;

        private NSArray _objects;

        public Job(Command command, NSArray objects) {
            _command = command;
            _objects = objects;
        }

        public NSArray objects() {
            return _objects;
        }

        public Command command() {
            return _command;
        }
    }

    public class Transaction {

        private NSMutableArray<Job> _jobs = new NSMutableArray<Job>();

        private EOEditingContext _editingContext;

        private boolean _clear = false;

        private int _objectCount = 0;

        private Transaction(EOEditingContext ec) {
            _editingContext = ec;
        }

        public void clear() {
            _clear = true;
        }

        public void addJob(Command command, NSArray objects) {
            if (objects.count() > 0) {
                _objectCount += objects.count();
                _jobs.addObject(new Job(command, objects));
            }
        }

        public EOEditingContext editingContext() {
            return _editingContext;
        }

        public NSArray<Job> jobs() {
            return _jobs;
        }

        public String toString() {
            if (hasClear()) {
                return "Transaction@" + hashCode() + " clear";
            }
            return "Transaction@" + (editingContext() != null ? editingContext().hashCode() : null) + "@" + hashCode() + " jobs: " + jobs().count() + " objects: " + _objectCount;
        }

        public boolean hasClear() {
            return _clear;
        }

        private ERIndex index() {
            return ERIndex.this;
        }
    }

    public class Configuration {

        public boolean active = false;

        public NSMutableArray<String> keys = new NSMutableArray();

        public NSMutableArray<String> notificationKeys = new NSMutableArray();

        @Override
        public String toString() {
            return "{ active = " + active + "; keys = " + keys + "; notificationKeys = " + notificationKeys + ";}";
        }
    }

    private NSMutableDictionary<String, Configuration> configuration = new NSMutableDictionary<String, Configuration>();

    private Directory _indexDirectory;

    private Analyzer _analyzer;

    private NSDictionary _attributes;

    private ERIndexModel _model;

    private NSSet _entities;

    private String _name;

    private EOEditingContext ec;

    public ERIndex(ERIndexModel model, String name, NSDictionary indexDef) {
        log = Logger.getLogger(ERIndex.class.getName() + "." + name);
        synchronized (ERIndex.class) {
            if (_queue == null) {
                _queue = new ERXAsyncQueue<Transaction>() {

                    public void process(Transaction transaction) {
                        transaction.index().index(transaction);
                    }
                };
                _queue.setName(KEY);
                _queue.start();
            }
        }
        _model = model;
        _name = name;
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
                while (iterator.hasNextBatch()) {
                    NSArray objects = iterator.nextBatch();
                    if (iterator.currentBatchIndex() % treshhold == 0) {
                        ec.unlock();
                        // ec.dispose();
                        ec = ERXEC.newEditingContext();
                        ec.lock();
                        iterator.setEditingContext(ec);
                    }
                    addObjectsToIndex(ec, objects);
                }
            } finally {
                ec.unlock();
            }
            log.info("Indexing " + entityName + " took: " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    protected void initFromDictionary(NSDictionary indexDef) {
        try {
            String store = (String) indexDef.objectForKey("store");
            if (store.startsWith("file://")) {
                File indexDirectory = new File(new URL(store).getFile());
                _indexDirectory = FSDirectory.getDirectory(indexDirectory);
            } else {
                EOEditingContext ec = ERXEC.newEditingContext();

                ec.lock();
                try {
                    _indexDirectory = ERIDirectory.clazz.directoryForName(ec, store);
                } finally {
                    ec.unlock();
                }
            }
        } catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        _analyzer = createAnalyzer(indexDef);
        configuration.clear();
        NSArray<String> entities = (NSArray) indexDef.objectForKey("entities");

        for (String entityName : entities) {
            Configuration config = configureEntity(entityName, attributeNames());
            config.active = true;
        }
        log.info(configuration);
    }

    protected Configuration configureEntity(String entityName, NSArray keys) {

        _entities = new NSMutableSet();
        Configuration config = configuration.objectForKey(entityName);
        if (config == null) {
            config = new Configuration();
            configuration.setObjectForKey(config, entityName);
        }

        EOEntity source = ERXEOAccessUtilities.entityNamed(null, entityName);
        for (Enumeration e = keys.objectEnumerator(); e.hasMoreElements();) {
            String keyPath = (String) e.nextElement();
            configureKeyPath(config, keyPath, source);
        }
        return config;
    }

    private Configuration configureKeyPath(Configuration config, String keyPath, EOEntity source) {
        String key = ERXStringUtilities.firstPropertyKeyInKeyPath(keyPath);
        String rest = ERXStringUtilities.keyPathWithoutFirstProperty(keyPath);
        EORelationship rel = source._relationshipForPath(key);
        if (rel != null) {
            if (rel.isFlattened()) {
                Configuration c = configureKeyPath(config, rel.definition() + (rest != null ? "." + rest : ""), source);
                return c;
            }
            EOEntity destinationEntity = rel.destinationEntity();

            Configuration destinationConfiguration;
            if (rest != null) {
                destinationConfiguration = configureEntity(destinationEntity.name(), new NSArray(rest));
            } else {
                destinationConfiguration = configureEntity(destinationEntity.name(), new NSArray());
            }
            String inverseName = rel.anyInverseRelationship().name();
            destinationConfiguration.notificationKeys.addObject(inverseName);
        } else {
            config.keys.addObject(key);
        }
        return config;
    }

    protected Analyzer createAnalyzer(NSDictionary indexDef) {
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        NSMutableDictionary attributes = new NSMutableDictionary();
        NSDictionary properties = (NSDictionary) indexDef.objectForKey("properties");
        for (Enumeration names = properties.keyEnumerator(); names.hasMoreElements();) {
            String propertyName = (String) names.nextElement();
            NSDictionary propertyDefinition = (NSDictionary) properties.objectForKey(propertyName);
            IndexAttribute attribute = new IndexAttribute(this, propertyName, propertyDefinition);
            wrapper.addAnalyzer(propertyName, attribute.analyzer());
            attributes.setObjectForKey(attribute, propertyName);
        }
        _attributes = attributes.immutableClone();
        return wrapper;
    }

    private void registerNotification(String notificationName, String selectorName) {
        NSNotificationCenter.defaultCenter().addObserver(this, ERXSelectorUtilities.notificationSelector(selectorName), notificationName, null);
    }

    protected void registerNotifications() {
        registerNotification(ERXEC.EditingContextWillSaveChangesNotification, "_handleChanges");
        registerNotification(ERXEC.EditingContextDidSaveChangesNotification, "_handleChanges");
        registerNotification(ERXEC.EditingContextDidRevertChanges, "_handleChanges");
        registerNotification(ERXEC.EditingContextFailedToSaveChanges, "_handleChanges");
    }

    public ERIndexModel model() {
        return _model;
    }

    private Analyzer analyzer() {
        return _analyzer;
    }

    protected Directory indexDirectory() {
        return _indexDirectory;
    }

    private IndexReader _reader;

    private IndexReader indexReader() throws CorruptIndexException, IOException {
        if (_reader == null) {
            _reader = IndexReader.open(indexDirectory());
        }
        if (!_reader.isCurrent()) {
            _reader = _reader.reopen();
        }
        return _reader;
    }

    protected NSSet entities() {
        return _entities;
    }

    private void index(Transaction transaction) {
        try {
            NSNotificationCenter.defaultCenter().postNotification(IndexingStartedNotification, transaction);
            boolean create = transaction.hasClear();
            if (create) {
                log.warn("Clearing index");
            } else if (transaction.jobs().count() == 0) {
                NSNotificationCenter.defaultCenter().postNotification(IndexingEndedNotification, transaction);
                return;
            }
            long start = System.currentTimeMillis();
            log.info("Running " + transaction);
            if (!create && !indexDirectory().fileExists("segments.gen")) {
                log.error("segments did not exist but create wasn't called");
                create = true;
            }
            IndexWriter modifier = new IndexWriter(indexDirectory(), analyzer(), create);
            for (Job job : transaction.jobs()) {
                log.info("Indexing: " + job.command() + " " + job.objects().count());
                if (job.command() == Command.DELETE) {
                    for (Enumeration iter = job.objects().objectEnumerator(); iter.hasMoreElements();) {
                        Term term = (Term) iter.nextElement();
                        modifier.deleteDocuments(term);
                    }
                } else if (job.command() == Command.ADD) {
                    for (Enumeration iter = job.objects().objectEnumerator(); iter.hasMoreElements();) {
                        Document document = (Document) iter.nextElement();
                        modifier.addDocument(document, analyzer());
                    }
                }
                log.info("Done: " + job.command() + " " + job.objects().count());
            }
            modifier.flush();
            modifier.close();
            NSNotificationCenter.defaultCenter().postNotification(IndexingEndedNotification, transaction);
            log.info("Finished in " + (System.currentTimeMillis() - start) / 1000 + "s: " + transaction);
        } catch (IOException e) {
            NSNotificationCenter.defaultCenter().postNotification(IndexingFailedNotification, transaction);
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    private Map<EOEditingContext, Transaction> activeChanges = new WeakHashMap<EOEditingContext, Transaction>();

    public void _handleChanges(NSNotification n) {
        EOEditingContext ec = (EOEditingContext) n.object();
        if (ec.parentObjectStore() == ec.rootObjectStore()) {

            String notificationName = n.name();
            if (notificationName.equals(ERXEC.EditingContextWillSaveChangesNotification)) {
                ec.processRecentChanges();
                NSArray inserted = (NSArray) ec.insertedObjects();
                NSArray updated = (NSArray) ec.updatedObjects();
                updated = ERXArrayUtilities.arrayMinusArray(updated, inserted);
                NSArray deleted = (NSArray) ec.deletedObjects();

                Transaction transaction = new Transaction(ec);

                NSMutableSet<EOEnterpriseObject> deletedHandledObjects = new NSMutableSet<EOEnterpriseObject>();
                NSMutableSet<EOEnterpriseObject> addedHandledObjects = new NSMutableSet<EOEnterpriseObject>();

                // first handle active objects
                NSArray<EOEnterpriseObject> directObjects;

                directObjects = handledObjects(deleted);
                deleted = ERXArrayUtilities.arrayMinusArray(deleted, directObjects);
                deletedHandledObjects.addObjectsFromArray(directObjects);

                directObjects = handledObjects(updated);
                updated = ERXArrayUtilities.arrayMinusArray(updated, directObjects);
                addedHandledObjects.addObjectsFromArray(directObjects);
                deletedHandledObjects.addObjectsFromArray(directObjects);

                directObjects = handledObjects(inserted);
                inserted = ERXArrayUtilities.arrayMinusArray(inserted, directObjects);
                addedHandledObjects.addObjectsFromArray(directObjects);

                NSArray<EOEnterpriseObject> indirectObjects;
                indirectObjects = indexableObjectsForObjects(EOEditingContext.UpdatedKey, updated);
                deletedHandledObjects.addObjectsFromArray(indirectObjects);
                addedHandledObjects.addObjectsFromArray(indirectObjects);

                indirectObjects = indexableObjectsForObjects(EOEditingContext.InsertedKey, inserted);
                deletedHandledObjects.addObjectsFromArray(indirectObjects);
                addedHandledObjects.addObjectsFromArray(indirectObjects);

                indirectObjects = indexableObjectsForObjects(EOEditingContext.DeletedKey, deleted);
                deletedHandledObjects.addObjectsFromArray(indirectObjects);
                addedHandledObjects.addObjectsFromArray(indirectObjects);

                deleteObjectsFromIndex(transaction, deletedHandledObjects.allObjects());
                addObjectsToIndex(transaction, addedHandledObjects.allObjects());

                activeChanges.put(ec, transaction);

            } else if (notificationName.equals(ERXEC.EditingContextDidSaveChangesNotification)) {
                Transaction transaction = activeChanges.get(ec);
                if (transaction != null) {
                    activeChanges.remove(ec);
                }
                _queue.enqueue(transaction);

            } else if (notificationName.equals(ERXEC.EditingContextDidRevertChanges) || notificationName.equals(ERXEC.EditingContextFailedToSaveChanges)) {
                activeChanges.remove(ec);
            }
        }
    }

    private NSArray<EOEnterpriseObject> handledObjects(NSArray<EOEnterpriseObject> objects) {
        NSMutableArray<EOEnterpriseObject> result = new NSMutableArray<EOEnterpriseObject>(objects.count());
        for (EOEnterpriseObject eo : objects) {
            if (handlesEntity(eo.entityName())) {
                result.addObject(eo);
            }
        }
        return result;
    }

    protected NSArray indexableObjectsForObjects(String type, NSArray<EOEnterpriseObject> objects) {
        NSMutableSet<EOEnterpriseObject> result = new NSMutableSet();
        for (EOEnterpriseObject eo : objects) {
            NSArray targetObjects = indexableObjectsForObject(type, eo);
            result.addObjectsFromArray(targetObjects);
        }
        return result.allObjects();
    }

    private NSMutableSet<String> _warned = new NSMutableSet();

    protected NSArray indexableObjectsForObject(String type, EOEnterpriseObject object) {
        ERXGenericRecord eo = (ERXGenericRecord) object;
        EOEditingContext ec = eo.editingContext();
        NSMutableSet<EOEnterpriseObject> result = new NSMutableSet();
        String entityName = eo.entityName();
        Configuration config = configuration.objectForKey(entityName);
        if (config != null) {
            if (!config.active) {
                for (Enumeration e1 = config.notificationKeys.objectEnumerator(); e1.hasMoreElements();) {
                    String key = (String) e1.nextElement();
                    Object value = null;

                    if (type.equals(EOEditingContext.DeletedKey)) {
                        value = ec.committedSnapshotForObject(eo);
                    }

                    EOEntity source = ERXEOAccessUtilities.entityForEo(eo);

                    if (source.classPropertyNames().containsObject(key)) {
                        value = eo.valueForKey(key);
                    } else {
                        if (eo.isNewObject()) {
                            if (!_warned.containsObject(entityName)) {
                                log.error("We currently don't support unsaved related objects for this entity: " + entityName);
                                _warned.addObject(entityName);
                            }
                        } else {
                            EORelationship rel = source.anyRelationshipNamed(key);
                            EOKeyGlobalID sourceGlobalID = (EOKeyGlobalID) ec.globalIDForObject(eo);
                            // AK: I wish I could, but when a relationship is
                            // not a class prop, there's nothing we can do.
                            // value =
                            // ec.arrayFaultWithSourceGlobalID(sourceGlobalID,
                            // rel.name(), ec);
                            EOFetchSpecification fs = new EOFetchSpecification(rel.destinationEntity().name(), null, null);
                            NSMutableArray<EOQualifier> qualifiers = new NSMutableArray(rel.joins().count());
                            NSDictionary pk = source.primaryKeyForGlobalID(sourceGlobalID);
                            for (Iterator iterator = rel.joins().iterator(); iterator.hasNext();) {
                                EOJoin join = (EOJoin) iterator.next();
                                Object pkValue = pk.objectForKey(join.sourceAttribute().name());
                                EOKeyValueQualifier qualifier = new EOKeyValueQualifier(join.destinationAttribute().name(), EOQualifier.QualifierOperatorEqual, pkValue);
                                qualifiers.addObject(qualifier);
                            }
                            fs.setQualifier(qualifiers.count() == 1 ? qualifiers.lastObject() : new EOAndQualifier(qualifiers));
                            value = ec.objectsWithFetchSpecification(fs);
                        }
                    }
                    if (value != null) {
                        NSArray<EOEnterpriseObject> eos = (value instanceof EOEnterpriseObject ? new NSArray(value) : (NSArray) value);
                        for (EOEnterpriseObject target : eos) {
                            NSArray targetObjects = indexableObjectsForObject(EOEditingContext.UpdatedKey, target);
                            result.addObjectsFromArray(targetObjects);
                        }
                    }
                    if (!result.isEmpty() && log.isDebugEnabled()) {
                        log.debug("re-index: " + eo + "->" + result);
                    }
                }
            } else {
                result.addObject(eo);
            }
        }
        return result.allObjects();
    }

    private NSArray<IndexAttribute> attributes() {
        return _attributes.allValues();
    }

    public String name() {
        return _name;
    }

    public NSArray<String> attributeNames() {
        return _attributes.allKeys();
    }

    public void clear() {
        Transaction transaction = new Transaction(null);
        transaction.clear();
        _queue.enqueue(transaction);
    }

    private IndexAttribute attributeNamed(String fieldName) {
        return (IndexAttribute) _attributes.objectForKey(fieldName);
    }

    protected NSArray addedDocumentsForObjects(NSArray objects) {
        NSMutableArray documents = new NSMutableArray();

        for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
            if (handlesObject(eo)) {
                Document doc = createDocumentForObject(eo);
                if (doc != null) {
                    documents.addObject(doc);
                }
            }
        }
        return documents;
    }

    protected Document createDocumentForObject(EOEnterpriseObject eo) {
        EOKeyGlobalID gid = ((ERXGenericRecord) eo).permanentGlobalID();
        IndexDocument document = createDocumentForGlobalID(gid);
        Document doc = document.document();
        for (IndexAttribute info : attributes()) {
            String key = info.name();
            Object value = eo.valueForKeyPath(key);

            if (log.isDebugEnabled()) {
                log.info(key + "->" + value);
            }

            String stringValue = info.formatValue(value);
            if (stringValue != null) {
                Field field = new Field(key, stringValue, info.store(), info.index(), info.termVector());
                document.document().add(field);
            }
        }
        return doc;
    }

    protected NSArray deletedTermsForObjects(NSArray objects) {
        NSMutableArray terms = new NSMutableArray();
        Term term;
        for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
            if (handlesObject(eo)) {
                term = createTerm(eo);
                if (term != null) {
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
        EOKeyGlobalID gid = ((ERXGenericRecord) eo).permanentGlobalID();
        String pk = ERXKeyGlobalID.globalIDForGID(gid).asString();
        return pk;
    }

    protected EOEnterpriseObject objectForGidString(EOEditingContext ec, String gidString) {
        EOKeyGlobalID gid = ERXKeyGlobalID.fromString(gidString).globalID();
        return ec.faultForGlobalID(gid, ec);
    }

    protected void addObjectsToIndex(EOEditingContext ec, NSArray objects) {
        NSArray added = addedDocumentsForObjects(objects);
        Transaction transaction = new Transaction(ec);

        transaction.addJob(Command.ADD, added);

        _queue.enqueue(transaction);
    }

    protected void addObjectsToIndex(Transaction transaction, NSArray objects) {
        NSArray added = addedDocumentsForObjects(objects);
        transaction.addJob(Command.ADD, added);
    }

    protected void deleteObjectsFromIndex(Transaction transaction, NSArray objects) {
        NSArray deleted = deletedTermsForObjects(objects);
        transaction.addJob(Command.DELETE, deleted);
    }

    private Query queryForQualifier(EOQualifier qualifier) throws ParseException {
        EOKeyValueQualifier q = (EOKeyValueQualifier) qualifier;
        return queryForString(q.key(), (String) q.value());
    }

    private Query queryForString(String fieldName, String queryString) throws ParseException {
        Analyzer analyzer = attributeNamed(fieldName).analyzer();
        QueryParser parser = new QueryParser(fieldName, analyzer());
        Query query = parser.parse(queryString);

        return query;
    }

    private Query queryForString(String queryString) throws ParseException {
        return null;
    }

    private NSArray<EOKeyGlobalID> findGlobalIDs(Query query) {
        NSMutableArray<EOKeyGlobalID> result = new NSMutableArray();
        long start = System.currentTimeMillis();
        try {
            IndexReader reader = indexReader();
            Searcher searcher = new IndexSearcher(reader);
            Hits hits = searcher.search(query);
            log.info("Searched for: " + query + " in  " + (System.currentTimeMillis() - start) + " ms");
            for (Iterator iter = hits.iterator(); iter.hasNext();) {
                Hit hit = (Hit) iter.next();
                String gidString = hit.getDocument().getField(GID).stringValue();
                EOKeyGlobalID gid = ERXKeyGlobalID.fromString(gidString).globalID();
                result.addObject(gid);
            }
            log.info("Returning " + result.count() + " after " + (System.currentTimeMillis() - start) + " ms");
            return result;
        } catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    private NSArray<EOEnterpriseObject> findObjects(EOEditingContext ec, Query query) {
        return ERXEOControlUtilities.faultsForGlobalIDs(ec, findGlobalIDs(query));
    }

    public NSArray<EOKeyGlobalID> findGlobalIDs(String queryString) {
        try {
            Query query = queryForString(queryString);
            return findGlobalIDs(query);
        } catch (ParseException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    public NSArray<EOKeyGlobalID> findGlobalIDs(EOQualifier qualifier) {
        try {
            Query query = queryForQualifier(qualifier);
            return findGlobalIDs(query);
        } catch (ParseException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    public IndexDocument findDocument(EOKeyGlobalID globalID) {
        NSMutableArray<Document> result = new NSMutableArray();
        long start = System.currentTimeMillis();
        try {
            IndexReader reader = indexReader();
            Searcher searcher = new IndexSearcher(reader);
            String pk = ERXKeyGlobalID.globalIDForGID(globalID).asString();
            BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term(GID, pk)), Occur.MUST);
            Hits hits = searcher.search(query);

            log.info("Searched for: " + query.toString(GID) + " in  " + (System.currentTimeMillis() - start) + " ms");
            for (Iterator iter = hits.iterator(); iter.hasNext();) {
                Hit hit = (Hit) iter.next();
                result.addObject(hit.getDocument());
            }
            log.info("Returning " + result.count() + " after " + (System.currentTimeMillis() - start) + " ms");
        } catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        return new IndexDocument(result.lastObject());
    }

    public NSArray findObjects(EOEditingContext ec, EOQualifier qualifier) {
        try {
            Query query = queryForQualifier(qualifier);
            return findObjects(ec, query);
        } catch (ParseException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    public NSArray findObjects(EOEditingContext ec, String queryString) {
        try {
            Query query = queryForString(queryString);
            NSArray<EOKeyGlobalID> gids = findGlobalIDs(query);
            return ERXEOControlUtilities.faultsForGlobalIDs(ec, gids);
        } catch (ParseException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    public boolean handlesEntity(String name) {
        Configuration config = configuration.objectForKey(name);
        return config != null && config.active;
    }

    public NSArray terms(String fieldName) {
        NSMutableSet result = new NSMutableSet();
        TermEnum terms = null;
        try {
            IndexReader reader = indexReader();
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
            if (terms != null) {
                try {
                    terms.close();
                } catch (IOException e) {
                    throw NSForwardException._runtimeExceptionForThrowable(e);
                }
            }
        }

        return result.allObjects();
    }

    public IndexDocument documentForGlobalID(EOKeyGlobalID globalID) {
        return findDocument(globalID);
    }

    public IndexDocument createDocumentForGlobalID(EOKeyGlobalID globalID) {
        Document doc = new Document();
        String pk = ERXKeyGlobalID.globalIDForGID(globalID).asString();
        doc.add(new Field(GID, pk, Field.Store.YES, Field.Index.UN_TOKENIZED));
        return new IndexDocument(doc);
    }
}
