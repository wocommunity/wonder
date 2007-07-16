/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This class contains a bunch of extensions to the
 * regular {@link com.webobjects.eocontrol.EOGenericRecord} class. Of notable
 * interest it contains built in support for generating
 * primary keys via the {@link ERXGeneratesPrimaryKeyInterface},
 * support for an augmented transaction methods like <code>
 * willUpdate</code> and <code>didDelete</code> and a bunch
 * of handy utility methods like <code>committedSnapshotValueForKey
 * </code>. At the moment it is required that those wishing to take
 * advantage of templatized and localized validation exceptions
 * need to subclass this class. Hopefully in the future we can
 * get rid of this requirement.
 */
public class ERXGenericRecord extends EOGenericRecord implements ERXGuardedObjectInterface, ERXGeneratesPrimaryKeyInterface {

    /** logging support. Called after an object is successfully inserted */
    public static final ERXLogger tranLogDidInsert = ERXLogger.getERXLogger("er.transaction.eo.did.insert.ERXGenericRecord");
    /** logging support. Called after an object is successfully deleted */
    public static final ERXLogger tranLogDidDelete = ERXLogger.getERXLogger("er.transaction.eo.did.delete.ERXGenericRecord");
    /** logging support. Called after an object is successfully updated */
    public static final ERXLogger tranLogDidUpdate = ERXLogger.getERXLogger("er.transaction.eo.did.update.ERXGenericRecord");
    /** logging support. Called after an object is reverted. **/
    public static final ERXLogger tranLogDidRevert = ERXLogger.getERXLogger("er.transaction.eo.did.revert.ERXGenericRecord");
    /** logging support. Called before an object is inserted */
    public static final ERXLogger tranLogWillInsert = ERXLogger.getERXLogger("er.transaction.eo.will.insert.ERXGenericRecord");
    /** logging support. Called before an object is deleted */
    public static final ERXLogger tranLogWillDelete = ERXLogger.getERXLogger("er.transaction.eo.will.delete.ERXGenericRecord");
    /** logging support. Called before an object is updated */
    public static final ERXLogger tranLogWillUpdate = ERXLogger.getERXLogger("er.transaction.eo.will.update.ERXGenericRecord");
    /** logging support. Called before an object is reverted. **/
    public static final ERXLogger tranLogWillRevert = ERXLogger.getERXLogger("er.transaction.eo.will.revert.ERXGenericRecord");
    /** logging support for validation information */
    public static final ERXLogger validation = ERXLogger.getERXLogger("er.eo.validation.ERXGenericRecord");
    /** logging support for validation exceptions */
    public static final ERXLogger validationException = ERXLogger.getERXLogger("er.eo.validationException.ERXGenericRecord");
    /** logging support for insertion tracking */
    public static final ERXLogger insertionTrackingLog = ERXLogger.getERXLogger("er.extensions.ERXGenericRecord.insertion");
    /** general logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger("er.eo.ERXGenericRecord");

    /** holds validity Methods */
    private static Method[] validityMethods = null;

    /** index of validity save method */ 
    private static int VALIDITY_SAVE = 0;

    /** index of validity delete method */ 
    private static int VALIDITY_DELETE = 1;

    /** index of validity insert method */
    private static int VALIDITY_INSERT = 2;

    /** index of validity update method */
    private static int VALIDITY_UPDATE = 3;

    /** the shared validity engine instance as Object to eliminate compile errors
        * if validity is not linked and should not be used
        */ 
    private static Object sharedGSVEngineInstance;

    /** Boolean that gets initialized on first use to indicate if validity should
        * be used or not, remember that the call System.getProperty acts synchronized
        * so this saves some time in multithreaded apps.
        */
    private static Boolean useValidity;
    
    /** holds all subclass related ERXLogger's */
    public static NSMutableDictionary classLogs = new NSMutableDictionary();
    public static final Object lock = new Object();
    
    public static boolean shouldTrimSpaces(){
        return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXGenericRecord.shouldTrimSpaces", false);
    }
    
    // DELETEME: Once we get rid of the half baked rule validation here, we can delete this.
    public final static String KEY_MARKER="** KEY_MARKER **";

