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
    public static final Category willChangeCat = Category.getInstance("er.eo.willChange.ERXGenericRecord");
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public final static String KEY_MARKER="** KEY_MARKER **";

    public static class ERXGenericRecordClazz extends EOGenericRecordClazz {        
    }

    // DELETEME: Should remove once all dependency has been removed.
    public static void willFixToOneRelationship(String key, EOEnterpriseObject object) {
        // Not needed anymore, they fixed the bug!
    }
    
    // DELETEME: Should remove once all dependency has been removed.
    protected void _willFixRelationship(String key, EOEnterpriseObject object) {
        // Not needed anymore, they fixed the bug!
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
     * null.
     * @param ec editing context that used to be associated
     *		with the object.
     */
    public void didDelete(EOEditingContext ec) {
        if (tranCatDidDelete.isDebugEnabled())
            tranCatDidDelete.debug("Object:" + description());
    }
    /**
     * Called on the object after is has successfully
     * been updated.
     */
    public void didUpdate() {
        if (tranCatDidUpdate.isDebugEnabled())
            tranCatDidUpdate.debug("Object:" + description());
    }
    /**
     * Called on the object after is has successfully
     * been inserted.
     */
    public void didInsert() {
        if (tranCatDidInsert.isDebugEnabled())
            tranCatDidInsert.debug("Object:" + description());
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
                //(ak) commented out because when we have a mandatory, propagte PK relationship, this is called before the new OE is inserted.
                // cat.warn("******** Attempted to link to EOs through "+key+" when one of them was not in an editing context: "+this+":"+editingContext()+" and "+eo+ ":" + ((EOEnterpriseObject)eo).editingContext());
                if(editingContext()==null) throw new RuntimeException("******** Attempted to link to EOs through "+key+" when one of them was not in an editing context: "+this+":"+editingContext()+" and "+eo+ ":" + ((EOEnterpriseObject)eo).editingContext());
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

    /**
     * Gives the raw primary key of the object. This could be anything from
     * an NSData to a BigDecimal.
     * @return the raw primary key of this object.
     */
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
    // FIXME: This should work for compound pks as well, ie don't try to gen a new pk
    //		if they have a compound pk.
    public NSDictionary primaryKeyDictionary(boolean inTransaction) {
        if (!inTransaction && _primaryKeyDictionary == null) {
            if (primaryKey() != null) {
                //FIXME: Should be getting primaryKey name from the entity of the enterprise object.
                _primaryKeyDictionary = new NSDictionary(primaryKey(), "id");
            } else
                _primaryKeyDictionary = ERXUtilities.primaryKeyDictionaryForEntity(editingContext(), entityName());
        }
        return _primaryKeyDictionary;
    }

    /**
     * Determines what the value of the given key is in the committed
     * snapshot
     * @param key to be checked in committed snapshot
     * @return the committed snapshot value for the given key
     */
    public Object committedSnapshotValueForKey(String key) {
        return (editingContext().committedSnapshotForObject(this)).objectForKey(key);
    }

    /**
     * Computes the current set of changes that this object has from the
     * currently committed snapshot.
     * @return a dictionary holding the changed values from the currently
     *         committed snapshot.
     */
    public NSDictionary changesFromCommittedSnapshot() {
        return changesFromSnapshot(editingContext().committedSnapshotForObject(this));
    }

    /**
     * Simple method that will return if the parent object store of this object's editing
     * context is an instance of {@link EOObjectStoreCoordinator}. The reason this is important
     * is because if this condition evaluates to true then when changes are saved in this
     * editing context they will be propogated to the database.
     * @return if the parent object store of this object's editing context is an EOObjectStoreCoordinator.
     */
    public boolean parentObjectStoreIsObjectStoreCoordinator() {
        return editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator;
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
        EOEditingContext ec = editingContext();
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
    // MOVEME: Might be a canidate for EOGenericRecordClazz
    private static NSArray stringAttributeListForEntityNamed(String entityName) {
        // FIXME: this will need to be synchronized if you go full-MT
        NSArray result=(NSArray)_attributeKeysPerEntityName.objectForKey(entityName);
        if (result==null) {
            // FIXME: Bad way of getting the entity.
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

// Note that after an eo has been deleted its editingContext will be set to null and as such it can be a bit
// difficult to distinguish between a new EO and a deleted EO.
    public boolean isDeletedEO() {
        if (cat.isDebugEnabled())
            cat.debug("editingContext() = " + editingContext() + " this object: " + this);
        return editingContext() != null && editingContext().deletedObjects().containsObject(this);
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
    public boolean isNewEO() {
        return ERXExtensions.isNewObject(this);
    }

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

    /** Caches the context used for validations */
    // FIXME: We should have a better mechanism than this.
    private static D2WContext _validationContext;
    
    // DELETEME: Let's ditch this for now, it is kinda half baked
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
            // MOVEME: All of this rule based validation should move to ERXClassDescription
            //		also should be re-thought.
            // FIXME: Bad way of getting the entity.
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
        // CHECKME: This was a 4.5 issue, not sure if this one has been fixed yet.
        if (editingContext() != null && editingContext().deletedObjects().containsObject(this)) {
            validation.warn("Calling validate for save on an eo: " + this + " that has been marked for deletion!");
        }
        super.validateForSave();
        // FIXME: Should move all of the keys into on central place for easier management.
        // 	  Also might want to have a flag off of ERXApplication is debugging is enabled.
        if (ERXProperties.booleanForKey("ERDebuggingEnabled"))
            checkConsistency();
    }
    public void checkConsistency() throws NSValidation.ValidationException {}
    // batchCheckConsistency won't be called by validateForSave so you can check
    // things here that you only want checked by a batch process
    public void batchCheckConsistency() throws NSValidation.ValidationException {}
    
}
