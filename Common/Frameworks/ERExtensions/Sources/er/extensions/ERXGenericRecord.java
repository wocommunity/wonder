/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXGenericRecord.java created by patrice on Thu 20-Jul-2000 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.lang.*;
import java.util.*;
import org.apache.log4j.Category;

public class ERXGenericRecord extends EOGenericRecord implements ERXGuardedObjectInterface, ERXGeneratesPrimaryKeyInterface {

    ///////////////////////////  log4j category  ///////////////////////////
    // Did* Transaction categories
    public static final Category tranCatDidInsert = Category.getInstance("er.transaction.eo.did.insert.ERXGenericRecord");
    public static final Category tranCatDidDelete = Category.getInstance("er.transaction.eo.did.delete.ERXGenericRecord");
    public static final Category tranCatDidUpdate = Category.getInstance("er.transaction.eo.did.update.ERXGenericRecord");
    // Will* Transaction categories
    public static final Category tranCatWillInsert = Category.getInstance("er.transaction.eo.will.insert.ERXGenericRecord");
    public static final Category tranCatWillDelete = Category.getInstance("er.transaction.eo.will.delete.ERXGenericRecord");
    public static final Category tranCatWillUpdate = Category.getInstance("er.transaction.eo.will.update.ERXGenericRecord");

    public static final Category fix = Category.getInstance("er.extensions.fixes.ERXGenericRecord");

    public static final Category validation = Category.getInstance("er.eo.validation.ERXGenericRecord");
    public static final Category validationException = Category.getInstance("er.eo.validationException.ERXGenericRecord");

    public static final Category cat = Category.getInstance("er.eo.ERXGenericRecord");
    public static final Category cloneCat =  Category.getInstance("er.eo.clone.ERXGenericRecord");
    public static final Category willChangeCat =
        Category.getInstance("er.eo.willChange.ERXGenericRecord");
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String KEY_MARKER="** KEY_MARKER **";

    // This is used to fix a bug in EOF where an object has a to-one to an abstract entity.  If the EO has not been fetch yet an exception
    // can occur that will manifest itself as '*** -[NSConcreteMutableDictionary setObject:forKey:]: attempt to insert nil key'  This method
    // works around that bug if called before storedValueForKey.


 public static void willFixToOneRelationship(String key, EOEnterpriseObject object) {
     /*
        if (object!=null) {
            if (fix.isDebugEnabled())
                fix.debug("WillFixToOneRelationship key: " + key);
            EOEntity objectEntity = EOModelGroup.defaultGroup(EOUtilities.entityForObject(), object);
            EORelationship relationship = objectEntity.relationshipNamed(key);
            if (!relationship.isToMany() && relationship.destinationEntity().isAbstractEntity()) {
                EOEditingContext ec = object.editingContext();
                if (ec!=null) {
                    EOAttribute attribute = (EOAttribute)relationship.sourceAttributes().objectAtIndex(0);
                    EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, objectEntity.model().name());
                    if (context!=null) {
                        if (context.snapshotForGlobalID(ec.globalIDForObject(object)) != null) {
                            Object value = (context.snapshotForGlobalID(ec.globalIDForObject(object))).valueForKey(attribute.name());
                            if (!(value instanceof NSKeyValueCoding.Null)) {
                                if (fix.isDebugEnabled())
                                    fix.debug("Not null, value: " + value + "( "+relationship.destinationEntity().name() +")");
                                Object[] gidValue=new Object[] { value };
                                EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(relationship.destinationEntity().name(), gidValue);
                                if (ec.objectForGlobalID(gid) == null || EOFaultHandler.isFault(ec.objectForGlobalID(gid))
                                    || context.snapshotForGlobalID(gid, ec.fetchTimestamp()) == null) {
                                    // In cases where the abstract entity is itself in the middle of an inheritance tree, the GID we put together above
                                    // does not work, and the EO is refetched every time (this was exposed in LandlordResponse.landlordProperty
                                    // the GID created 
                                    // we need to walk up the hierarchy to create [GID: Property 1053]
                                    EOEntity superEntity =relationship.destinationEntity();
                                    EOKeyGlobalID superGID=null;
                                    while (superEntity.parentEntity()!=null) {
                                        superEntity = superEntity.parentEntity();
                                        gid = EOKeyGlobalID.globalIDWithEntityName(superEntity.name(), gidValue);
                                        if (ec.objectForGlobalID(gid) != null && !EOFaultHandler.isFault(ec.objectForGlobalID(gid)) &&
                                            context.snapshotForGlobalID(gid, ec.fetchTimestamp()) != null) {
                                            superGID=gid;
                                            break;
                                        }
                                    }
                                    if (superGID ==null) { // we didn't find anything!
                                        /*                                        
                                        for (Enumeration e = relationship.destinationEntity().subEntities().objectEnumerator(); e.hasMoreElements();) {
                                            EOEntity subEntity = (EOEntity)e.nextElement();
                                            EOKeyValueQualifier primaryKeyQualifier =
                                                new EOKeyValueQualifier(((EOAttribute)subEntity.primaryKeyAttributes().objectAtIndex(0)).name(),
                                                                        EOQualifier.QualifierOperatorEqual,
                                                                        value);
                                            EOQualifier restrictQualifier = subEntity.restrictingQualifier();
                                            EOQualifier fetchQualifier = (restrictQualifier != null ?
                                                                          new EOAndQualifier(new NSArray(new Object[] {primaryKeyQualifier, restrictQualifier})) :
                                                                          (EOQualifier)primaryKeyQualifier);
                                            if (fix.isDebugEnabled())
                                                fix.debug("Trying entity: " + subEntity.name() + " and pk value: " + value + " fetchQualifier: " +
                                                          fetchQualifier);
                                            NSArray objects = ec.objectsWithFetchSpecification(new EOFetchSpecification(subEntity.name(), fetchQualifier, null));
                                            if (objects.count() > 0) {
                                                if (fix.isDebugEnabled()) {
                                                    fix.debug("Got eo: " + objects.objectAtIndex(0));
                                                    fix.debug("GID = "+ec.globalIDForObject((EOEnterpriseObject)objects.objectAtIndex(0)));
                                                }
                                                break;
                                            } else if (fix.isDebugEnabled()) {
                                                fix.debug("Didn't find anything");
                                            }
                                        }
                                        **

                                        EOEntity e=relationship.destinationEntity();
                                        EOKeyValueQualifier fetchQualifier =
                                                new EOKeyValueQualifier(((EOAttribute)e.primaryKeyAttributes().objectAtIndex(0)).name(),
                                                                        EOQualifier.QualifierOperatorEqual,
                                                                        value);
                                        if (fix.isDebugEnabled())
                                            fix.debug("Trying entity: " + e.name() + " and pk value: " + value + " fetchQualifier: " +
                                                      fetchQualifier);
                                        NSArray objects = ec.objectsWithFetchSpecification(new EOFetchSpecification(e.name(), fetchQualifier, null));
                                        if (objects.count() > 0) {
                                            if (fix.isDebugEnabled()) {
                                                fix.debug("Got eo: " + objects.objectAtIndex(0));
                                                fix.debug("GID = "+ec.globalIDForObject((EOEnterpriseObject)objects.objectAtIndex(0)));
                                            }
                                        } else if (fix.isDebugEnabled()) {
                                            fix.debug("Didn't find anything");
                                        }

                                    } else if (fix.isDebugEnabled()) {
                                        fix.debug("Found GID for super entity "+ superGID);
                                    }
                                } else if (fix.isDebugEnabled()) {
                                    fix.debug("Object registered and not fault");
                                }
                            } else if (fix.isDebugEnabled()) {
                                fix.debug("Foreign key is null. Optional ToOne.");
                            }
                        } else if (fix.isDebugEnabled()) {
                            fix.debug("EO is new.  No snapshot in the db context");
                        }
                    } else
                        fix.error("null EODatabaseContext for "+objectEntity.model().name());
                } else {
                    fix.error("Null editing context for "+object);
                }
            } else {
                fix.error("Trying to fix a relationship that is not a ToOne or that points to a non-Abstract Entity. Key: " + key + " object" + object);
            }
        } else {
            fix.error("Null object");
        }
*/
    }




                                        
    // Another work around for the relationship to an abstract.  This one also handles toMany relationships.
    // This was submitted, although it has not been tested yet.

