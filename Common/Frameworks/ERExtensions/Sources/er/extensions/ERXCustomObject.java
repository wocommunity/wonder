/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOCustomObject;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EORelationshipManipulation;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;

/**
 * This class contains a bunch of extensions to the
 * regular {@link com.webobjects.eocontrol.EOCustomObject} class. Of notable
 * interest it contains built in support for generating
 * primary keys via the {@link ERXGeneratesPrimaryKeyInterface},
 * support for an augmented transaction methods like <code>
 * willUpdate</code> and <code>didDelete</code> and a bunch
 * of handy utility methods like <code>committedSnapshotValueForKey
 * </code>. At the moment it is required that those wishing to take
 * advantage of templatized and localized validation exceptions
 * need to subclass this class. Hopefully in the future we can
 * get rid of this requirement.
 * Also, this class supports auto-updating of inverse relationships. You can
 * simply call <code>eo.setFoo(other), eo.takeValueForKey(other),
 * eo.addObjectToBothSidesOfRelationshipWithKey(other, "foo")</code> or <code>eo.addToFoos(other)</code> 
 * and the inverse relationship will get
 * updated for you automagically, so that you don't need to call
 * <code>other.addToBars(eo)</code> or <code>other.setBar(eo)</code>. Doing so doesn't hurt, though.
 * Giving a <code>null</code> value of removing the object from a to-many will result in the inverse 
 * relationship getting cleared. <br />
 * This feature should greatly help readability and reduce the number errors you make when you
 * forget to update an inverse relationship. To turn this feature on, you must set the system default 
 * <code>er.extensions.ERXEnterpriseObject.updateInverseRelationships=true</code>.
 */

public class ERXCustomObject extends EOCustomObject implements ERXGuardedObjectInterface, ERXGeneratesPrimaryKeyInterface, ERXEnterpriseObject {

    /** holds all subclass related Logger's */
    private static NSMutableDictionary classLogs = new NSMutableDictionary();
     
    public static boolean shouldTrimSpaces(){
        return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXCustomObject.shouldTrimSpaces", false);
    }
    
   /**
     * Clazz object implementation for ERXCustomObject. See
     * {@link EOEnterpriseObjectClazz} for more information on this
     * neat design pattern.
     */
    public static class ERXCustomObjectClazz<T extends EOEnterpriseObject> extends EOEnterpriseObjectClazz<T> {
        
    }

    public String insertionStackTrace = null;
    