    public static final NSSelector FlushCachesSelector = new NSSelector("flushCaches");

    /**
     * Clazz object implementation for ERXGenericRecord. See
     * {@link EOEnterpriseObjectClazz} for more information on this
     * neat design pattern.
     */
    public static class ERXGenericRecordClazz extends EOEnterpriseObjectClazz {
        
    }

    public String insertionStackTrace = null;
    
    /** This methods checks if we already have created an ERXLogger for this class
        * If not, one will be created, stored and returned on next request.
        * This method eliminates individual static variables for ERXLogger's in all
        * subclasses. We use an NSDictionary here because static fields are class specific
        * and thus something like lazy initialization would not work in this case.
        *
        * @return an {@link ERXLogger} for this objects class
        */
    public ERXLogger getClassLog() {
        ERXLogger log = (ERXLogger)classLogs.objectForKey(this.getClass());
        if ( log == null) {
            synchronized(lock) {
                log = ERXLogger.getERXLogger(this.getClass());
                classLogs.setObjectForKey(log, this.getClass());
            }
        }
        return log;
    }

    /**
        * self is usefull for directtoweb purposes
     */
    public ERXGenericRecord self(){
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

    /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * deleted from the database. This method is also called
     * before <code>validateForDelete</code> is called on this
     * object. This method is called by the editing context
     * delegate {@link ERXDefaultEditingContextDelegate}.
     * @throws NSValidation.ValidationException to stop the object
     *		from being deleted.
     */
    public void willDelete() throws NSValidation.ValidationException {
        if (canDelete() == false) {
            // FIXME: This shouldn't be a RuntimeException seeing as these are difficult to catch
            throw new RuntimeException("The ERXGenericRecord "+this+" cannot be deleted.");
        }
        if (tranLogWillDelete.isDebugEnabled())
            tranLogWillDelete.debug("Object:" + description());
    }
    
    /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * inserted into the database. This method is also called
     * before <code>validateForInsert</code> is called on this
     * object. This method is called by the editing context
     * delegate {@link ERXDefaultEditingContextDelegate}.
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

    /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * updated in the database. This method is also called
     * before <code>validateForSave</code> is called on this
     * object. This method is called by the editing context
     * delegate {@link ERXDefaultEditingContextDelegate}.
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

    /**
     * This is called when an object has had
     * changes merged into it by the editing context.
     * This is called by {@link ERXDefaultEditingContextDelegate}
     * after it merges changes. Any caches that an object
     * keeps based on any of it's values it should flush.
     * The default implementation of this method does
     * nothing.
     */
    public void flushCaches() {}

    /**
     * Called on the object after is has been deleted.
     * The editing context is passed to the object since
     * by this point the editingContext of the object is
     * null. You should check if the <code>ec</code>
     * is a child context when doing something here that
     * can't be undone.
     * @param ec editing context that used to be associated
     *		with the object.
     */
    public void didDelete(EOEditingContext ec) {
        if (tranLogDidDelete.isDebugEnabled())
            tranLogDidDelete.debug("Object:" + description());
    }
    /**
     * Called on the object after is has successfully
     * been updated in the database.
     */
    public void didUpdate() {
        if (tranLogDidUpdate.isDebugEnabled())
            tranLogDidUpdate.debug("Object:" + description());
    }
    /**
     * Called on the object after is has successfully
     * been inserted into the database.
     */
    public void didInsert() {
        if (tranLogDidInsert.isDebugEnabled())
            tranLogDidInsert.debug("Object:" + description());

        //We're goung to blow the primaryKey cache:
        _primaryKey = null;
        _permanentGlobalID = null;
    }

    /**
     * Called on the object before it will be reverted.
     *
     * Default implementation does nothing other than log.
     */
    public void willRevert() {
        if ( tranLogWillRevert.isDebugEnabled() )
            tranLogWillRevert.debug("Object: " + description());
    }

    /**
     * Called on the object after it has been reverted.
     * The editing context is passed to the object because
     * if the object was in the insertedObjects list before
     * the revert, the object has had its editingContext
     * nulled.
     *
     * Default implementation calls <code>flushCaches</code>.
     *
     * @param ec editing context that is either currently associated
     * with the object if the object was marked as changed or deleted before
     * the revert, otherwise the editing context that was associated with the object
     * before the revert.
     */
    public void didRevert(EOEditingContext ec) {
        if ( tranLogDidRevert.isDebugEnabled() )
            tranLogDidRevert.debug("Object: " + description());
        flushCaches();
    }

    /**
     * Adds a collection of objects to a given relationship by calling
     * <code>addObjectToBothSidesOfRelationshipWithKey</code> for all
     * objects in the collection.
     * @param objects objects to add to both sides of the given relationship
     * @param key relationship key
     */
    public void addObjectsToBothSidesOfRelationshipWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                addObjectToBothSidesOfRelationshipWithKey(eo, key);
            }
        }
    }

    /**
     * Removes a collection of objects to a given relationship by calling
     * <code>removeObjectFromBothSidesOfRelationshipWithKey</code> for all
     * objects in the collection.
     * @param objects objects to be removed from both sides of the given relationship
     * @param key relationship key
     */
    public void removeObjectsFromBothSidesOfRelationshipWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            NSArray objectsSafe = objects instanceof NSMutableArray ? (NSArray)objects.clone() : objects;
            for (Enumeration e = objectsSafe.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                removeObjectFromBothSidesOfRelationshipWithKey(eo, key);
             }
        }
    }

    /**
        * Removes a collection of objects to a given relationship by calling
     * <code>removeObjectFromPropertyWithKey</code> for all
     * objects in the collection.
     * @param objects objects to be removed from both sides of the given relationship
     * @param key relationship key
     */
    public void removeObjectsFromPropertyWithKey(NSArray objects, String key) {
        if (objects != null && objects.count() > 0) {
            NSArray objectsSafe = objects instanceof NSMutableArray ? (NSArray)objects.clone() : objects;
            for (Enumeration e = objectsSafe.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                removeObjectFromPropertyWithKey(eo, key);
            }
        }
    }
    
    /** 
     * caches the boolean value of the property key:
     *  <b>er.extensions.ERXRaiseOnMissingEditingContextDelegate</b>
     */
    // MOVEME: Need to have a central repository of all of these keys and what they mean
    static boolean _raiseOnMissingEditingContextDelegate = 	ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXRaiseOnMissingEditingContextDelegate"), true);
    /**
     * By default, and this should change in the future, all editing contexts that
     * are created and use ERXGenericRecords or subclasses need to have a delegate
     * set of instance {@link ERXEditingContextDelegate}. These delegates provide
     * the augmentation to the regular transaction mechanism, all of the will* methods
     * plus the flushCaching method. To change the default behaviour set the property:
     * <b>er.extensions.ERXRaiseOnMissingEditingContextDelegate</b> to false in your
     * WebObjects.properties file. This method is called when an object is fetched,
     * updated or inserted.
     * @param editingContext to check for the correct delegate.
     * @return if the editing context has the correct delegate set.
     */
    public boolean _checkEditingContextDelegate(EOEditingContext editingContext) {
        Object delegate=editingContext.delegate();

        if (delegate==null) {
	    EOObjectStore parent = editingContext.parentObjectStore();
	    if(!_raiseOnMissingEditingContextDelegate && parent != null && parent instanceof EOEditingContext) {
		Object parentDelegate=((EOEditingContext)parent).delegate();
		if(parentDelegate != null && (parentDelegate instanceof ERXEditingContextDelegate)) {
		    editingContext.setDelegate(parentDelegate);
		    log.info("Found null delegate. Setting to the parent's delegate.");
		    return true;
		}
	    }
	    if(!_raiseOnMissingEditingContextDelegate) {
		log.warn("Found null delegate. I will fix this for now by setting it to ERXExtensions.defaultDelegate");
		ERXExtensions.setDefaultDelegate(editingContext);
		return true;
	    } else {
		throw new RuntimeException("Found null delegate. You can disable this check by setting er.extensions.ERXRaiseOnMissingEditingContextDelegate=false in your WebObjects.properties");
	    }
	}
	if (delegate!=null && !(delegate instanceof ERXEditingContextDelegate)) {
	    if(!_raiseOnMissingEditingContextDelegate) {
		log.warn("Found unexpected delegate class: "+delegate.getClass().getName());
		return true;
	    } else {
		throw new RuntimeException("Found unexpected delegate class. You can disable this check by setting er.extensions.ERXRaiseOnMissingEditingContextDelegate=false in your WebObjects.properties");
	    }
	}
	return false;

    }
    /**
     * Checks the editing context delegate before calling
     * super's implementation. See the method <code>
     * _checkEditingContextDelegate</code> for an explanation
     * as to what this check does.
     * @param editingContext to be checked to make sure it has the
     *		correct type of delegate set.
     */
    public void awakeFromClientUpdate(EOEditingContext editingContext) {
        _checkEditingContextDelegate(editingContext);
        super.awakeFromClientUpdate(editingContext);
    }
    /**
     * Checks the editing context delegate before calling
     * super's implementation. See the method <code>
     * _checkEditingContextDelegate</code> for an explanation
     * as to what this check does.
     * @param editingContext to be checked to make sure it has the
     *		correct type of delegate set.
     */
    public void awakeFromInsertion(EOEditingContext editingContext) {
        _checkEditingContextDelegate(editingContext);
        if (insertionTrackingLog.isDebugEnabled()) {
            insertionStackTrace = ERXUtilities.stackTrace();
            insertionTrackingLog.debug("inserted "+getClass().getName()+" at "+insertionStackTrace);
        } else if (insertionTrackingLog.isInfoEnabled()) {
            insertionStackTrace = ERXUtilities.stackTrace();
        }            
        super.awakeFromInsertion(editingContext);
    }
    /**
     * Checks the editing context delegate before calling
     * super's implementation. See the method <code>
     * _checkEditingContextDelegate</code> for an explanation
     * as to what this check does.
     * @param editingContext to be checked to make sure it has the
     *		correct type of delegate set.
     */
    public void awakeFromFetch(EOEditingContext editingContext) {
        _checkEditingContextDelegate(editingContext);
        super.awakeFromFetch(editingContext);
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

    /**
     * Primary key of the object as a String.
     * @return primary key for the given object as a String
     */
    protected String _primaryKey = null;
    public String primaryKey() {
        if(_primaryKey == null) {
            _primaryKey = ERXEOControlUtilities.primaryKeyStringForObject(this);
        }
        return _primaryKey;
    }

    /**
     * Calling this method will return the primary key of the
     * given enterprise object or if one has not been asigned
     * to it yet, then it will have the adaptor channel generate
     * one for it, cache it and then use that primary key when it
     * is saved to the database. If you just want the
     * primary key of the object or null if it doesn't have one
     * yet, use the method <code>rawPrimaryKey</code>.
     * @return the primary key of this object.
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

    /**
     * Calling this method will return the primary key of the
     * given enterprise object or if one has not been assigned
     * to it yet, then it will have the adaptor channel generate
     * one for it, cache it and then use that primary key when it
     * is saved to the database. This method returns the string
     * representation of the primary key. If you just want the
     * primary key of the object or null if it doesn't have one
     * yet, use the method <code>primaryKey</code>.
     * @return string representation of the primary key of this
     *		object.
     */
    public String primaryKeyInTransaction() {
        return ERXEOControlUtilities._stringForPrimaryKey(rawPrimaryKeyInTransaction());
    }

    /**
     * Gives the raw primary key of the object. This could be anything from
     * an NSData to a BigDecimal.
     * @return the raw primary key of this object.
     */
    public Object rawPrimaryKey() {
        return ERXEOControlUtilities.primaryKeyObjectForObject(this);
    }

    /**
     * Takes the primary key of the object and encrypts it
     * with the blowfish cipher using {@link ERXCrypto ERXCrypto}.
     * @return blowfish encrypted primary key
     */
    public String encryptedPrimaryKey() {
        String pk = ERXEOControlUtilities.primaryKeyStringForObject(this);
        return pk==null ? null : ERXCrypto.blowfishEncode(pk);
    }
        
    /**
     * Returns the foreign key for a given relationship.
     * @param rel relationship key
     * @return foreign key for a given relationship.
     */
    public Object foreignKeyForRelationshipWithKey(String rel) {
        NSDictionary d=EOUtilities.destinationKeyForSourceObject(editingContext(), this, rel);
        return d != null && d.count()>0 ? d.allValues().objectAtIndex(0) : null;
    }


    public NSArray primaryKeyAttributeNames() {
        return entity().primaryKeyAttributeNames();
    }

    /**
     * Returns the entity for the current object.
     * Defers to {@link ERXEOAccessUtilities#entityNamed ERXEOAccessUtilities.entityNamed()} for the actual work.
     * @return EOEntity for the current object
     */
    public EOEntity entity() {
        return ERXEOAccessUtilities.entityNamed(editingContext(), entityName());
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
     *		middle of a transaction.
     * @return primary key dictionary for the current object, if the object does not have
     *		a primary key assigned yet and is not in the middle of a transaction then
     *		a new primary key dictionary is created, cached and returned.
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

    private EOKeyGlobalID _permanentGlobalID;

    /**
     * This method allows you to compute what the permanent EOGlobalID will be for an object before it has been
     * saved to the database.  It functions by calling into <code>primaryKeyDictionary()</code> to allocate the primary
     * key if necessary.  Then we build an EOKeyGlobalID from it.  If the object already has a permanent global ID, we use that.
     *
     * If you pass false for <code>generateIfMissing</code> and this object has a temporary global ID, null will be returned.
     */
    public EOKeyGlobalID permanentGlobalID(boolean generateIfMissing) {
        if ( _permanentGlobalID == null ) {
            final EOEditingContext ec = editingContext();

            if ( ec != null ) {
                final EOGlobalID gid = ec.globalIDForObject(this);

                if ( ! gid.isTemporary() ) {
                    _permanentGlobalID = (EOKeyGlobalID)gid;
                }
                else if ( generateIfMissing ) {
                    final NSDictionary primaryKeyDictionary = primaryKeyDictionary(false);
                    final Object[] values;

                    if ( primaryKeyDictionary.count() == 1 ) {
                        values = primaryKeyDictionary.allValues().objects();
                    }
                    else {
                        final NSArray sortedKeys = ERXDictionaryUtilities.stringKeysSortedAscending(primaryKeyDictionary);

                        values = primaryKeyDictionary.objectsForKeys(sortedKeys, null).objects();
                    }

                    _permanentGlobalID = EOKeyGlobalID.globalIDWithEntityName(entityName(), values);
                }
            }
        }

        return _permanentGlobalID;
    }

    /**
     * Calls permanentGlobalID(boolean) passing true for generateIfMissing.
     *
     * @see #permanentGlobalID(boolean)
     */
    public EOKeyGlobalID permanentGlobalID() {
        return permanentGlobalID(true);
    }

    /**
     * Determines what the value of the given key is in the committed
     * snapshot
     * @param key to be checked in committed snapshot
     * @return the committed snapshot value for the given key
     */
    public Object committedSnapshotValueForKey(String key) {
        NSDictionary snapshot = committedSnapshot();
        return snapshot != null ? snapshot.objectForKey(key) : null;
    }

    /**
     * This method exists because {@link com.webobjects.eocontrol.EOEditingContext#committedSnapshotForObject EOEditingContext.committedSnapshotForObject()}
     * gives unexpected results for newly inserted objects if {@link com.webobjects.eocontrol.EOEditingContext#processRecentChanges() EOEditingContext.processRecentChanges()} has been called.
     * This method always returns a dictionary whose values are all NSKeyValueCoding.NullValue in the case of a newly inserted object. 
     * @return the committed snapshot
     */
    public NSDictionary committedSnapshot() {
        if( !isNewObject() ) {
            return editingContext().committedSnapshotForObject(this);
        } else {
            NSArray keys = allPropertyKeys();
            NSMutableDictionary allNullDict = new NSMutableDictionary(keys.count());
            ERXDictionaryUtilities.setObjectForKeys(allNullDict, NSKeyValueCoding.NullValue, keys);
            return allNullDict;
        }
    }

    /**
     * Returns an EO in the same editing context as the caller.
     * @return an EO in the same editing context as the caller.
     */
    public EOEnterpriseObject localInstanceOf(EOEnterpriseObject eo) {
        return ERXEOControlUtilities.localInstanceOfObject(editingContext(), eo);
    }

    /**
     * Returns an array of EOs in the same editing context as the caller.
     * @return  array of EOs in the same editing context as the caller.
     */
    public NSArray localInstancesOf(NSArray eos) {
        return ERXEOControlUtilities.localInstancesOfObjects(editingContext(), eos);
    }

    /**
        * Computes the current set of changes that this object has from the
     * currently committed snapshot.
     * @return a dictionary holding the changed values from the currently
     *         committed snapshot.
     */
    public NSDictionary changesFromCommittedSnapshot() {
        return changesFromSnapshot(committedSnapshot());
    }

    /**
     * Simple method that will return if the parent object store of this object's editing
     * context is an instance of {@link com.webobjects.eocontrol.EOObjectStoreCoordinator EOObjectStoreCoordinator}. The reason this is important
     * is because if this condition evaluates to true then when changes are saved in this
     * editing context they will be propagated to the database.
     * @return if the parent object store of this object's editing context is an EOObjectStoreCoordinator.
     */
    public boolean parentObjectStoreIsObjectStoreCoordinator() {
        return editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator;
    }

    /**
     * Method that will make sure to fetch an eo from the Database and
     * place it in the editingContext provided
     * as an argument
     * @param the editing context in which the result will be placed
     * @return a fresh instance of an EO fetched from the DB and placed in the editing context argument
     */
    public ERXGenericRecord refetchObjectFromDBinEditingContext(EOEditingContext ec){
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName());
        EOQualifier qual = entity.qualifierForPrimaryKey(primaryKeyDictionary(false));
        EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName(), qual, null);
        fetchSpec.setRefreshesRefetchedObjects(true);
        NSArray results = ec.objectsWithFetchSpecification(fetchSpec);
        ERXGenericRecord freshObject = null;
        if(results.count()>0){
            freshObject = (ERXGenericRecord)results.objectAtIndex(0);
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
     *		object.
     */
    public String toString() {
        String pk = primaryKey();
        pk = (pk == null) ? "null" : pk;
        return "<" + getClass().getName() + " pk:\""+ pk + "\">";
    }

    /**
     * Cover method to return <code>toString</code>.
     * @return the results of calling toString.
     */
    public String description() { return toString(); }
    /**
     * Returns the super classes implementation of toString
     * which prints out the current key-value pairs for all
     * of the attributes and relationships for the current
     * object. Very verbose.
     * @return super's implementation of <code>toString</code>.
     */
    public String toLongString() { return super.toString(); }

    /** Caches the string attribute keys on a per entity name basis */
    private static NSMutableDictionary _attributeKeysPerEntityName=new NSMutableDictionary();
    /**
     * Calculates all of the EOAttributes of a given entity that
     * are mapped to String objects.
     * @return array of all attribute names that are mapped to
     *		String objects.
     */
    // MOVEME: Might be a canidate for EOEnterpriseObjectClazz
    private static synchronized NSArray stringAttributeListForEntityNamed(String entityName) {
        // FIXME: this will need to be synchronized if you go full-MT
        NSArray result=(NSArray)_attributeKeysPerEntityName.objectForKey(entityName);
        if (result==null) {
            // FIXME: Bad way of getting the entity.
            EOEntity entity=ERXEOAccessUtilities.entityNamed(null, entityName);
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

    /**
     * This method will trim the leading and trailing white
     * space from any attributes that are mapped to a String
     * object. This method is called before the object is saved
     * to the database. Override this method to do nothing if
     * you wish to preserve your leading and trailing white space.
     */
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

    /**
     * Determines if this object is a deleted object by
     * checking to see if it is included in the deletedObjects
     * array of the editing context or if it's editing context
     * is null.<br/>
     * <br/>
     * Note: An object that has just been created will also not
     * have an editing context and by this method would test
     * positive for being a deleted object.
     * @return if the object is a deleted object
     */
    // CHECKME: Might be able to tell better by checking EOGlobalIDs
    public boolean isDeletedEO() {
        if (log.isDebugEnabled())
            log.debug("editingContext() = " + editingContext() + " this object: " + this);
        return editingContext() != null && editingContext().deletedObjects().containsObject(this);
    }

    /**
        * @deprecated use {@link ERXGenericRecord#isNewObject() ERXGenericRecord#isNewObject}
     */
    public boolean isNewEO() {
        return isNewObject();
    }

    /**
        * Determines if this object is a new object and
     * hasn't been saved to the database yet. This
     * method just calls the method ERExtensions.isNewObject
     * passing in this object as the current parameter. Note
     * that an object that has been successfully deleted will
     * also look as if it is a new object because it will have
     * a null editingcontext.
     * @return if the object is a new enterprise object.
     */

    public boolean isNewObject() {
        return ERXExtensions.isNewObject(this);
    }
    
    
    /**
     * Called by an observer after an editing context has
     * successfully saved changes to a database. This method
     * enumerates through all of the objects that were inserted,
     * updated and deleted calling <code>didInsert</code>, <code>
     * didUpdate</code> and <code>didDelete</code> on the objects
     * respectively.
     * @param n notifcation posted after an editing context has
     *		successfully saved changes to the database.
     */
    // MOVEME: ERXECFactory move it onto the default factory so subclasses can provide different implementations
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
            if (e.key() == null || e.object() == null)
                e = new NSValidation.ValidationException(e.getMessage(), this, key);
            validationException.debug("Exception: " + e.getMessage() + " raised while validating object: "
                                      + this + " class: " + getClass() + " pKey: " + primaryKey() + "\n" + e);
            throw e;
        } catch (RuntimeException e) {
            log.error("**** During validateValueForKey "+key);
            log.error("**** caught "+e);
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
     *		pass validation for saving to the database.
     */
    public void validateForSave( )  throws NSValidation.ValidationException {
        // This condition shouldn't ever happen, but it does ;)
        // CHECKME: This was a 4.5 issue, not sure if this one has been fixed yet.
        if (editingContext() != null && editingContext().deletedObjects().containsObject(this)) {
            validation.warn("Calling validate for save on an eo: " + this + " that has been marked for deletion!");
        }
        if (useValidity()) {
            invokeValidityMethodWithType(VALIDITY_SAVE);
        }
        
        super.validateForSave();
        // FIXME: Should move all of the keys into on central place for easier management.
        // 	  Also might want to have a flag off of ERXApplication is debugging is enabled.
        // FIXME: Should have a better flag than just ERDebuggingEnabled
        if (ERXProperties.booleanForKey("ERDebuggingEnabled"))
            checkConsistency();
    }

    /**
     * This method uses Validity if the property key
     * <b>er.extensions.ERXGenericRecord.useValidity</b> is set to true
     * @throws NSValidation.ValidationException if the object does not
     *		pass validation for saving to the database.
     */
    public void validateForInsert() throws NSValidation.ValidationException {
        if (useValidity()) {
            invokeValidityMethodWithType(VALIDITY_INSERT);
        }
        EOClassDescription cd = classDescription();
        if(cd instanceof ERXEntityClassDescription) {
            ((ERXEntityClassDescription)cd).validateObjectForInsert(this);
        }
        super.validateForInsert();
    }

    /**
     * This method uses Validity if the property key
     * <b>er.extensions.ERXGenericRecord.useValidity</b> is set to true
     * @throws NSValidation.ValidationException if the object does not
     *		pass validation for saving to the database.
     */
    public void validateForUpdate() throws NSValidation.ValidationException {
        if (useValidity()) {
            invokeValidityMethodWithType(VALIDITY_UPDATE);
        }
        EOClassDescription cd = classDescription();
        if(cd instanceof ERXEntityClassDescription) {
            ((ERXEntityClassDescription)cd).validateObjectForUpdate(this);
        }
        super.validateForUpdate();
    }

    /**
     * This method uses Validity if the property key
     * <b>er.extensions.ERXGenericRecord.useValidity</b> is set to true
     * @throws NSValidation.ValidationException if the object does not
     *		pass validation for saving to the database.
     */
    public void validateForDelete() throws NSValidation.ValidationException {
        if (useValidity()) {
            invokeValidityMethodWithType(VALIDITY_DELETE);
        }
        super.validateForDelete();
    }

    private static boolean useValidity() {
        if (useValidity == null) {
            useValidity = "true".equals(System.getProperty("er.extensions.ERXGenericRecord.useValidity")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return useValidity.booleanValue();
    }


    private void invokeValidityMethodWithType(int type) throws NSValidation.ValidationException{
        try {
            Object dummy = null;
            Method m = validityMethods()[type];
            m.invoke(sharedGSVEngineInstance(), new Object[]{this});
        } catch (IllegalAccessException e1) {
            log.error("an exception occured in validityValidateEOObjectOnSave", e1);
        } catch (IllegalArgumentException e2) {
            log.error("an exception occured in validityValidateEOObjectOnSave", e2);
        } catch (NullPointerException e3) {
            log.error("an exception occured in validityValidateEOObjectOnSave", e3);
        } catch (InvocationTargetException e4) {
            Throwable targetException = e4.getTargetException();
            if (targetException instanceof NSValidation.ValidationException) {
                throw (NSValidation.ValidationException)targetException;
            } else {
                log.error("an exception occured in validityValidateEOObjectOnSave", e4);
            }
        }
    }

    private Method[] validityMethods() {
        if (validityMethods == null) {
            validityMethods = new Method[4];
            Method m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnSave");
            validityMethods[0] = m;
            
            m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnDelete");
            validityMethods[1] = m;
            
            m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnInsert");
            validityMethods[2] = m;
            
            m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnUpdate");
            validityMethods[3] = m;
        }
        return validityMethods;
    }
    
    private static Method methodInSharedGSVEngineInstanceWithName(String name) {
        try {
            return sharedGSVEngineInstance().getClass().getMethod(name, new Class[]{EOEnterpriseObject.class});
        } catch (IllegalArgumentException e2) {
            throw new NSForwardException(e2);
        } catch (NullPointerException e3) {
            throw new NSForwardException(e3);
        } catch (NoSuchMethodException e4) {
            throw new NSForwardException(e4);
        }
    }
    
    private static Object sharedGSVEngineInstance() {
        if (sharedGSVEngineInstance == null) {
            try {
                Class gsvEngineClass = Class.forName("com.gammastream.validity.GSVEngine");
                Method m = gsvEngineClass.getMethod("sharedValidationEngine", new Class[]{});
                Object dummy = null;
                sharedGSVEngineInstance = m.invoke(dummy, new Object[]{});
            } catch (ClassNotFoundException e1) {
                throw new NSForwardException(e1);
            } catch (NoSuchMethodException e2) {
                throw new NSForwardException(e2);
            } catch (IllegalAccessException e3) {
                throw new NSForwardException(e3);
            } catch (InvocationTargetException e4) {
                throw new NSForwardException(e4);
            }
        }
        return sharedGSVEngineInstance;
    }
    
    
    /**
        * Debugging method that will be called on an object before it is
     * saved to the database if the property key: <b>ERDebuggingEnabled</b>
     * is enabled. This allows for adding in a bunch of expensive validation
     * checks that should only be enabled in developement and testing
     * environments.
     * @throws NSValidation.ValidationException if the object is not consistent
     */
    // CHECKME: This method was very useful at NS, might not be as useful here.
    public void checkConsistency() throws NSValidation.ValidationException {}
    
    /**
        * This method is very similiar to the <code>checkConsistency</code> method
     * except that this method is only called from an outside process, usually
     * a batch process, to verify that the data this object holds is consistent.
     * JUnit tests are great for testing that all of the methods of a single
     * object function correctly, batch checking of consistency is a good way
     * of checking that all of the data in a given database is consistent. Hopefully
     * in the future we will add a batch check consistency application to demonstrate
     * the use of this method.
     * @throws NSValidation.ValidationException if the object fails consisntency
     */
    public void batchCheckConsistency() throws NSValidation.ValidationException {}
    
    
    
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
    
}