    protected void _willFixRelationship(String key, EOEnterpriseObject object) {
/*                                        
        EOEntity objectEntity = EOModelGroup.defaultGroup(EOUtilities.entityForObject(), object);
        EORelationship relationship = objectEntity.relationshipNamed(key);

        if (relationship.destinationEntity().isAbstractEntity()) {
            // we potentially need to worry about any type of relationship to an abstract entity
            EOQualifier qual = null;

            if (!relationship.isToMany()) {
                // okay we have a  T O - O N E   relationship
                // this block of code is more or less Max's original code except the qualifier
                // creation is simplified - letting EOF do the hard work for us.
                EOEditingContext ec = object.editingContext();
                EOEntity destinationEntity = relationship.destinationEntity();
                EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec,objectEntity.model().name());
                EOGlobalID objectGlobalID = (EOGlobalID)ec.globalIDForObject(object);
                NSDictionary snapshot;
                EOAttribute attribute = (EOAttribute)relationship.sourceAttributes().lastObject();
                if ((snapshot = context.snapshotForGlobalID(objectGlobalID)) != null) {
                    Object value = snapshot.valueForKey(attribute.name());
                    if (!(value instanceof NSKeyValueCoding.Null)) {
                        // we've got a valid relationship - check if we've already fetched it....
                        EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(destinationEntity.name(), new Object[] {value});
                        EOEnterpriseObject myObj = ec.objectForGlobalID(gid);
                        if (myObj == null || EOFaultHandler.isFault(myObj)) {
                            // okay - we need to fetch the object so build the correct qualifier
                            NSDictionary myPKDict = destinationEntity.primaryKeyForGlobalID(gid);
                            qual = destinationEntity.qualifierForPrimaryKey(myPKDict);
                        }
                    }
                }
            } else {
                // we have a T O  - M A N Y  relationship - need to treat slightly differently
                //due to differences with snapshots & fetching.... this is the new code...
                EOEditingContext ec = object.editingContext();
                EOEntity destinationEntity = relationship.destinationEntity();
                EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec,objectEntity.model().name());
                //cat.debug
                if (ec == null) {
                    throw new RuntimeException("Attempting to fix the relationship of an object with no reference to an editing context -- most likely because it was deleted in the current transaction: " + object);
                }
                EOGlobalID objectGlobalID = (EOGlobalID)ec.globalIDForObject(object);
                NSArray snapshot;
                if ((snapshot = context.snapshotForSourceGlobalID(objectGlobalID,key)) != null) {
                    if ((snapshot.count() > 0)) {
                        // we have a snapshot for the rel - now check an arbitrary object to see if we've already fetched
                        Object myObj = ec.objectForGlobalID((EOKeyGlobalID)snapshot.lastObject());
                        if (myObj == null || EOFaultHandler.isFault(myObj)) {
                            // okay - we need to fetch the objects so build the correct qualifier
                            NSDictionary myDict = objectEntity.primaryKeyForGlobalID((EOKeyGlobalID)objectGlobalID);
                            qual = relationship.qualifierWithSourceRow(myDict);
                        }
                    }
                }
            }
            if (qual != null) {
                EOEntity destinationEntity = relationship.destinationEntity();
                EOEditingContext ec = object.editingContext();
                // right - we've got a qualifier so we need to fetch......
                NSArray objects = ec.objectsWithFetchSpecification(new EOFetchSpecification(destinationEntity.name(), qual, null));
            }
        }
*/
    }

