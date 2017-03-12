package er.indexing;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.lucene.document.Document;

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
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSSet;

import er.extensions.concurrency.ERXAsyncQueue;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * 
 * <pre>
 *   File Documents.indexModel is 
 *   {
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
 * }
 *
 * </pre>
 * 
 * @author ak
 * 
 */

public class ERAutoIndex extends ERIndex {

    protected class ConfigurationEntry {

        public boolean active = false;

        public NSMutableArray<String> keys = new NSMutableArray();

        public NSMutableArray<String> notificationKeys = new NSMutableArray();

        @Override
        public String toString() {
            return "{ active = " + active + "; keys = " + keys + "; notificationKeys = " + notificationKeys + ";}";
        }
    }

    /**
     * This class encapsulates the index configuration logic and configures this Index 
     * from the indexModel file associated with this ERIndex instance.
     * One instance of Configuration is created during the ERAutoIndex constructor.
     * 
     * The Configuration class contains a private instance of a configuration dictionary
     *
     */
    protected class Configuration {

    	// Holds the configuration
    	// Each entity has an entry where the key is the entity name and the object is a ConfigurationEntry for that entity
        private final NSMutableDictionary<String, ConfigurationEntry> configuration = new NSMutableDictionary<>();

        protected void initFromDictionary(NSDictionary indexDef) {
            String store = (String) indexDef.objectForKey("store");
            
            // Set the index storage location
            setStore(store);

            // Clear the current configuration
            configuration.clear();
            
            // Get the list of entities specififed in the indexModel definition
            NSArray<String> entities = (NSArray) indexDef.objectForKey("entities");
            
            // Creates IndexAttributes, one for each entry in indexModel.properties
            createAttributes(indexDef);

            for (String entityName : entities) {
                ConfigurationEntry config = configureEntity(entityName, attributeNames());
                config.active = true;
            }
            log.info(configuration);
        }

        /**
         * Creates the lucene index attributes from the properties dictionary contained
         * in the indexModel defintion. Each property is a key or keypath.
         * @param indexDef
         */
        protected void createAttributes(NSDictionary indexDef) {
        	// Get the properties dictionary which is one element of the indexModel dictionary
            NSDictionary properties = (NSDictionary) indexDef.objectForKey("properties");
            
            // For each property in indexModel, create configuration attributes
            for (Enumeration names = properties.keyEnumerator(); names.hasMoreElements();) {
                String propertyName = (String) names.nextElement();
                NSDictionary propertyDefinition = (NSDictionary) properties.objectForKey(propertyName);
                createAttribute(propertyName, propertyDefinition);
            }
        }

        /**
         * @param entityName entity to be indexed
         * @param keys attributes (keys or keypaths) to be indexed
         */
        protected ConfigurationEntry configureEntity(String entityName, NSArray keys) {

            // Get ConfigurationEntry for the entity if it already exists
            ConfigurationEntry config = configuration.objectForKey(entityName);

            // If not already existing, create it
            if (config == null) {
                config = new ConfigurationEntry();
                configuration.setObjectForKey(config, entityName);
            }

            EOEntity source = ERXEOAccessUtilities.entityNamed(null, entityName);
            for (Enumeration e = keys.objectEnumerator(); e.hasMoreElements();) {
                String keyPath = (String) e.nextElement();
                configureKeyPath(config, keyPath, source);
            }
            return config;
        }

