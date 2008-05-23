package er.corebusinesslogic.audittrail;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
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
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXGenericRecord;
import er.extensions.ERXKeyGlobalID;
import er.extensions.ERXModelGroup;
import er.extensions.ERXProperties;
import er.extensions.ERXQ;
import er.extensions.ERXSelectorUtilities;
import er.extensions.ERXStringUtilities;
import er.extensions.ERXValueUtilities;

/**
 * Bracket for all single audit trail actions. It serves as a "shadow" of the object in question.
 * @author ak
 *
 */
public class ERCAuditTrail extends _ERCAuditTrail {
    
    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCAuditTrail.class);

    private static final String ERXAUDIT_KEYS = "ERXAuditKeys";

    public static final ERCAuditTrailClazz clazz = new ERCAuditTrailClazz();

    public interface Delegate {
    }

    public static class Handler {

        public class Configuration {
            public boolean isAudited = false;
            public NSMutableArray keys = new NSMutableArray();
            public NSMutableArray notificationKeys = new NSMutableArray();
            
            @Override
            public String toString() {
                return "{ isAudited =" + isAudited + "; keys = " + keys + "; notificationKeys = "+ notificationKeys + ";}";
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
                    if(entity.userInfo() != null && entity.userInfo().objectForKey(ERXAUDIT_KEYS) != null) {
                        configureEntity(entity);
                    }
                }
            }
            log.info("Configuration : " + configuration);
            NSNotificationCenter.defaultCenter().removeObserver(_handler, ERXModelGroup.ModelGroupAddedNotification, null);
            NSSelector sel = ERXSelectorUtilities.notificationSelector("handleChanges");
            NSNotificationCenter.defaultCenter().addObserver(_handler, sel, ERXEC.EditingContextWillSaveChangesNotification, null);
        }

        protected Configuration configureEntity(EOEntity entity) {
            Configuration config = configuration.objectForKey(entity.name());
            if(config == null) {
                config = new Configuration();
                configuration.setObjectForKey(config, entity.name());
            }
            if(entity.userInfo() != null) {
                Object object = entity.userInfo().objectForKey(ERXAUDIT_KEYS);
                String val = object != null ? object.toString() : null;
                if(val != null) {
                    NSArray keys = null;

                    if(val.length() == 0) {
                        keys = entity.classDescriptionForInstances().attributeKeys();
                    } else  {
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
                            if(rel != null) {
                                if(rel.isFlattened()) {
                                    throw new IllegalStateException("Can't handle flattened relations, use the definition: " + rel);
                                }
                                if(rel.isToMany()) {
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

        public void handleChanges(NSNotification n) {
            EOEditingContext ec = (EOEditingContext) n.object();
            if(ec.parentObjectStore() instanceof EOObjectStoreCoordinator) {
                ec.processRecentChanges();
                NSArray insertedObjects = (NSArray) ec.insertedObjects();
                NSArray updatedObjects = (NSArray) ec.updatedObjects();
                NSArray deletedObjects = (NSArray) ec.deletedObjects();
                handleChanges(ec, EOEditingContext.InsertedKey, insertedObjects);
                handleChanges(ec, EOEditingContext.UpdatedKey, updatedObjects);
                handleChanges(ec, EOEditingContext.DeletedKey, deletedObjects);
            }
        }

        protected void handleInsert(EOEditingContext ec, EOEnterpriseObject eo, Object newValue) {
            ERXGenericRecord rec = (ERXGenericRecord)eo;
            log.info("Insert: " + rec.permanentGlobalID() + " new state " + newValue);
        }

        protected void handleUpdate(EOEditingContext ec, EOEnterpriseObject eo) {
            NSArray keys = configuration.objectForKey(eo.entityName()).keys;
            NSDictionary committedSnapshotForObject = ec.committedSnapshotForObject(eo);
            NSDictionary changes = eo.changesFromSnapshot(committedSnapshotForObject);
            for (Enumeration e1 = changes.keyEnumerator(); e1.hasMoreElements();) {
                String key = (String) e1.nextElement();
                if(keys.containsObject(key)) {
                    handleUpdate(ec, eo, key, committedSnapshotForObject.objectForKey(key), changes.objectForKey(key));
                }
            }
        }

        protected void handleUpdate(EOEditingContext ec, EOEnterpriseObject eo, String keyPath, Object oldValue, Object newValue) {
            ERXGenericRecord rec = (ERXGenericRecord)eo;
            log.info("Update: " + rec.permanentGlobalID() + " " + keyPath + " from " + oldValue + " to " + newValue);
        }

        protected void handleDelete(EOEditingContext ec, EOEnterpriseObject eo, Object oldValue) {
            ERXGenericRecord rec = (ERXGenericRecord)eo;
            log.info("Delete: " + rec.permanentGlobalID() + " last state is " + oldValue);
        }

        protected void handleRemove(EOEditingContext ec, EOEnterpriseObject target, String keyPath, EOEnterpriseObject eo) {
            ERXGenericRecord rec = (ERXGenericRecord)target;
            log.info("Remove: " + rec.permanentGlobalID() + " " + keyPath + " eo " + eo);
        }

        protected void handleAdd(EOEditingContext ec, EOEnterpriseObject target, String keyPath, EOEnterpriseObject eo) {
            ERXGenericRecord rec = (ERXGenericRecord)target;
            log.info("Add: " + rec.permanentGlobalID() + " " + keyPath + " eo " + eo);
        }

        protected void handleUpdate(EOEditingContext ec, EOEnterpriseObject target, String keyPath, EOEnterpriseObject eo) {
            ERXGenericRecord rec = (ERXGenericRecord)target;
            EOEnterpriseObject oldValue = (EOEnterpriseObject) rec.valueForKeyPath(keyPath);
            log.info("Update: " + rec.permanentGlobalID() + " " + keyPath + " from " + oldValue + " to " + eo);
        }

        private void handleChange(EOEditingContext ec, String typeKey, EOEnterpriseObject eo) {
            if(typeKey.equals(EOEditingContext.UpdatedKey)) {
                handleUpdate(ec, eo);
            } else if (typeKey.equals(EOEditingContext.InsertedKey)) {
                handleInsert(ec, eo, eo.snapshot());
            } else if (typeKey.equals(EOEditingContext.DeletedKey)) {
                handleDelete(ec, eo, eo.snapshot());
            }
        }

        protected void handleChanges(EOEditingContext ec, String typeKey, NSArray objects) {
            if(objects == null) return;
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
                Configuration config = configuration.objectForKey(eo.entityName());

                if(config != null) {
                    if(config.isAudited) {
                        handleChange(ec, typeKey, eo);
                    } else {
                        for (Enumeration e1 = config.notificationKeys.objectEnumerator(); e1.hasMoreElements();) {
                            String key = (String) e1.nextElement();
                            EOEnterpriseObject target = (EOEnterpriseObject) eo.valueForKey(key);
                            EOEntity entity = ERXEOAccessUtilities.entityForEo(eo);
                            String inverse = entity.relationshipNamed(key).anyInverseRelationship().name();
                            if(typeKey.equals(EOEditingContext.UpdatedKey)) {
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

    }
    
    private static Handler _handler;
    public static void initialize() {
        _handler = new Handler();
        NSSelector sel = ERXSelectorUtilities.notificationSelector("modelGroupDidLoad");
        NSNotificationCenter.defaultCenter().addObserver(_handler, sel, ERXModelGroup.ModelGroupAddedNotification, null);
    }

    public static class ERCAuditTrailClazz extends _ERCAuditTrail._ERCAuditTrailClazz {

        public ERCAuditTrail auditTrailForObject(EOEditingContext ec, EOEnterpriseObject eo) {
            return auditTrailForGlobalID(ec, ec.globalIDForObject(eo));
        }

        public ERCAuditTrail auditTrailForGlobalID(EOEditingContext ec, EOGlobalID gid) {
            return (ERCAuditTrail) ERXEOControlUtilities.objectWithQualifier(ec, ENTITY_NAME, ERXQ.equals(Key.GID, gid));
        }

        public ERCAuditTrail createAuditTrailForObject(EOEditingContext ec, EOEnterpriseObject eo) {
            ERCAuditTrail trail = createAndInsertObject(ec);

            trail.setObject(eo);
            return trail;
        }
    }

    public interface Key extends _ERCAuditTrail.Key {

    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    public void setObject(EOEnterpriseObject eo) {
        EOGlobalID gid;
        if (gid() == null) {
            if (eo instanceof ERXGenericRecord) {
                gid = ((ERXGenericRecord) eo).permanentGlobalID();
            } else {
                throw new IllegalArgumentException("Can't handle non ERXGenericRecord");
            }
            ERXKeyGlobalID keyGID = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID) gid);
            setGid(keyGID);
        }
    }

    public EOEnterpriseObject object() {
        EOKeyGlobalID gid = gid().globalID();
        return editingContext().faultForGlobalID(gid, editingContext());
    }
}