    // Tests if a relationship is a fault. Useful for testing if a toOne abstract relationship key is a fault.
/*
 public boolean isAbstractRelationshipKeyFault(String relationshipKey) {
        boolean isFault = false;
        EOEntity objectEntity = EOModelGroup.defaultGroup(EOUtilities.entityForObject(), this);
        EORelationship relationship = objectEntity.relationshipNamed(relationshipKey);
        if (!relationship.isToMany() && relationship.destinationEntity().isAbstractEntity()) {
            EOEditingContext ec = editingContext();
            if (ec!=null) {
                EOAttribute attribute = (EOAttribute)relationship.sourceAttributes().objectAtIndex(0);
                EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, objectEntity.model().name());
                if (context!=null) {
                    if (context.snapshotForGlobalID(ec.globalIDForObject(this)) != null) {
                        Object value = (context.snapshotForGlobalID(ec.globalIDForObject(this))).valueForKey(attribute.name());
                        if (!(value instanceof NSKeyValueCoding.Null)) {
                            if (fix.isDebugEnabled())
                                fix.debug("Not null, value: " + value + "( "+relationship.destinationEntity().name() +")");
                            Object[] gidValue=new Object[] { value };
                            EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(relationship.destinationEntity().name(), gidValue);
                            if (ec.objectForGlobalID(gid) == null || EOFaultHandler.isFault(ec.objectForGlobalID(gid))
                                || context.snapshotForGlobalID(gid, ec.fetchTimestamp()) == null) {
                                // In cases where the abstract entity is itself in the middle of an inheritance tree, the GID we put together above
                                // does not work, and the EO is refetched every time (this was exposed in LandlordResponse.landlordProperty
                                // the GID created
                                // we need to walk up the hierarchy to create [GID: Property 1053]
                                EOEntity superEntity =relationship.destinationEntity();
                                EOKeyGlobalID superGID=null;
                                while (superEntity.parentEntity()!=null) {
                                    superEntity = superEntity.parentEntity();
                                    gid = EOKeyGlobalID.globalIDWithEntityName(superEntity.name(), gidValue);
                                    if (ec.objectForGlobalID(gid) != null && !EOFaultHandler.isFault(ec.objectForGlobalID(gid)) &&
                                        context.snapshotForGlobalID(gid, ec.fetchTimestamp()) != null) {
                                        superGID=gid;
                                        break;
                                    }
                                }
                                if (superGID ==null) { // we didn't find anything!
                                    isFault = true;
                                }
                           }
                            }
                        }
}
}
}
    return isFault;
    }
*/
    //-------------------------------------------------------------------------------------
    // Basic permission methods.  Overridden by subClasses.
    public boolean canDelete() { return true; }
    public boolean canUpdate() { return true; }


    // Used by the delegate to notify objects at the begining of a saveChanges.
    public void willDelete() throws NSValidation.ValidationException {
        if(canDelete() == false) {
            throw new RuntimeException("The ERXGenericRecord "+this+" cannot be deleted.");
        }
        if (tranCatWillDelete.isDebugEnabled())
            tranCatWillDelete.debug("Object:" + description());
    }
    public void willInsert() {
        /* Disabling this check by default -- it's causing problems for objects created and deleted
        in the same transaction */
         if (tranCatWillInsert.isDebugEnabled()) {
             /* check that all the to manies have an array */
             for (Enumeration e=toManyRelationshipKeys().objectEnumerator(); e.hasMoreElements();) {
                 String key=(String)e.nextElement();
                 Object o=storedValueForKey(key);
                 if (o==null || !EOFaultHandler.isFault(o) && o instanceof NSKeyValueCoding.Null) {
                     tranCatWillInsert.error("Found illegal value in to many "+key+" for "+this+": "+o);
                 }
             }
             tranCatWillInsert.debug("Object:" + description());
         }
         trimSpaces();
    }

    public void willUpdate() {
        /* Disabling this check by default -- it's causing problems for objects created and deleted
        in the same transaction */
         if (tranCatWillUpdate.isDebugEnabled()) {
             /* check that all the to manies have an array */
             for (Enumeration e=toManyRelationshipKeys().objectEnumerator(); e.hasMoreElements();) {
                 String key=(String)e.nextElement();
                 Object o=storedValueForKey(key);
                 if (o==null || !EOFaultHandler.isFault(o) && o instanceof NSKeyValueCoding.Null) {
                     tranCatWillUpdate.error("Found illegal value in to many "+key+" for "+this+": "+o);
                 }
             }
             if (tranCatWillUpdate.isDebugEnabled())
                 tranCatWillUpdate.debug("Object:" + description() + " changes: " + changesFromCommittedSnapshot());
         }
        trimSpaces();
    }

