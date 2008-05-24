package er.corebusinesslogic.audittrail;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXEC;
import er.extensions.ERXEOAccessUtilities;
import er.extensions.ERXGenericRecord;
import er.extensions.ERXModelGroup;
import er.extensions.ERXSelectorUtilities;
import er.extensions.ERXValueUtilities;

public class ERCAuditTrailHandler {
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCAuditTrail.class);

    private static final String ERXAUDIT_KEYS = "ERXAuditKeys";

    private static ERCAuditTrailHandler _handler;

    public interface Delegate {
        
    }

    public static void initialize() {
        if(1 == 0) return;
        _handler = new ERCAuditTrailHandler();
        NSSelector sel = ERXSelectorUtilities.notificationSelector("modelGroupDidLoad");
        NSNotificationCenter.defaultCenter().addObserver(_handler, sel, ERXModelGroup.ModelGroupAddedNotification, null);
    }

    public class Configuration {
        
        public boolean isAudited = false;

        public NSMutableArray keys = new NSMutableArray();

        public NSMutableArray notificationKeys = new NSMutableArray();

        @Override
        public String toString() {
            return "{ isAudited =" + isAudited + "; keys = " + keys + "; notificationKeys = " + notificationKeys + ";}";
        }
    }

    private NSMutableDictionary<String, Configuration> configuration = new NSMutableDictionary<String, Configuration>();

    public void modelGroupDidLoad(NSNotification n) {
        configuration.removeAllObjects();
        EOModelGroup group = (EOModelGroup) n.object();
        for (Enumeration e = group.models().objectEnumerator(); e.hasMoreElements();) {
            EOModel model = (EOModel) e.nextElement();
            for (Enumeration e1 = model.entities().objectEnumerator(); e1.hasMoreElements();) {
                EOEntity entity = (EOEntity) e1.nextElement();
                if (entity.userInfo() != null && entity.userInfo().objectForKey(ERXAUDIT_KEYS) != null) {
                    configureEntity(entity);
                }
            }
        }
        log.info("Configuration : " + configuration);
        NSNotificationCenter.defaultCenter().removeObserver(_handler, ERXModelGroup.ModelGroupAddedNotification, null);
        NSSelector sel = ERXSelectorUtilities.notificationSelector("handleSave");
        NSNotificationCenter.defaultCenter().addObserver(_handler, sel, ERXEC.EditingContextWillSaveChangesNotification, null);
    }

    protected Configuration configureEntity(EOEntity entity) {
        Configuration config = configuration.objectForKey(entity.name());
        if (config == null) {
            config = new Configuration();
            configuration.setObjectForKey(config, entity.name());
        }
        if (entity.userInfo() != null) {
            Object object = entity.userInfo().objectForKey(ERXAUDIT_KEYS);
            String val = object != null ? object.toString() : null;
            if (val != null) {
                NSArray keys = null;

                if (val.length() == 0) {
                    keys = entity.classDescriptionForInstances().attributeKeys();
                } else {
                    keys = ERXValueUtilities.arrayValue(val);
                }
                config.isAudited = true;
                config.keys.addObjectsFromArray(keys);
                for (Enumeration e = config.keys.objectEnumerator(); e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    EOEntity source = entity;
                    // AK: for now this only handles non-flattened rels
                    for (Enumeration e1 = NSArray.componentsSeparatedByString(key, ".").objectEnumerator(); e1.hasMoreElements();) {
                        String part = (String) e1.nextElement();
                        EORelationship rel = source._relationshipForPath(key);
                        if (rel != null) {
                            if (rel.isFlattened()) {
                                throw new IllegalStateException("Can't handle flattened relations, use the definition: " + rel);
                            }
                            if (rel.isToMany()) {
                                EOEntity destinationEntity = rel.destinationEntity();
                                Configuration destinationConfiguration = configureEntity(destinationEntity);
                                String inverseName = rel.anyInverseRelationship().name();
                                destinationConfiguration.notificationKeys.addObject(inverseName);
                                source = rel.destinationEntity();
                            } else {
                                config.keys.addObject(rel.name());
                            }
                        }
                    }
                }
            }
        }
        return config;
    }

    public void handleSave(NSNotification n) {
        EOEditingContext ec = (EOEditingContext) n.object();
        if (ec.parentObjectStore() instanceof EOObjectStoreCoordinator) {
            ec.processRecentChanges();
            NSArray insertedObjects = (NSArray) ec.insertedObjects();
            NSArray updatedObjects = (NSArray) ec.updatedObjects();
            NSArray deletedObjects = (NSArray) ec.deletedObjects();
            handleSave(ec, EOEditingContext.InsertedKey, insertedObjects);
            handleSave(ec, EOEditingContext.UpdatedKey, updatedObjects);
            handleSave(ec, EOEditingContext.DeletedKey, deletedObjects);
        }
    }

    protected void handleUpdate(EOEditingContext ec, EOEnterpriseObject eo) {
        NSArray keys = configuration.objectForKey(eo.entityName()).keys;
        NSDictionary committedSnapshotForObject = ec.committedSnapshotForObject(eo);
        NSDictionary changes = eo.changesFromSnapshot(committedSnapshotForObject);
        for (Enumeration e1 = changes.keyEnumerator(); e1.hasMoreElements();) {
            String key = (String) e1.nextElement();
            if (keys.containsObject(key)) {
                handleUpdate(ec, eo, key, committedSnapshotForObject.objectForKey(key), changes.objectForKey(key));
            }
        }
    }

    private void handleSave(EOEditingContext ec, String typeKey, EOEnterpriseObject eo) {
        if (typeKey.equals(EOEditingContext.UpdatedKey)) {
            handleUpdate(ec, eo);
        } else if (typeKey.equals(EOEditingContext.InsertedKey)) {
            handleInsert(ec, eo, eo.snapshot());
        } else if (typeKey.equals(EOEditingContext.DeletedKey)) {
            handleDelete(ec, eo, eo.snapshot());
        }
    }

    protected void handleSave(EOEditingContext ec, String typeKey, NSArray objects) {
        if (objects == null)
            return;
        for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
            Configuration config = configuration.objectForKey(eo.entityName());

            if (config != null) {
                if (config.isAudited) {
                    handleSave(ec, typeKey, eo);
                } else {
                    for (Enumeration e1 = config.notificationKeys.objectEnumerator(); e1.hasMoreElements();) {
                        String key = (String) e1.nextElement();
                        EOEnterpriseObject target = (EOEnterpriseObject) eo.valueForKey(key);
                        EOEntity entity = ERXEOAccessUtilities.entityForEo(eo);
                        String inverse = entity.relationshipNamed(key).anyInverseRelationship().name();
                        if (typeKey.equals(EOEditingContext.UpdatedKey)) {
                            handleUpdate(ec, target, inverse, eo);
                        } else if (typeKey.equals(EOEditingContext.InsertedKey)) {
                            handleAdd(ec, target, inverse, eo);
                        } else if (typeKey.equals(EOEditingContext.DeletedKey)) {
                            target = (EOEnterpriseObject) ec.committedSnapshotForObject(eo).valueForKey(key);
                            handleRemove(ec, target, inverse, eo);
                        }
                    }
                }
            }
        }
    }

    protected void handleInsert(EOEditingContext ec, EOEnterpriseObject eo, Object newValue) {
        handleChange(ec, eo, ERCAuditTrailType.INSERTED, null, newValue, null);
    }

    protected void handleUpdate(EOEditingContext ec, EOEnterpriseObject eo, String keyPath, Object oldValue, Object newValue) {
        handleChange(ec, eo, ERCAuditTrailType.UPDATED, keyPath, oldValue, newValue);
    }

    protected void handleDelete(EOEditingContext ec, EOEnterpriseObject eo, Object oldValue) {
        handleChange(ec, eo, ERCAuditTrailType.DELETED, null, null, oldValue);
    }

    protected void handleRemove(EOEditingContext ec, EOEnterpriseObject target, String keyPath, EOEnterpriseObject eo) {
        ERXGenericRecord rec = (ERXGenericRecord) target;
        handleChange(ec, target, ERCAuditTrailType.REMOVED, keyPath, null, eo);
    }

    protected void handleAdd(EOEditingContext ec, EOEnterpriseObject target, String keyPath, EOEnterpriseObject eo) {
        ERXGenericRecord rec = (ERXGenericRecord) target;
        handleChange(ec, target, ERCAuditTrailType.ADDED, keyPath, eo, null);
    }

    protected void handleUpdate(EOEditingContext ec, EOEnterpriseObject target, String keyPath, EOEnterpriseObject eo) {
        ERXGenericRecord rec = (ERXGenericRecord) target;
        EOEnterpriseObject oldValue = (EOEnterpriseObject) rec.valueForKeyPath(keyPath);
        handleChange(ec, target, ERCAuditTrailType.UPDATED, keyPath, oldValue, oldValue);
    }
    
    protected void handleChange(EOEditingContext ec, EOEnterpriseObject eo, ERCAuditTrailType type, String keyPath, Object oldValue, Object newValue) {
        ERXGenericRecord rec = (ERXGenericRecord) eo;
        ERCAuditTrail trail = ERCAuditTrail.clazz.auditTrailForObject(ec, eo);
        if(trail == null) {
            trail = ERCAuditTrail.clazz.createAuditTrailForObject(ec, eo);
        }
        log.info(trail + " " + type + ": " + rec.permanentGlobalID() + " " + keyPath + " from " + oldValue + " to " + newValue);
        trail.createEntry(type, keyPath, oldValue, newValue);
    }
}