        private ConfigurationEntry configureKeyPath(ConfigurationEntry config, String keyPath, EOEntity source) {
            String key = ERXStringUtilities.firstPropertyKeyInKeyPath(keyPath);
            String rest = ERXStringUtilities.keyPathWithoutFirstProperty(keyPath);
            EORelationship rel = source._relationshipForPath(key);
            if (rel != null) {
                if (rel.isFlattened()) {
                    ConfigurationEntry c = configureKeyPath(config, rel.definition() + (rest != null ? "." + rest : ""), source);
                    return c;
                }
                EOEntity destinationEntity = rel.destinationEntity();

                ConfigurationEntry destinationConfiguration;
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

        public ConfigurationEntry entryForKey(String key) {
            return configuration.objectForKey(key);
        }

        public void setEntryForKey(ConfigurationEntry entry, String key) {
            configuration.setObjectForKey(entry, key);
        }

        public void clear() {
            configuration.clear();
        }

    }

    protected class AutoTransactionHandler extends TransactionHandler {

        @Override
		public void submit(Transaction transaction) {
            if(false) {
                _queue.enqueue(transaction);
            } else {
                index(transaction);
            }
        }

        @Override
		public void _handleChanges(NSNotification n) {
            EOEditingContext ec = (EOEditingContext) n.object();
            if (ec.parentObjectStore() == ec.rootObjectStore()) {

                String notificationName = n.name();
                if (notificationName.equals(ERXEC.EditingContextWillSaveChangesNotification)) {
                    ec.processRecentChanges();
                    NSArray inserted = ec.insertedObjects();
                    NSArray updated = ec.updatedObjects();
                    updated = ERXArrayUtilities.arrayMinusArray(updated, inserted);
                    NSArray deleted = ec.deletedObjects();

                    Transaction transaction = new Transaction(ec);

                    NSMutableSet<EOEnterpriseObject> deletedHandledObjects = new NSMutableSet<>();
                    NSMutableSet<EOEnterpriseObject> addedHandledObjects = new NSMutableSet<>();

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
                    submit(transaction);

                } else if (notificationName.equals(ERXEC.EditingContextDidRevertChanges) || notificationName.equals(ERXEC.EditingContextFailedToSaveChanges)) {
                    activeChanges.remove(ec);
                }
            }
        }

        private NSArray<EOEnterpriseObject> handledObjects(NSArray<EOEnterpriseObject> objects) {
            NSMutableArray<EOEnterpriseObject> result = new NSMutableArray<>(objects.count());
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

        private final NSMutableSet<String> _warned = new NSMutableSet();

        protected NSArray indexableObjectsForObject(String type, EOEnterpriseObject object) {
            ERXGenericRecord eo = (ERXGenericRecord) object;
            EOEditingContext ec = eo.editingContext();
            NSMutableSet<EOEnterpriseObject> result = new NSMutableSet();
            String entityName = eo.entityName();
            ConfigurationEntry config = _configuration.entryForKey(entityName);
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
                                // AK: I wish I could, but when a relationship
                                // is
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

    }

    private static ERXAsyncQueue<Transaction> _queue;

    private NSSet<String> _entities = NSSet.EmptySet;

    private final Configuration _configuration = new Configuration();

    public ERAutoIndex(String name, NSDictionary indexDef) {
        super(name);
        
        // Ensures that the first instance of ERAutoIndex creates the singleton thread
        // for processing the queue of transactions
        synchronized (ERIndex.class) {
            if (_queue == null) {
                _queue = new ERXAsyncQueue<Transaction>() {

                    @Override
					public void process(Transaction transaction) {
                        transaction.handler().index(transaction);
                    }
                };
                
                // Set the name of the thread
                _queue.setName(KEY);
                
                // Start the thread
                _queue.start();
            }
        }
        
        _entities = new NSMutableSet();
        
         _configuration.initFromDictionary(indexDef);
        setTransactionHandler(new AutoTransactionHandler());
    }

    protected NSSet entities() {
        return _entities;
    }

    public void reindexAllObjects() {
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
                    NSArray<Document> documents = addedDocumentsForObjects(objects);
                    Transaction transaction = new Transaction(ec);
                    handler().addObjectsToIndex(transaction, objects);
                }
            } finally {
                ec.unlock();
            }
            log.info("Indexing " + entityName + " took: " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    protected boolean handlesEntity(String name) {
        ConfigurationEntry config = _configuration.entryForKey(name);
        return config != null && config.active;
    }

    @Override
	protected boolean handlesObject(EOEnterpriseObject eo) {
        return handlesEntity(eo.entityName());
    }
}