    // Called by the delegate on editingContextDidMergeChanges
    public void flushCaches() {}

    // did delete needs the EC, since it has been set to null by the time didDelete is called
    public void didDelete(EOEditingContext ec) {
        if (tranCatDidDelete.isDebugEnabled())
            tranCatDidDelete.debug("Object:" + description());
    }
    public void didUpdate() {
        if (tranCatDidUpdate.isDebugEnabled())
            tranCatDidUpdate.debug("Object:" + description());
    }
    public void didInsert() {
        if (tranCatDidInsert.isDebugEnabled())
            tranCatDidInsert.debug("Object:" + description());
    }

    public void addObjectsToBothSidesOfRelationshipWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                addObjectToBothSidesOfRelationshipWithKey(eo, key);
            }
        }
    }

    public void removeObjectsFromBothSidesOfRelationshipWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                removeObjectFromBothSidesOfRelationshipWithKey(eo, key);
             }
        }
    }

    static boolean _raiseOnMissingEditingContextDelegate = ERXUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXRaiseOnMissingEditingContextDelegate"), true);
				    
    public boolean _checkEditingContextDelegate(EOEditingContext editingContext) {
	Object delegate=editingContext.delegate();
	
	if (delegate==null) {
	    EOObjectStore parent = editingContext.parentObjectStore();
	    if(!_raiseOnMissingEditingContextDelegate && parent != null && parent instanceof EOEditingContext) {
		Object parentDelegate=((EOEditingContext)parent).delegate();
		if(parentDelegate != null && (parentDelegate instanceof ERXEditingContextDelegate)) {
		    editingContext.setDelegate(parentDelegate);
		    cat.info("Found null delegate. Setting to the parent's delegate.");
		    return true;
		}
	    }
	    if(!_raiseOnMissingEditingContextDelegate) {
		cat.warn("Found null delegate. I will fix this for now by setting it to ERXExtensions.defaultDelegate");
		ERXExtensions.setDefaultDelegate(editingContext);
		return true;
	    } else {
		throw new RuntimeException("Found null delegate. You can disable this check by setting er.extensions.ERXRaiseOnMissingEditingContextDelegate=false in your WebObjects.properties");
	    }
	}
	if (delegate!=null && !(delegate instanceof ERXEditingContextDelegate)) {
	    if(!_raiseOnMissingEditingContextDelegate) {
		cat.warn("Found unexpected delegate class: "+delegate.getClass().getName());
		return true;
	    } else {
		throw new RuntimeException("Found unexpected delegate class. You can disable this check by setting er.extensions.ERXRaiseOnMissingEditingContextDelegate=false in your WebObjects.properties");
	    }
	}
	return false;

    }
    public void awakeFromClientUpdate(EOEditingContext editingContext) {
	if(_checkEditingContextDelegate(editingContext)) {
	    // willUpdate();
	}
	super.awakeFromClientUpdate(editingContext);
    }
    public void awakeFromInsertion(EOEditingContext editingContext) {
	if(_checkEditingContextDelegate(editingContext)) {
	    // willInsert();
	}
	super.awakeFromInsertion(editingContext);
    }
    public void awakeFromFetch(EOEditingContext editingContext) {
	if(_checkEditingContextDelegate(editingContext)) {
	    // willUpdate();
	}
	super.awakeFromFetch(editingContext);
    }

				    
    // --------------------------------------------------------------------------------------------
    // Debugging aids -- turn off during production
    
/*
    public Object storedValueForKey(String key) {
        // FIXME: turn this off during production
        if (!allPropertyKeys().containsObject(key))
            throw new RuntimeException("********* Tried to access storedValueForKey on "+entityName()+" on a non class property: "+key);
        Object value = super.storedValueForKey(key);
        if (toManyRelationshipKeys().containsObject(key)) {
            if (value instanceof EONullValue) {
                System.err.println(entityName() + ".storedValueForKey(" + key + ") = EONullValue");
            }
            if (value == null) {
                System.err.println(entityName() + ".storedValueForKey(" + key + ") = null");
            }
        }
        return value;
    }
    public void takeStoredValueForKey(Object value, String key) {
        // FIXME: turn this off during production
        if (!allPropertyKeys().containsObject(key)) {
            System.err.println("********* Tried to takeStoredValueForKey on "+entityName()+" on a non class property: "+key);
            throw new RuntimeException("********* Tried to takeStoredValueForKey on "+entityName()+" on a non class property: "+key);
        }
        if (value == null && toManyRelationshipKeys().containsObject(key)) {
            System.err.println(new RuntimeException("********* Tried to takeStoredValueForKey of null on "
                                                    +entityName()+" on a toManyRelationship key: "+key));
        }
        if (value != null &&
            (value instanceof EONullValue) &&
            toManyRelationshipKeys().containsObject(key)) {
            System.err.println( new RuntimeException("********* Tried to takeStoredValueForKey of EONullValue on "+entityName()
                                                     +" on a toManyRelationship key: "+key));
        }
        super.takeStoredValueForKey(value,key);
    }
*/
     public void addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation eo, String key) {
        if (eo!=null &&
            ((EOEnterpriseObject)eo).editingContext()!=editingContext() &&
            !(editingContext() instanceof EOSharedEditingContext) &&
            !(((EOEnterpriseObject)eo).editingContext() instanceof EOSharedEditingContext)) {
            if (((EOEnterpriseObject)eo).editingContext()==null || editingContext()==null) {
                cat.warn("******** Attempted to link to EOs through "+key+" when one of them was not in an editing context: "+this+":"+editingContext()+" and "+eo+ ":" + ((EOEnterpriseObject)eo).editingContext());
                // editingContext().insertObject(((EOEnterpriseObject)eo));
                // throw new RuntimeException("******** Attempted to link to EOs through "+key+" when one of them was not in an editing context: "+this+":"+editingContext()+" and "+eo+ ":" + ((EOEnterpriseObject)eo).editingContext());
            } else {
                throw new RuntimeException("******** Attempted to link to EOs through "+key+" in different editing contexts: "+this+" and "+eo);
            }
        }
        super.addObjectToBothSidesOfRelationshipWithKey(eo,key);
    }

    // -----------------------------------------------------------------------------------------------------
    // A couple of Convenience methods
    public String primaryKey() {
        return ERXExtensions.primaryKeyForObject(this);
    }

    public Object rawPrimaryKeyInTransaction() {
        Object result = rawPrimaryKey();
        if (result == null) {
            NSDictionary pk = primaryKeyDictionary(false);
            if (cat.isDebugEnabled()) cat.debug("pk: " + pk);
            result = ERXExtensions.rawPrimaryKeyFromPrimaryKeyAndEO(pk, this);
        }
        return result;
    }

    public String primaryKeyInTransaction() {
        Object rpk=rawPrimaryKeyInTransaction();
        return rpk!=null ? rpk.toString() : null;
    }
    
    public Object rawPrimaryKey() { return ERXExtensions.rawPrimaryKeyForObject(this); }

    public Object foreignKeyForRelationshipWithKey(String rel) {
        NSDictionary d=EOUtilities.destinationKeyForSourceObject(editingContext(), this, rel);
        return d.count()>0 ? d.allValues().objectAtIndex(0) : null;
    }

    /* useful for debugging */