    protected boolean wasInitialized;
    
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#getClassLog()
     */
    public Logger getClassLog() {
        Logger classLog = (Logger)classLogs.objectForKey(this.getClass());
        if ( classLog == null) {
            synchronized(classLogs) {
                classLog = Logger.getLogger(this.getClass());
                classLogs.setObjectForKey(classLog, this.getClass());
            }
        }
        return classLog;
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#self()
     */
    public ERXEnterpriseObject self(){
        return this;
    }

   /**
    * Implementation of {@link ERXGuardedObjectInterface}. This is checked
    * before the object is deleted in the <code>willDelete</code> method
    * which is in turn called by {@link ERXEditingContextDelegate}. The default
    * implementation returns <code>true</code>.
    * @return true
    */
    public boolean canDelete() { return true; }
    /**
     * Implementation of {@link ERXGuardedObjectInterface}. This is checked
     * before the object is deleted in the <code>willUpdate</code> method
     * which is in turn called by {@link ERXEditingContextDelegate}. The default
     * implementation returns <code>true</code>.
     * @return true
     */
    public boolean canUpdate() { return true; }
    
    /**
        * Implementation of {@link ERXGuardedObjectInterface}.
     * This is used to work around a bug in EOF that doesn't refresh the relationship in the parent
     * editingContext for the object.
     */
    public void delete() {
        editingContext().deleteObject(this);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#mightDelete()
     */
    public void mightDelete() {
        if (tranLogMightDelete.isDebugEnabled())
        	tranLogMightDelete.debug("Object:" + description());
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#willDelete()
     */
    public void willDelete() throws NSValidation.ValidationException {
        if (canDelete() == false) {
            throw ERXValidationFactory.defaultFactory().createException(this, null, null, "ObjectCannotBeDeletedException");            
        }
        if (tranLogWillDelete.isDebugEnabled())
            tranLogWillDelete.debug("Object:" + description());
    }
    
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#willInsert()
     */
    public void willInsert() {
        /* Disabling this check by default -- it's causing problems for objects created and deleted
        in the same transaction */
         if (tranLogWillInsert.isDebugEnabled()) {
             /* check that all the to manies have an array */
             for (Enumeration e=toManyRelationshipKeys().objectEnumerator(); e.hasMoreElements();) {
                 String key=(String)e.nextElement();
                 Object o=storedValueForKey(key);
                 if (o==null || !EOFaultHandler.isFault(o) && o instanceof NSKeyValueCoding.Null) {
                     tranLogWillInsert.error("Found illegal value in to many "+key+" for "+this+": "+o);
                 }
             }
             tranLogWillInsert.debug("Object:" + description());
         }
         if(shouldTrimSpaces())
             trimSpaces();
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#willUpdate()
     */
    public void willUpdate() {
        /* Disabling this check by default -- it's causing problems for objects created and deleted
        in the same transaction */
         if (tranLogWillUpdate.isDebugEnabled()) {
             /* check that all the to manies have an array */
             for (Enumeration e=toManyRelationshipKeys().objectEnumerator(); e.hasMoreElements();) {
                 String key=(String)e.nextElement();
                 Object o=storedValueForKey(key);
                 if (o==null || !EOFaultHandler.isFault(o) && o instanceof NSKeyValueCoding.Null) {
                     tranLogWillUpdate.error("Found illegal value in to many "+key+" for "+this+": "+o);
                 }
             }
             if (tranLogWillUpdate.isDebugEnabled())
                 tranLogWillUpdate.debug("Object:" + description() + " changes: " + changesFromCommittedSnapshot());
         }
         if(shouldTrimSpaces())
             trimSpaces();
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#flushCaches()
     */
    public void flushCaches() {}

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#didDelete(com.webobjects.eocontrol.EOEditingContext)
     */
    public void didDelete(EOEditingContext ec) {
        if (tranLogDidDelete.isDebugEnabled())
            tranLogDidDelete.debug("Object:" + description());
    }
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#didUpdate()
     */
    public void didUpdate() {
        if (tranLogDidUpdate.isDebugEnabled())
            tranLogDidUpdate.debug("Object:" + description());
    }
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#didInsert()
     */
    public void didInsert() {
        if (tranLogDidInsert.isDebugEnabled())
            tranLogDidInsert.debug("Object:" + description());

        //We're going to blow the primaryKey cache:
        _primaryKey = null;
}

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#willRevert()
     */
    public void willRevert() {
        if ( tranLogWillRevert.isDebugEnabled() )
            tranLogWillRevert.debug("Object: " + description());
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#didRevert(com.webobjects.eocontrol.EOEditingContext)
     */
    public void didRevert(EOEditingContext ec) {
        if ( tranLogDidRevert.isDebugEnabled() )
            tranLogDidRevert.debug("Object: " + description());
        flushCaches();
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#addObjectsToBothSidesOfRelationshipWithKey(com.webobjects.foundation.NSArray, java.lang.String)
     */
    public void addObjectsToBothSidesOfRelationshipWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            NSArray objectsSafe = objects instanceof NSMutableArray ? (NSArray)objects.immutableClone() : objects;
            for (Enumeration e = objectsSafe.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                addObjectToBothSidesOfRelationshipWithKey(eo, key);
            }
        }
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#removeObjectsFromBothSidesOfRelationshipWithKey(com.webobjects.foundation.NSArray, java.lang.String)
     */
    public void removeObjectsFromBothSidesOfRelationshipWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            NSArray objectsSafe = objects instanceof NSMutableArray ? (NSArray)objects.immutableClone() : objects;
            for (Enumeration e = objectsSafe.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                removeObjectFromBothSidesOfRelationshipWithKey(eo, key);
             }
        }
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#removeObjectsFromPropertyWithKey(com.webobjects.foundation.NSArray, java.lang.String)
     */
    public void removeObjectsFromPropertyWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            NSArray objectsSafe = objects instanceof NSMutableArray ? (NSArray)objects.immutableClone() : objects;
            for (Enumeration e = objectsSafe.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                removeObjectFromPropertyWithKey(eo, key);
            }
        }
    }
    
    /**
     * By default, and this should change in the future, all editing contexts that
     * are created and use ERXEnterpriseObjects or subclasses need to have a delegate
     * set of instance {@link ERXEditingContextDelegate}. These delegates provide
     * the augmentation to the regular transaction mechanism, all of the will* methods
     * plus the flushCaching method. To change the default behaviour set the property:
     * <b>er.extensions.ERXRaiseOnMissingEditingContextDelegate</b> to false in your
     * WebObjects.properties file. This method is called when an object is fetched,
     * updated or inserted.
     * @param editingContext to check for the correct delegate.
     * @return if the editing context has the correct delegate set.
     */
    private boolean _checkEditingContextDelegate(EOEditingContext editingContext) {
        return ERXEditingContextDelegate._checkEditingContextDelegate(editingContext);
    }

   /**
     * Checks the editing context delegate before calling
     * super's implementation. See the method <code>
     * _checkEditingContextDelegate</code> for an explanation
     * as to what this check does.
     * @param editingContext to be checked to make sure it has the
     *      correct type of delegate set.
     */
    public void awakeFromClientUpdate(EOEditingContext editingContext) {
        _checkEditingContextDelegate(editingContext);
        super.awakeFromClientUpdate(editingContext);
        wasInitialized = true;
    }
    /**
     * Checks the editing context delegate before calling
     * super's implementation. See the method <code>
     * _checkEditingContextDelegate</code> for an explanation
     * as to what this check does.
     * @param editingContext to be checked to make sure it has the
     *      correct type of delegate set.
     */
    public void awakeFromInsertion(EOEditingContext editingContext) {
        _checkEditingContextDelegate(editingContext);
        if (insertionTrackingLog.isDebugEnabled()) {
            insertionStackTrace = ERXUtilities.stackTrace();
            insertionTrackingLog.debug("inserted "+getClass().getName()+" at "+insertionStackTrace);
        }            
        super.awakeFromInsertion(editingContext);
        EOGlobalID gid = editingContext.globalIDForObject(this);
        if (gid.isTemporary()) {
            init(editingContext);
        }
        wasInitialized = true;
    }

    /**
     * used for initialization stuff instead of awakeFromInsertion.
     * <code>awakeFromInsertions</code> is buggy because if an EO is
     * deleted and then its EOEditingContext is reverted using 'revert' 
     * for example then EOF will -insert- this EO again in its EOEditingContext
     * which in turn calls awakeFromInsertion again.
     * 
     * @param ec the EOEditingContext in which this new EO is inserted
     */
    protected void init(EOEditingContext ec) {
        
    }
    
    /**
     * Checks the editing context delegate before calling
     * super's implementation. See the method <code>
     * _checkEditingContextDelegate</code> for an explanation
     * as to what this check does.
     * @param editingContext to be checked to make sure it has the
     *      correct type of delegate set.
     */
    public void awakeFromFetch(EOEditingContext editingContext) {
        _checkEditingContextDelegate(editingContext);
        super.awakeFromFetch(editingContext);
        wasInitialized = true;
    }
    /**
     * Adds a check to make sure that both the object being added and
     * this object are in the same editing context. If not then a runtime
     * exception is thrown instead of getting the somewhat cryptic NSInternalInconsistency
     * excpetion that is thrown when you attempt to save changes to the database.
     * @param eo enterprise object to be added to the relationship
     * @param key relationship to add the object to.
     */
    public void addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation eo, String key) {
        if (eo!=null &&
            ((EOEnterpriseObject)eo).editingContext()!=editingContext() &&
            !(editingContext() instanceof EOSharedEditingContext) &&
            !(((EOEnterpriseObject)eo).editingContext() instanceof EOSharedEditingContext)) {
            if (((EOEnterpriseObject)eo).editingContext()==null || editingContext()==null) {
                if(editingContext()==null)
                    throw new RuntimeException("******** Attempted to link to EOs through "
                                               +key+" when one of them was not in an editing context: "
                                               +this+":"+editingContext()+" and "+eo+ ":" + ((EOEnterpriseObject)eo).editingContext());
            } else {
                throw new RuntimeException("******** Attempted to link to EOs through "+key+" in different editing contexts: "+this+" and "+eo);
            }
        }
        super.addObjectToBothSidesOfRelationshipWithKey(eo,key);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#primaryKey()
     */
    protected String _primaryKey = null;
    public String primaryKey() {
        if(_primaryKey == null) {
        _primaryKey = ERXEOControlUtilities.primaryKeyStringForObject(this);
        }
        return _primaryKey;
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#rawPrimaryKeyInTransaction()
     */
    public Object rawPrimaryKeyInTransaction() {
        Object result = rawPrimaryKey();
        if (result == null) {
            NSDictionary pk = primaryKeyDictionary(false);
            NSArray primaryKeyAttributeNames=primaryKeyAttributeNames();
            result = ERXArrayUtilities.valuesForKeyPaths(pk, primaryKeyAttributeNames);
            if(((NSArray)result).count() == 1) result = ((NSArray)result).lastObject();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#primaryKeyInTransaction()
     */
    public String primaryKeyInTransaction() {
        return ERXEOControlUtilities._stringForPrimaryKey(rawPrimaryKeyInTransaction());
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#rawPrimaryKey()
     */
    public Object rawPrimaryKey() {
        return ERXEOControlUtilities.primaryKeyObjectForObject(this);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#encryptedPrimaryKey()
     */
    public String encryptedPrimaryKey() {
        String pk = ERXEOControlUtilities.primaryKeyStringForObject(this);
        return pk==null ? null : ERXCrypto.blowfishEncode(pk);
    }
        
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#foreignKeyForRelationshipWithKey(java.lang.String)
     */
    public Object foreignKeyForRelationshipWithKey(String rel) {
        NSDictionary d=EOUtilities.destinationKeyForSourceObject(editingContext(), this, rel);
        return d != null && d.count()>0 ? d.allValues().objectAtIndex(0) : null;
    }


    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#primaryKeyAttributeNames()
     */
    public NSArray primaryKeyAttributeNames() {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(editingContext(), entityName());
        return entity.primaryKeyAttributeNames();
    }

    
    /** caches the primary key dictionary for the given object */
    private NSDictionary _primaryKeyDictionary;

    /**
     * Implementation of the interface {@link ERXGeneratesPrimaryKeyInterface}.
     * This implementation operates in the following fashion. If it is called
     * passing in 'false' and it has not yet been saved to the database, meaning
     * this object does not yet have a primary key assigned, then it will have the
     * adaptor channel generate a primary key for it. Then when the object is saved
     * to the database it will use the previously generated primary key instead of
     * having the adaptor channel generate another primary key. If 'true' is passed in
     * then this method will either return the previously generated primaryKey
     * dictionary or null if it does not have one. Typically you should only call
     * this method with the 'false' parameter seeing as unless you are doing something
     * really funky you won't be dealing with this object when it is in the middle of
     * a transaction. The delegate {@link ERXDatabaseContextDelegate} is the only class
     * that should be calling this method and passing in 'true'.
     * @param inTransaction boolean flag to tell the object if it is currently in the
     *      middle of a transaction.
     * @return primary key dictionary for the current object, if the object does not have
     *      a primary key assigned yet and is not in the middle of a transaction then
     *      a new primary key dictionary is created, cached and returned.
     */
    // FIXME: this method is really misnamed; it should be called rawPrimaryKeyDictionary
    public NSDictionary primaryKeyDictionary(boolean inTransaction) {
        if(_primaryKeyDictionary == null) {
            if (!inTransaction) {
                Object rawPK = rawPrimaryKey();
                if (rawPK != null) {
                    if (log.isDebugEnabled()) log.debug("Got raw key: "+ rawPK);
                    NSArray primaryKeyAttributeNames=primaryKeyAttributeNames();
                    _primaryKeyDictionary = new NSDictionary(rawPK instanceof NSArray ? (NSArray)rawPK : new NSArray(rawPK), primaryKeyAttributeNames);
                } else {
                    if (log.isDebugEnabled()) log.debug("No raw key, trying single key");
                    _primaryKeyDictionary = ERXEOControlUtilities.newPrimaryKeyDictionaryForObject(this);
                }
            }
        }
        return _primaryKeyDictionary;
    }
    
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#committedSnapshotValueForKey(java.lang.String)
     */
    public Object committedSnapshotValueForKey(String key) {
        NSDictionary snapshot = editingContext().committedSnapshotForObject(this);
        return snapshot != null ? snapshot.objectForKey(key) : null;
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#localInstanceOf(com.webobjects.eocontrol.EOEnterpriseObject)
     */
    public EOEnterpriseObject localInstanceOf(EOEnterpriseObject eo) {
        return ERXEOControlUtilities.localInstanceOfObject(editingContext(), eo);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#localInstanceIn(com.webobjects.eocontrol.EOEnterpriseObject)
     */
    public EOEnterpriseObject localInstanceIn(EOEditingContext ec) {
        return ERXEOControlUtilities.localInstanceOfObject(ec, this);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#localInstancesOf(com.webobjects.foundation.NSArray)
     */
    public NSArray localInstancesOf(NSArray eos) {
        return ERXEOControlUtilities.localInstancesOfObjects(editingContext(), eos);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#changesFromCommittedSnapshot()
     */
    public NSDictionary changesFromCommittedSnapshot() {
        return changesFromSnapshot(editingContext().committedSnapshotForObject(this));
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#parentObjectStoreIsObjectStoreCoordinator()
     */
    public boolean parentObjectStoreIsObjectStoreCoordinator() {
        return editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator;
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#refetchObjectFromDBinEditingContext(com.webobjects.eocontrol.EOEditingContext)
     */
    public ERXEnterpriseObject refetchObjectFromDBinEditingContext(EOEditingContext ec){
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName());
        EOQualifier qual = entity.qualifierForPrimaryKey(primaryKeyDictionary(false));
        EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName(), qual, null);
        fetchSpec.setRefreshesRefetchedObjects(true);
        NSArray results = ec.objectsWithFetchSpecification(fetchSpec);
        ERXEnterpriseObject freshObject = null;
        if(results.count()>0){
            freshObject = (ERXEnterpriseObject)results.objectAtIndex(0);
        }
        return freshObject;
    }
    
    /**
     * Overrides the EOGenericRecord's implementation to
     * provide a slightly less verbose output. A typical
     * output for an object mapped to the class com.foo.User
     * with a primary key of 50 would look like:
     * <com.foo.User pk:"50">
     * EOGenericRecord's implementation is preserved in the
     * method <code>toLongString</code>. To restore the original
     * verbose logging in your subclasses override this method and
     * return toLongString.
     * @return much less verbose description of an enterprise
     *      object.
     */
    public String toString() {
        String pk = primaryKey();
        pk = (pk == null) ? "null" : pk;
        return "<" + getClass().getName() + " pk:\""+ pk + "\">";
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#description()
     */
    public String description() { return toString(); }
    
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#toLongString()
     */
    public String toLongString() { return super.toString(); }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#trimSpaces()
     */
    public void trimSpaces() {
        ERXEOControlUtilities.trimSpaces(this);
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#isDeletedEO()
     */
    public boolean isDeletedEO() {
        if (log.isDebugEnabled()) {
            log.debug("editingContext() = " + editingContext() + " this object: " + this);
        }
        // HACK AK: using private API here
        EOGlobalID gid = __globalID();
        boolean isDeleted = (editingContext() == null && (gid != null && !gid.isTemporary()));
        return isDeleted || (editingContext() != null && editingContext().deletedObjects().containsObject(this));
    }

    /**
        * @deprecated use {@link ERXGenericRecord#isNewObject() ERXGenericRecord#isNewObject}
     */
    public boolean isNewEO() {
        return isNewObject();
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#isNewObject()
     */

    public boolean isNewObject() {
        return ERXEOControlUtilities.isNewObject(this);
    }

    /**
     * Overrides the default validation mechanisms to provide
     * a few checks before invoking super's implementation,
     * which incidently just invokes validateValueForKey on the
     * object's class description. The class description for this
     * object should be an {@link ERXEntityClassDescription} or subclass.
     * It is that class that provides the hooks to convert model
     * throw validation exceptions into {@link ERXValidationException}
     * objects.
     * @param value to be validated for a given attribute or relationship
     * @param key corresponding to an attribute or relationship
     * @throws NSValidation.ValidationException if the value fails validation
     * @return the validated value
     */
    public Object validateValueForKey(Object value, String key) throws NSValidation.ValidationException {
        if (validation.isDebugEnabled())
            validation.debug("ValidateValueForKey on eo: " + this + " value: " + value + " key: " + key);
        if (key==null) // better to raise before calling super which will crash
            throw new RuntimeException("validateValueForKey called with null key on "+this);
        Object result=null;
        try {
            result=super.validateValueForKey(value,key);
            EOClassDescription cd = classDescription();
            if(cd instanceof ERXEntityClassDescription) {
                ((ERXEntityClassDescription)cd).validateObjectWithUserInfo(this, value, "validateForKey." + key, key);
            }
        } catch (ERXValidationException e) {
            throw e;
        } catch (NSValidation.ValidationException e) {
            if (e.key() == null || e.object() == null || (e.object() != null && !(e.object() instanceof EOEnterpriseObject)))
                e = new NSValidation.ValidationException(e.getMessage(), this, key);
            if(validationException.isDebugEnabled()) {
                validationException.debug("Exception: " + e.getMessage() + " raised while validating object: "
                                      + this + " class: " + getClass() + " pKey: " + primaryKey(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            log.error("**** During validateValueForKey "+key, e);
            throw e;
        }
        return result;
    }
    
    /**
     * This method performs a few checks before invoking
     * super's implementation. If the property key:
     * <b>ERDebuggingEnabled</b> is set to true then the method
     * <code>checkConsistency</code> will be called on this object.
     * @throws NSValidation.ValidationException if the object does not
     *      pass validation for saving to the database.
     */
    public void validateForSave( )  throws NSValidation.ValidationException {
        // This condition shouldn't ever happen, but it does ;)
        // CHECKME: This was a 4.5 issue, not sure if this one has been fixed yet.
        if (editingContext() != null && editingContext().deletedObjects().containsObject(this)) {
            validation.warn("Calling validate for save on an eo: " + this + " that has been marked for deletion!");
        }
        super.validateForSave();
        // FIXME: Should move all of the keys into on central place for easier management.
        //    Also might want to have a flag off of ERXApplication is debugging is enabled.
        // FIXME: Should have a better flag than just ERDebuggingEnabled
        if (ERXProperties.booleanForKey("ERDebuggingEnabled"))
            checkConsistency();
    }

    /**
     *  Calls up validateForInsert() on the class description if it supports it.
     * @throws NSValidation.ValidationException if the object does not
     *      pass validation for saving to the database.
     */
    public void validateForInsert() throws NSValidation.ValidationException {
        EOClassDescription cd = classDescription();
        if(cd instanceof ERXEntityClassDescription) {
            ((ERXEntityClassDescription)cd).validateObjectForInsert(this);
        }
        super.validateForInsert();
    }

    /**
     * Calls up validateForUpdate() on the class description if it supports it.
     * @throws NSValidation.ValidationException if the object does not
     *      pass validation for saving to the database.
     */
    public void validateForUpdate() throws NSValidation.ValidationException {
        EOClassDescription cd = classDescription();
        if(cd instanceof ERXEntityClassDescription) {
            ((ERXEntityClassDescription)cd).validateObjectForUpdate(this);
        }
        super.validateForUpdate();
    }

    /**
     * Calls up validateForUpdate() on the class description if it supports it.
     * @throws NSValidation.ValidationException if the object does not
     *      pass validation for saving to the database.
     */
    public void validateForDelete() throws NSValidation.ValidationException {
        EOClassDescription cd = classDescription();
        if(cd instanceof ERXEntityClassDescription) {
            ((ERXEntityClassDescription)cd).validateObjectForDelete(this);
        }
        super.validateForDelete();
    }

    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#checkConsistency()
     */
    // CHECKME: This method was very useful at NS, might not be as useful here.
    public void checkConsistency() throws NSValidation.ValidationException {}
    
    /* (non-Javadoc)
     * @see er.extensions.ERXEnterpriseObject#batchCheckConsistency()
     */
    public void batchCheckConsistency() throws NSValidation.ValidationException {}
    
    /**
     * Overridden to support two-way relationship setting.
     */
    protected void includeObjectIntoPropertyWithKey(Object o, String key) {
    	super.includeObjectIntoPropertyWithKey(o, key);
    	if(ERXEnterpriseObject.updateInverseRelationships && o != null) {
    		String inverse = classDescription().inverseForRelationshipKey(key);
    		if(inverse != null) {
    			EOEnterpriseObject eo = (EOEnterpriseObject) o;
				if(!eo.isToManyKey(inverse)) {
					EOEnterpriseObject value = (EOEnterpriseObject)eo.valueForKey(inverse);
					if(value != this) {
						eo.takeValueForKey(this, inverse);
					}
				} else {
					NSArray values = (NSArray)eo.valueForKey(inverse);
					if(!values.containsObject(this)) {
						eo.addObjectToPropertyWithKey(this, inverse);
					}
				}
    		}
    	}
    }
    
    
    /**
     * Overridden to support two-way relationship setting.
     */
    protected void excludeObjectFromPropertyWithKey(Object o, String key) {
       	super.excludeObjectFromPropertyWithKey(o, key);
    	if(ERXEnterpriseObject.updateInverseRelationships && o != null) {
    		String inverse = classDescription().inverseForRelationshipKey(key);
    		if(inverse != null) {
				EOEnterpriseObject eo = (EOEnterpriseObject) o;
    			if(!eo.isToManyKey(inverse)) {
					if(eo.valueForKey(inverse) != null) {
						eo.takeValueForKey(null, inverse);
					}
				} else {
					NSArray values = (NSArray)eo.valueForKey(inverse);
					if(values.containsObject(this)) {
						eo.removeObjectFromPropertyWithKey(this, inverse);
					}
				}
    		}
    	}
    }
    
    /**
     * Overridden to support two-way relationship setting.
     */
    public void takeStoredValueForKey(Object object, String key) {
    	// we only handle toOne keys here, but there is no API for that so
    	// this unreadable monster first checks the fastest thing, the the slower conditions
    	if(ERXEnterpriseObject.updateInverseRelationships && wasInitialized && (object instanceof EOEnterpriseObject || 
    			((object == null) && !isToManyKey(key) 
    					&& classDescriptionForDestinationKey(key) != null))) {
    		String inverse = classDescription().inverseForRelationshipKey(key);
    		if(inverse != null) {
    			if(object != null) {
    				EOEnterpriseObject eo = (EOEnterpriseObject)object;
    				super.takeStoredValueForKey(object, key);
    				if(eo.isToManyKey(inverse)) {
    					NSArray values = (NSArray)eo.valueForKey(inverse);
    					if(!values.containsObject(this)) {
    						eo.addObjectToPropertyWithKey(this, inverse);
    					}
    				} else {
    					EOEnterpriseObject old = (EOEnterpriseObject) eo.valueForKey(inverse);
    					if(old != this) {
    						eo.takeValueForKey(this, inverse);
    					}
    				}
    			} else {
    				EOEnterpriseObject old = (EOEnterpriseObject) valueForKey(key);
    				super.takeStoredValueForKey(null, key);
    				if(old != null) { 
    					if(old.isToManyKey(inverse)) {
    						NSArray values = (NSArray) old.valueForKey(inverse);
    						if(values.containsObject(this)) {
    							old.removeObjectFromPropertyWithKey(this, inverse);
    						}
    					} else {
    						if(old == this) {
    							old.takeValueForKey(null, inverse);
        					}
    					}
    				}
    			}
    		} else {
    			super.takeStoredValueForKey(object, key);
    		}
    	} else {
    		super.takeStoredValueForKey(object, key);
    	}
    }

    
    // Debugging aids -- turn off during production
    // These methods are used to catch the classic mistake of:
    // public String foo() { return (String)valueForKey("foo"); }
    // where foo is not a property key
    
    /*
     public Object storedValueForKey(String key) {
         // FIXME: turn this off during production
         if (!allPropertyKeys().containsObject(key))
             throw new RuntimeException("********* Tried to access storedValueForKey on "+entityName()+" on a non class property: "+key);
         Object value = super.storedValueForKey(key);
         return value;
     }
     
     public void takeStoredValueForKey(Object value, String key) {
         // FIXME: turn this off during production
         if (!allPropertyKeys().containsObject(key)) {
             throw new RuntimeException("********* Tried to takeStoredValueForKey on "+entityName()+" on a non class property: "+key);
         }
         super.takeStoredValueForKey(value,key);
     }
     
     */
    
    public static boolean usesDeferredFaultCreation() { return true; }
}
