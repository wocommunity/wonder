/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXEnterpriseObject;
import er.extensions.eof.ERXReplicableInterface;

/**
 * Diverse collection of utility methods for handling everything from
 * EOF to foundation. In the future this class will most likely be
 * split into more meaning full groups of utility methods.
 */
public class ERXUtilities {
    private static final Logger log = LoggerFactory.getLogger(ERXUtilities.class);

    /**
     * Utility method for returning all of the primary keys for
     * all of the objects that are marked for deletion in
     * the editing context.
     * @param ec editing context
     * @return an array containing all of the dictionaries of
     *		primary keys for all of the objects marked for
     *		deletion
     */
    // CHECKME: I don't think this is a value add
    public static NSArray deletedObjectsPKeys(EOEditingContext ec) {
        NSMutableArray result = new NSMutableArray();
        for (Enumeration e = ec.deletedObjects().objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo=(EOEnterpriseObject)e.nextElement();
            if (eo instanceof ERXEnterpriseObject)
                result.addObject(((ERXEnterpriseObject)eo).primaryKeyInTransaction());
            else
                result.addObject(EOUtilities.primaryKeyForObject(ec, eo));
        }
        return result;
    }
 
    /**
     * Traverses a key path to return the last {@link EORelationship}
     * object.
     * <p>
     * Note: that this method uses the object and not the model to traverse
     * the key path, this has the added benefit of handling EOF inheritance
     * @param object enterprise object to find the relationship off of
     * @param keyPath key path used to find the relationship
     * @return relationship object corresponding to the last property key of
     * 		the key path.
     */
    public static EORelationship relationshipWithObjectAndKeyPath(EOEnterpriseObject object, String keyPath) {
        EOEnterpriseObject lastEO = relationshipObjectWithObjectAndKeyPath(object, keyPath);
        EORelationship relationship = null;
        
        if (lastEO!=null) {
            EOEntity entity=ERXEOAccessUtilities.entityNamed(object.editingContext(), lastEO.entityName());
            String lastKey=ERXStringUtilities.lastPropertyKeyInKeyPath(keyPath);
            relationship=entity.relationshipNamed(lastKey);
        }
        return relationship;
    }

    
    public static NSDictionary relationshipEntityWithEntityAndKeyPath(EOEntity srcentity, String keyPath) {
        //keyPath is something like 'project.user.person.firstname'
        //we will get the Person entity
        if (keyPath.indexOf(".") == -1) {
            NSDictionary d = new NSDictionary(new Object[]{srcentity, keyPath}, new Object[]{"entity", "keyPath"});
            return d;
        }

        while (keyPath.indexOf(".") != -1) {
            String key = ERXStringUtilities.firstPropertyKeyInKeyPath(keyPath);
            EORelationship rel = srcentity.relationshipNamed(key);
            if (rel == null) {
                break;
            }
            srcentity = rel.destinationEntity();
            keyPath = ERXStringUtilities.keyPathWithoutFirstProperty(keyPath);
        }
        NSDictionary d = new NSDictionary(new Object[]{srcentity, keyPath}, new Object[]{"entity", "keyPath"});
        return d;
    }
    