/*    private Boolean _willChangeCalled = Boolean.FALSE;
    private NSArray _skipKeys = new NSArray(new String[] {"roles"}); //keys that cause stack overflow here
    public void willChange() {
        if (willChangeCat.isDebugEnabled() && _willChangeCalled != null) {
            if (_willChangeCalled.booleanValue()) {
                System.err.println("will change again: " + this);
                super.willChange();
            } else {
                _willChangeCalled = Boolean.TRUE;
                if (getClass().getName().equals("er.eo.LandlordUser")) {
                    NSArray allProps = allPropertyKeys();
                    for (Enumeration e = allProps.objectEnumerator(); e.hasMoreElements();) {
                        String key = (String)e.nextElement();
                        if (!_skipKeys.containsObject(key)) {
                            Object value = valueForKey(key);
                            if (value != null && (value instanceof Boolean))
                                System.err.println("BOOLEAN key: " + key);
                        }
                    }
                }
                super.willChange();
                _willChangeCalled = Boolean.FALSE;
            }
        }
    }*/

    private NSDictionary _primaryKeyDictionary;
    public NSDictionary primaryKeyDictionary(boolean inTransaction) {
        if (!inTransaction && _primaryKeyDictionary == null) {
            if (primaryKey() != null) {
                _primaryKeyDictionary = new NSDictionary(primaryKey(), "id");
            } else
                _primaryKeyDictionary = ERXUtilities.primaryKeyDictionaryForEntity(editingContext(), entityName());
        }
        return _primaryKeyDictionary;
    }

    public Object committedSnapshotValueForKey(String key) {
        return (editingContext().committedSnapshotForObject(this)).objectForKey(key);
    }

    public NSDictionary changesFromCommittedSnapshot() {
        return changesFromSnapshot(editingContext().committedSnapshotForObject(this));
    }

    public boolean parentObjectStoreIsObjectStoreCoordinator() { return editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator; }

    public String toString() {
        String pk = "null pk";
        try {
            primaryKey();
            pk = (pk == null) ? "null pk" : pk;
        } catch(NullPointerException ex) {
        }
        return "<" + getClass().getName() + " "+ pk + ">";
    }/* */
    public String description() { return toString(); }
    public String toLongString() { return super.toString(); }

    private static NSMutableDictionary _attributeKeysPerEntityName=new NSMutableDictionary();
    private static NSArray stringAttributeListForEntityNamed(String entityName) {
        // FIXME: this will need to be synchronized if you go full-MT
        NSArray result=(NSArray)_attributeKeysPerEntityName.objectForKey(entityName);
        if (result==null) {
            EOEntity entity=EOModelGroup.defaultGroup().entityNamed(entityName);
            NSMutableArray attList=new NSMutableArray();
            _attributeKeysPerEntityName.setObjectForKey(attList,entityName);
            result=attList;
            for (Enumeration e=entity.classProperties().objectEnumerator();e.hasMoreElements();) {
                Object property=e.nextElement();
                if (property instanceof EOAttribute) {
                    EOAttribute a=(EOAttribute)property;
                    if (a.className().equals("java.lang.String"))
                        attList.addObject(a.name());
                }
            }
        }
        return result;
    }

    public void trimSpaces() {
        for (Enumeration e=stringAttributeListForEntityNamed(entityName()).objectEnumerator(); e.hasMoreElements();) {
            String key=(String)e.nextElement();
            String value=(String)storedValueForKey(key);
            if (value!=null) {
                String trimmedValue=value.trim();
                if (trimmedValue.length()!=value.length())
                    takeStoredValueForKey(trimmedValue,key);
            }
        }
    }

// Note that after an eo has been deleted its editingContext will be set to null and as such it can be a bit
// difficult to distinguish between a new EO and a deleted EO.
    public boolean isDeletedEO() {
        cat.debug("editingContext() = "+editingContext());
        cat.debug("deleted objects = "+editingContext().deletedObjects());
        cat.debug("this = "+this);
        return editingContext() != null && editingContext().deletedObjects().containsObject(this);
    }

    // sometimes we need to know if this object is new (have not been committed yet).  Note that a deleted object will also have
    // a null editingContext.
    public boolean isNewEO() {
        return ERXExtensions.isNewObject(this);
    }

    public static void didSave(NSNotification n) {
        EOEditingContext ec=(EOEditingContext)n.object();
        // Changed objects
        NSArray updatedObjects=(NSArray)n.userInfo().objectForKey("updated");
        for (Enumeration e = updatedObjects.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
            if (eo instanceof ERXGenericRecord)
                ((ERXGenericRecord)eo).didUpdate();
        }
        // Deleted objects
        NSArray deletedObjects=(NSArray)n.userInfo().objectForKey("deleted");
        for (Enumeration e = deletedObjects.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
            if (eo instanceof ERXGenericRecord)
                ((ERXGenericRecord)eo).didDelete(ec);
        }
        // Inserted objects
        NSArray insertedObjects=(NSArray)n.userInfo().objectForKey("inserted");
        for (Enumeration e = insertedObjects.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
            if (eo instanceof ERXGenericRecord)
                ((ERXGenericRecord)eo).didInsert();
        }
    }

    private static D2WContext _validationContext;
    public static Object ruleValueForAttributeAndKey(EOAttribute a, String key) {
        if (_validationContext==null) {
            _validationContext=new D2WContext();
        }
        _validationContext.setEntity(a.entity());
        _validationContext.setPropertyKey(a.name());
        return _validationContext.valueForKey(key);
    }

    public Object validateValueForKey(Object value, String key) throws NSValidation.ValidationException {
        if (validation.isDebugEnabled())
            validation.debug("ValidateValueForKey on eo: " + this + " value: " + value + " key: " + key);
        if (key==null) // better to raise before calling super which will crash
            throw new RuntimeException("validateValueForKey called with null key on "+this);
        Object result=null;
        try {
            EOEntity myEntity=EOModelGroup.defaultGroup().entityNamed(entityName());
            EOAttribute attr = (EOAttribute)myEntity.attributeNamed(key);
            if (attr!=null && attr.userInfo()!=null && value!=null && value instanceof Number){
                String unit=(String)attr.userInfo().objectForKey("unit");
                if (unit==null) unit="";
                Number inputValue = (Number)value;
                String min=(String)ruleValueForAttributeAndKey(attr,"minValue");
                if (min!=null) {
                    Integer minimum = ERXConstant.integerForString(min);
                    if (inputValue.intValue() < minimum.intValue())
                        throw new NSValidation.ValidationException("<b>" +KEY_MARKER+"</b> should be greater than <b>" +min+ " "+unit+"</b>.");
                }
                String max=(String)ruleValueForAttributeAndKey(attr,"maxValue");
                if(max!=null){
                    Integer maximum = ERXConstant.integerForString(max);
                    if (inputValue.intValue() > maximum.intValue()){
                        throw new NSValidation.ValidationException("<b>" +KEY_MARKER+"</b> should be smaller than <b>" +max+ " "+unit+"</b>.");
                    }
                }
            }
            if (validation.isDebugEnabled())
                validation.debug("Before call to super, classDescription: " + classDescription());
            //result=classDescription().validateValueForKey(value,key);
            result=super.validateValueForKey(value,key);
        } catch (ERXValidationException e) {
            ((ERXValidationException)e).setPropertyKey(key);
            ((ERXValidationException)e).setEoObject(this);
            throw e;
        } catch (NSValidation.ValidationException e) {
            if (e.key() == null || e.object() == null)
                e = new NSValidation.ValidationException(e.getMessage(), this, key);
            validationException.debug("Exception: " + e.getMessage() + " raised while validating object: " + this + " class: " + getClass() + " pKey: " + primaryKey() + "\n" + e);
            throw e;
        } catch (RuntimeException e) {
            NSLog.err.appendln("**** During validateValueForKey "+key);
            NSLog.err.appendln("**** caught "+e);
            throw e;
        }
        return result;
    }

    public void validateForSave( )  throws NSValidation.ValidationException {
        // This condition shouldn't ever happen, but it does ;)
        if (editingContext() != null && editingContext().deletedObjects().containsObject(this)) {
            validation.warn("Calling validate for save on an eo: " + this + " that has been marked for deletion!");
        }
        super.validateForSave();
        if (ERXProperties.booleanForKey("ERDebuggingEnabled"))
            checkConsistency();
    }
    public void checkConsistency() throws NSValidation.ValidationException {}
    // batchCheckConsistency won't be called by validateForSave so you can check
    // things here that you only want checked by a batch process
    public void batchCheckConsistency() throws NSValidation.ValidationException {}
    //public boolean repair_for_1_2(){ return true;   }

    /* Used recursively to clone an object graph of ERXGenericRecord objects. If relationships are owned, they are also cloned also.

        NOTE This method has not yet been fully implemented. At least one issue remains: what to do when not cloning and the entity of the source is different from that of the destination. There will also be problems when not all objects are of class ERXGenericRecord
        */
    public static Boolean DELETE_ORIGINAL = Boolean.TRUE;
    public ERXGenericRecord cloneObjectGraph(EOEditingContext ec, NSDictionary cloneDictionary, Boolean deleteOriginal){
        return moveObjectGraphTo(null, ec, cloneDictionary, deleteOriginal);
    }
    public ERXGenericRecord moveObjectGraphTo(ERXGenericRecord newObject, EOEditingContext ec, NSDictionary cloneDictionary, Boolean deleteOriginal) {
        if (deleteOriginal == null) deleteOriginal = Boolean.FALSE;
        ERXGenericRecord result;
        NSMutableDictionary equivalence = new NSMutableDictionary();
        NSMutableSet deferredRelationships = new NSMutableSet();
        NSMutableSet deferredDeletions = new NSMutableSet();
        _initEquivalence(equivalence, cloneDictionary);
        cloneCat.debug("Equivalence: " + equivalence);
        result = _cloneObjectGraph(ec, cloneDictionary,
                                        equivalence,
                                        deferredRelationships, deleteOriginal, deferredDeletions, newObject);
        // process deferred relationships
        cloneCat.debug("Deferred Relationships: " + deferredRelationships);
        for (Enumeration e = deferredRelationships.objectEnumerator();
             e.hasMoreElements(); ) {
            NSDictionary dict = (NSDictionary)e.nextElement();
            ERXGenericRecord source = (ERXGenericRecord)dict.objectForKey("src");
            ERXGenericRecord dest = (ERXGenericRecord)dict.objectForKey("dest");
            if (dest == null) {
                ERXGenericRecord oldDest = (ERXGenericRecord)dict.objectForKey("oldDest");
                //System.err.println("olddest pkey: " + oldDest.primaryKeyInTransaction());
                dest = (ERXGenericRecord)equivalence.objectForKey(oldDest.primaryKeyInTransaction());
            }
            String key = (String)dict.objectForKey("key");
            cloneCat.debug("src: " + source + " dest: " + dest + " key: " + key);
            source.addObjectToBothSidesOfRelationshipWithKey(dest, key);
        }
        // delete the objects set for deletion
        if (deleteOriginal.booleanValue()) {
            cloneCat.debug("Deferred Deletions: " + deferredDeletions);
            for (Enumeration e = deferredDeletions.objectEnumerator(); e.hasMoreElements(); ) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                ec.deleteObject(eo);
            }
        }
        return result;
    }

    private void _initEquivalence(NSMutableDictionary equivalence, NSDictionary cloneDictionary) {
        String action = (String)cloneDictionary.objectForKey("action");
        if (action != null && action.endsWith("clone")) {
            equivalence.setObjectForKey(NSKeyValueCoding.NullValue, primaryKeyInTransaction());
            NSDictionary eos = (NSDictionary)cloneDictionary.objectForKey("eos");
            if (eos != null)
                for (Enumeration e = eos.allKeys().objectEnumerator(); e.hasMoreElements(); ) {
                    String relationship = (String)e.nextElement();
                    NSDictionary eo = (NSDictionary)eos.valueForKey(relationship);
                    Object value = valueForKey(relationship);
                    if (value instanceof NSArray) {
                        for (Enumeration ve = ((NSArray)value).objectEnumerator(); ve.hasMoreElements(); ) {
                            ERXGenericRecord gr = (ERXGenericRecord)ve.nextElement();
                            gr._initEquivalence(equivalence, eo);
                        }
                    } else {
                        ((ERXGenericRecord)value)._initEquivalence(equivalence, eo);
                    }
                };
        }
    }

    private ERXGenericRecord _cloneObjectGraph(EOEditingContext ec, NSDictionary cloneDictionary, NSMutableDictionary equivalence, NSMutableSet deferredRelationships, Boolean deleteOriginal, NSMutableSet deferredDeletions, ERXGenericRecord newObject) {
        String entityName = (String)cloneDictionary.objectForKey("entityName");
        String action = (String)cloneDictionary.objectForKey("action");
        cloneCat.debug("processing entityNamed: " + entityName);
        ERXGenericRecord result;
        if (action != null && action.endsWith("clone")) {
            cloneCat.debug("action: " + action);
            result = action.equals("clone") ? (ERXGenericRecord)ERXUtilities.createEO(entityName, ec) : newObject;
            NSDictionary eos = (NSDictionary)cloneDictionary.objectForKey("eos");
            NSArray invariantKeys = (NSArray)cloneDictionary.objectForKey("invariantKeys");
            if (invariantKeys == null) invariantKeys = new NSArray();
            cloneCat.debug("Invariant keys: " + invariantKeys);
            equivalence.setObjectForKey(result, primaryKeyInTransaction());
            // Transfer the attributes
            cloneCat.debug("Transferring Attributes:");
            for (Enumeration e = attributeKeys().objectEnumerator();
                 e.hasMoreElements();) {
                String relationship = (String)e.nextElement();
                cloneCat.debug("Processing key: " + relationship);
                if (valueForKey(relationship) != null) {
                    if (!invariantKeys.containsObject(relationship)) result.takeValueForKey(valueForKey(relationship), relationship);
                    cloneCat.debug("key contained in invariantKeys: " + invariantKeys.containsObject(relationship));
                    cloneCat.debug(relationship + " = " + valueForKey(relationship));
                }
            }
            // Transfer toOnes
            cloneCat.debug("Transferring To-One Relationships:");
            for (Enumeration e = toOneRelationshipKeys().objectEnumerator();
                 e.hasMoreElements();) {
                String relationship = (String)e.nextElement();
                cloneCat.debug("Processing relationship: " + relationship);
                EOEnterpriseObject value = (EOEnterpriseObject)valueForKey(relationship);
                NSDictionary cDict = eos == null ? null : (NSDictionary)eos.objectForKey(relationship);
                String nextAction = cDict == null ? "defaultAction" : (String)cDict.objectForKey("action");
                ERXGenericRecord newValue = nextAction.equals("autoclone") ? (ERXGenericRecord)result.valueForKey(relationship) : null;
                cloneCat.debug("key contained in invariantKeys: " + invariantKeys.containsObject(relationship));
                _transferValue(result, value, newValue, -1, cDict, deferredRelationships, equivalence, relationship, ec, deleteOriginal, deferredDeletions, invariantKeys);
            }
            // Transfer toManys
            cloneCat.debug("Transferring To-Many Relationships:" + toManyRelationshipKeys());
            for (Enumeration e = toManyRelationshipKeys().objectEnumerator();
                 e.hasMoreElements();) {
                String relationship = (String)e.nextElement();
                cloneCat.debug("Processing relationship: " + relationship);
                cloneCat.debug("key contained in invariantKeys: " + invariantKeys.containsObject(relationship));
                NSDictionary cDict = eos == null ? null : (NSDictionary)eos.objectForKey(relationship);
                NSArray values = (NSArray)valueForKey(relationship);
                String nextAction = cDict == null ? "defaultAction" : (String)cDict.objectForKey("action");
                NSArray newValues;
                if(nextAction.equals("autoclone")) {
                    newValues = (NSArray)result.valueForKey(relationship);
                    if (values.count() != newValues.count())
                        throw new RuntimeException("The new value count, " + newValues.count() + ", does not match the value count: " + values.count());
                } else 
                    newValues = null;
                if (relationship.equals("receivedNotifications"))
                    System.err.println("count: " + values.count());
                int index = 0;
                for (Enumeration ev = values.objectEnumerator(); ev.hasMoreElements(); index++) {
                    EOEnterpriseObject value = (EOEnterpriseObject)ev.nextElement();
                    ERXGenericRecord newValue = newValues == null ? null : (ERXGenericRecord)newValues.objectAtIndex(index);
                    _transferValue(result, value, newValue, index, cDict, deferredRelationships, equivalence, relationship, ec, deleteOriginal, deferredDeletions, invariantKeys);
                }
            }
            deferredDeletions.addObject(this);
        } else {
            result = null;
            throw new RuntimeException("This case is not supported yet. Object graph with broken cloning chains.");
        }
        cloneCat.debug("Exiting clone");
        return result;
    }

    private void _transferValue(ERXGenericRecord result, EOEnterpriseObject value, ERXGenericRecord newObject, int index, NSDictionary cDict, NSMutableSet deferredRelationships, NSMutableDictionary equivalence, String relationship, EOEditingContext ec, Boolean deleteOriginal, NSMutableSet deferredDeletions, NSArray invariantKeys) {
        if (value != null && deleteOriginal.booleanValue()) {
            cloneCat.debug("REMOVE "+ relationship +" - "+value+ " - "+ this);
            removeObjectFromBothSidesOfRelationshipWithKey(value, relationship);

        }
        String action = cDict == null ? "defaultAction" : (String)cDict.objectForKey("action");
        if(action.endsWith("clone"))             {
            cloneCat.debug(action + ": " + cDict.valueForKey("entityName"));
            value =
                ((ERXGenericRecord)value)._cloneObjectGraph(ec, cDict, equivalence, deferredRelationships, deleteOriginal, deferredDeletions, newObject);
            if (!invariantKeys.containsObject(relationship)) result.addObjectToBothSidesOfRelationshipWithKey(value, relationship);
            cloneCat.debug(relationship + " = " + value.getClass().getName() +
                           " pk: " + value.valueForKey("primaryKey"));
        } else if (value != null) {
            if (value instanceof ERXGenericRecord &&
                equivalence.objectForKey(((ERXGenericRecord)value).primaryKeyInTransaction()) != null) {
                cloneCat.debug("in equivalence");
                if (!invariantKeys.containsObject(relationship)) {
                    NSDictionary rel =
                    new NSDictionary(new Object[] {result, value, relationship },
                                     new Object[] {"src", "oldDest", "relationship"});
                    deferredRelationships.addObject(rel);
                }
            } else {
                if (!invariantKeys.containsObject(relationship)) {
                    value = EOUtilities.localInstanceOfObject(ec, value);
                    result.addObjectToBothSidesOfRelationshipWithKey(value, relationship);
                } else {
                    cloneCat.debug("not moving relationship: " + relationship);
                }
                if (cloneCat.isDebugEnabled()) {
                    cloneCat.debug("Set relationship: " + relationship + ", dest: " + value + ", source: " + result);
                    String inverseKey = result.inverseForRelationshipKey(relationship);
                    cloneCat.debug("inverse key: " + inverseKey);
                    if (inverseKey != null)
                        cloneCat.debug("source: " + result + ", Inverse source: " + value.valueForKey(inverseKey));
                }
            }
            cloneCat.debug(relationship + " = " + value.getClass().getName() +
                           " pk: " +
                           (value instanceof ERXGenericRecord ? ((ERXGenericRecord)value).primaryKey() :
                           "none"));
        }
    }
    
}