    public static EOEnterpriseObject relationshipObjectWithObjectAndKeyPath(EOEnterpriseObject object, String keyPath) {
        if(object == null) {
        	return null;
        }
    	EOEnterpriseObject lastEO=object;
        if (keyPath.indexOf(".")!=-1) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(keyPath);
            Object rawLastEO=object.valueForKeyPath(partialKeyPath);
            lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
        }
        return lastEO;
    }

    /**
     * Simple utility method for deleting an array
     * of objects from an editing context.
     * @param ec editing context to have objects deleted from
     * @param objects objects to be deleted.
     */
    public static void deleteObjects(EOEditingContext ec, NSArray objects) {
        if (ec == null)
            throw new RuntimeException("Attempting to delete objects with a null editing context!");
        if (objects != null && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();)
                ec.deleteObject((EOEnterpriseObject)e.nextElement());            
        }
    }
    
    /**
     * Utility method to get all of the framework names that
     * have been loaded into the application.
     * @return array containing all of the framework names
     */
    public static NSArray allFrameworkNames() {
        NSMutableArray frameworkNames = new NSMutableArray();
        for (Enumeration e = NSBundle.frameworkBundles().objectEnumerator(); e.hasMoreElements();) {
            NSBundle bundle = (NSBundle)e.nextElement();
            if (bundle.name() != null)
                frameworkNames.addObject(bundle.name());
            else
                log.warn("Null framework name for bundle: {}", bundle);
        }
        return frameworkNames;
    }

    /**
     * Simple utility method for getting all of the
     * entities for all of the models of a given
     * model group.
     * @param group eo model group
     * @return array of all entities for a given model group.
     */
    public static NSArray entitiesForModelGroup(EOModelGroup group) {
        return ERXArrayUtilities.flatten((NSArray)group.models().valueForKey("entities"));
    }

    /** entity name cache */
    private static NSDictionary<String, EOEntity> _entityNameEntityCache;

    /**
     * Finds an entity given a case insensitive search
     * of all the entity names.
     * <p>
     * Note: The current implementation caches the entity-entity name
     * pair in an insensitive manner. This means that all of the
     * models should be loaded before this method is called.
     */
    // FIXME: Should add an EOEditingContext parameter to get the right
    //	      EOModelGroup. Should also have a way to clear the cache.
    // CHECKME: Should this even be cached?
    public static EOEntity caseInsensitiveEntityNamed(String entityName) {
        EOEntity entity = null;
        if (entityName != null) {
            if (_entityNameEntityCache == null) {
            	NSMutableDictionary<String, EOEntity>entityNameDict = new NSMutableDictionary<String, EOEntity>();
                for (Enumeration<EOEntity> e = entitiesForModelGroup(ERXEOAccessUtilities.modelGroup(null)).objectEnumerator(); e.hasMoreElements();) {
                    EOEntity anEntity = e.nextElement();
                    entityNameDict.setObjectForKey(anEntity, anEntity.name().toLowerCase());    
                }
                _entityNameEntityCache = entityNameDict;
            }
            entity = _entityNameEntityCache.objectForKey(entityName.toLowerCase());
        }
        return entity;
    }
   
    /**
     * Generates a string representation of the current stacktrace.
     *
     * @return current stacktrace.
     */
    public static String stackTrace() {
        String result = null;
        try {
            throw new Throwable();
        } catch (Throwable t) {
            result = ERXUtilities.stackTrace(t);
        }

        String separator = System.getProperties().getProperty("line.separator");

        // Chop off the 1st line, "java.lang.Throwable"
        //
        int offset = result.indexOf(separator);
        result = result.substring(offset+1);

        // Chop off the lines at the start that refer to ERXUtilities
        //
        offset = result.indexOf(separator);
        while (result.substring(0,offset).indexOf("ERXUtilities.java") >= 0) {
            result = result.substring(offset+1);
            offset = result.indexOf(separator);
        }
        return separator+result;
    }

    /**
     * Converts a throwable's stacktrace into a
     * string representation.
     * @param t throwable to print to a string
     * @return string representation of stacktrace
     */
    public static String stackTrace(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        PrintStream printStream = new PrintStream(baos);
        t.printStackTrace(printStream);
        return baos.toString();
    }

    /**
     * Useful interface for binding objects to
     * WOComponent bindings where you want to
     * delay the evaluation of the boolean operation
     * until <code>valueForBinding</code> is
     * actually called. See {@link ERXStatelessComponent}
     * for examples.
     */
    public static interface BooleanOperation {
        public boolean value();
    }

    /**
     * Useful interface for binding objects to
     * WOComponent bindings where you want to
     * delay the evaluation of the operation
     * until <code>valueForBinding</code> is
     * actually called. See {@link ERXStatelessComponent}
     * for examples.
     */
    public static interface Operation {
        public Object value();
    }

    /**
     * Generic callback interface with a context
     * object.
     */
    public static interface Callback {
        public Object invoke(Object ctx);
    }

    /**
     * Generic boolean callback interface with a
     * context object.
     */
    public static interface BooleanCallback {
        public boolean invoke(Object ctx);
    }

    /** @deprecated use {@link NSTimestamp#DistantFuture} */
    @Deprecated
    public static final NSTimestamp DISTANT_FUTURE = new NSTimestamp(2999,1,1,1,1,1,TimeZone.getDefault());
    /** @deprecated use {@link NSTimestamp#DistantFuture} */
    @Deprecated
    public static NSTimestamp distantFuture() { return DISTANT_FUTURE; }
    /** @deprecated use {@link NSTimestamp#DistantPast} */
    @Deprecated
    public static final NSTimestamp DISTANT_PAST = new NSTimestamp(1000,1,1,1,1,1,TimeZone.getDefault());
    /** @deprecated use {@link NSTimestamp#DistantPast} */
    @Deprecated
    public static NSTimestamp distantPast() { return DISTANT_PAST; }

    /**
     * Gets rid of all ' from a String.
     * @param aString string to check
     * @return string without '
     */
    // CHECKME: Is this a value add? I don't think so.
    public static String escapeApostrophe(String aString) {
        NSArray parts = NSArray.componentsSeparatedByString(aString,"'");
        return parts.componentsJoinedByString("");
    }

    /** Copies values from one EO to another using an array of Attributes */
    public static void replicateDataFromEOToEO(EOEnterpriseObject r1, EOEnterpriseObject r2, NSArray attributeNames){
        for(Enumeration e = attributeNames.objectEnumerator(); e.hasMoreElements();){
            String attributeName = (String)e.nextElement();
            r2.takeValueForKey(r1.valueForKey(attributeName), attributeName);
        }
    }

    /** Copies a relationship from one EO to another using the name of the relationship */
    public static void replicateRelationshipFromEOToEO(EOEnterpriseObject r1, EOEnterpriseObject r2, String relationshipName){
        for(Enumeration e = ((NSArray)r1.valueForKey(relationshipName)).objectEnumerator(); e.hasMoreElements();){
            ERXReplicableInterface replicableTarget = (ERXReplicableInterface)e.nextElement();
            r2.addObjectToBothSidesOfRelationshipWithKey(replicableTarget.replicate(r2.editingContext()), relationshipName);
        }
    }

    /** Copies a relationship from one EO to another using the name of the relationship */
    public static void deplicateRelationshipFromEO(EOEnterpriseObject r1, String relationshipName){
        //System.out.println("r1 "+r1);
        //System.out.println("relationshipName "+relationshipName);
        //System.out.println("array "+r1.valueForKey(relationshipName));
        for(Enumeration e = ((NSArray)r1.valueForKey(relationshipName)).objectEnumerator(); e.hasMoreElements();){
            ERXReplicableInterface replicableTarget = (ERXReplicableInterface)e.nextElement();
            //System.out.println("replicableTarget "+replicableTarget);
            r1.removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject)replicableTarget,
                                                              relationshipName);
            replicableTarget.deplicate();
        }
    }

    /**
     * Returns a deep clone of the given object.  A deep clone will attempt 
     * to clone any contained values (in the case of an NSArray or NSDictionary)
     * as well as the value itself.
     * 
     * @param obj the object to clone
     * @param onlyCollections if true, only collections will be cloned, not individual values
     * @return a deep clone of obj
     */
	@SuppressWarnings("unchecked")
	public static <T> T deepClone(T obj, boolean onlyCollections) {
		Object clone;
		if (obj instanceof NSArray) {
			clone = ERXArrayUtilities.deepClone((NSArray)obj, onlyCollections);
		}
		else if (obj instanceof NSSet) {
			clone = ERXArrayUtilities.deepClone((NSSet)obj, onlyCollections);
		}
		else if (obj instanceof NSDictionary) {
			clone = ERXDictionaryUtilities.deepClone((NSDictionary)obj, onlyCollections);
		}
		else if (!onlyCollections && obj instanceof Cloneable) {
            try {
				Method m = obj.getClass().getMethod("clone", ERXConstant.EmptyClassArray);
				clone = m.invoke(obj, ERXConstant.EmptyObjectArray);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to clone " + obj + ".", e);
			}
		}
		else {
			clone = obj;
		}
		return (T)clone;
	}
}
