//
// EOEnterpriseObjectClazz.java
// Project ERExtensions
//
// Created by ak on Fri Apr 12 2002
//
package er.extensions;

import java.util.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Adds class-level inheritance to EOF.<br />
 * Use subclasses of EOEnterpriseObjectClazz as inner classes in your EO subclasses
 * to work around the missing class object inheritance of java. They <b>must</b>
 * be named XXX.XXXClazz to work.<br />
 * Every subclass of this class will get their own "ClazzObject" instance, so it's
 * OK to store things which might be different in superclasses. That is, the "User"'s
 * implementation can override the "Person"'s and because Person.clazz() will get
 * it's own instance, it will do only "Person" things.<br />
 * The methods from EOUtilities are mirrored here so you don't have to import EOAccess
 * in your subclasses, which is not legal for client-side classes. The implementation
 * for a client-side class could then be easily switched to use the server-side EOUtilites
 * implementation.
 */
public class EOEnterpriseObjectClazz extends Object {
    /**
     * logging support
     */
    public static final ERXLogger log = ERXLogger.getERXLogger(EOEnterpriseObjectClazz.class);
    
    /**
     * caches the clazz objects
     */
    private static NSMutableDictionary allClazzes = new NSMutableDictionary();

    /**
     * caches the count attribute
     */
    private static EOAttribute _objectCountAttribute = null;

    /**
     * Creates and caches an eo attribute that can be
     * used to return the number of objects that a given
     * fetch specification will return.
     * @return eo count attribute
     */
    protected static EOAttribute objectCountAttribute() {
        if ( _objectCountAttribute == null ) {
            _objectCountAttribute = new EOAttribute();

            _objectCountAttribute.setName("p_objectCountAttribute");
            _objectCountAttribute.setColumnName("p_objectCountAttribute");
            _objectCountAttribute.setClassName("java.lang.Number");
            _objectCountAttribute.setValueType("i");
            _objectCountAttribute.setReadFormat("count(*)");
        }
        return _objectCountAttribute;
    }

    /**
     * @param foo 
     */
    protected static EOAttribute objectCountUniqueAttribute(EOAttribute foo) {
        EOAttribute tmp = new EOAttribute();

        tmp.setName("p_objectCountUnique"+foo.name());
        tmp.setColumnName("p_objectCountUnique"+foo.name());
        tmp.setClassName("java.lang.Number");
        tmp.setValueType("i");
        tmp.setReadFormat("count( unique t0."+foo.columnName()+")");
        return tmp;
    }

    /**
     * caches the entity name
     */
    private String _entityName;

    /**
     * Default public constructor
     */
    public EOEnterpriseObjectClazz() {
    }

    /**
     * Resets the clazz cache.
     */
    public static void resetClazzCache() { allClazzes.removeAllObjects(); }
    
    /**
     * Creates a clazz object for a given entity.
     * Will look for a clazz object with the name:
     * <entity name>$<entity name>Clazz.
     * @param entity to generate the clazz for
     * @return clazz object for the given entity
     */
    private static EOEnterpriseObjectClazz classFromEntity(EOEntity entity) {
        EOEnterpriseObjectClazz clazz = null;
        if(entity == null) {
            return new EOEnterpriseObjectClazz();
        }
        try {
            String className = entity.className();
            if(className.equals("ERXGenericRecord"))
                clazz = new ERXGenericRecord.ERXGenericRecordClazz();
            else
                clazz = (EOEnterpriseObjectClazz)Class.forName(className + "$" + entity.name() + "Clazz").newInstance();
        } catch (InstantiationException ex) {
        } catch (ClassNotFoundException ex) {
        } catch (IllegalAccessException ex) {
        }
        if(clazz == null) return classFromEntity(entity.parentEntity());
        return clazz;
    }
    
    /**
     * Method used to get a clazz object for a given entity name.
     * This method will cache the generated clazz object so that
     * for a given entity name only one clazz object will be created.
     * @param entityName name of the entity to get the Clazz object for
     * @return clazz object for the given entity
     */
    public static EOEnterpriseObjectClazz clazzForEntityNamed(String entityName) {
        EOEnterpriseObjectClazz clazz = (EOEnterpriseObjectClazz)allClazzes.objectForKey(entityName);
        if(clazz == null) {
            clazz = classFromEntity(ERXEOAccessUtilities.entityNamed(null, entityName));
            clazz.setEntityName(entityName);
            allClazzes.setObjectForKey(clazz,entityName);
        }
        if(log.isDebugEnabled()) {
            log.debug("clazzForEntityNamed '" +entityName+ "': " + clazz.getClass().getName());
        }
        return clazz;
    }
    
    /**
     * Creates and inserts an object of the type of
     * the clazz into the given editing context.
     * @param ec an editing context
     * @return newly created and inserted object
     */
    public EOEnterpriseObject createAndInsertObject(EOEditingContext ec) {
        EOEnterpriseObject eo = ERXEOControlUtilities.createAndInsertObject(ec,entityName());
        return eo;
    }

    /**
     * Generates an array of primary key values for
     * the clazz's entity. Uses the database context
     * for the entity's model and the given editingcontext.
     * @param ec am editing context
     * @param i number of primary keys to generate
     * @return array of new primary keys
     */
    public NSArray newPrimaryKeys(EOEditingContext ec, int i) {
        EOEntity entity = entity(ec);
        EODatabaseContext dc = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        
        return dc.availableChannel().adaptorChannel().primaryKeysForNewRowsWithEntity(i, entity);
    }

    /**
     * Gets all of the objects for the clazz's entity.
     * Just a cover method for the {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * method <code>objectsForEntityNamed</code>.
     * @param ec editingcontext to fetch the objects into
     * @return array of all the objects for a given entity name.
     */
    public NSArray allObjects(EOEditingContext ec) {
        return EOUtilities.objectsForEntityNamed(ec, entityName());
    }

    /**
     * Creates an enterprise object from a raw row
     * for the clazz's entity in the given editing context.
     * @param ec editing context to create the eo in
     * @param dict raw row dictionary
     * @return enterprise object for the raw row
     */
    public EOEnterpriseObject objectFromRawRow(EOEditingContext ec, NSDictionary dict) {
        return EOUtilities.objectFromRawRow(ec, entityNameFromRawRow(ec, dict), dict);
    }

    /**
     * Utility method to get the entity name from a raw row dictionary, taking subclasses and restricting qualifiers into account.
     * @param ec an editing context
     * @param dict raw row dictionary
     * @return entity name, if any
     */
    protected String entityNameFromRawRow(EOEditingContext ec, NSDictionary dict) {
        String entityName = entityName();
        EOEntity entity = entity(ec);
        if(entity.isAbstractEntity() && entity.subEntities().count() > 0) {
            for(Enumeration e = entity.subEntities().objectEnumerator(); e.hasMoreElements();) {
                EOEntity sub = (EOEntity)e.nextElement();
                if(sub.restrictingQualifier() != null) {
                    if(sub.restrictingQualifier().evaluateWithObject(dict)) {
                        return sub.name();
                    }
                } else {
                    if(sub.isAbstractEntity()) {
                        // do sth useful?
                    }
                }
            }
        }
        return entityName;
    }

    /**
     * Fetches the enterprise object for the specified
     * primary key value and corresponding to the clazz's
     * entity name.
     * @param ec editing context to fetch into
     * @param pk primary key value. Compound primary keys are given as NSDictionaries.
     * @return enterprise object for the specified primary key value.
     */
    public EOEnterpriseObject objectWithPrimaryKeyValue(EOEditingContext ec, Object pk) {
        return ERXEOControlUtilities.objectWithPrimaryKeyValue(ec, entityName(), pk, null);
    }

    /**
     * Fetches all of the objects matching the given qualifer
     * format corresponding to the clazz's entity using the
     * given editing context.
     * @param ec editing context
     * @param qualifier qualifier string
     * @param args qualifier format arguments
     * @param qualifer format string
     * @return array of objects corresponding to the passed in parameters.
     */
    public NSArray objectsWithQualifierFormat(EOEditingContext ec, String qualifier, NSArray args) {
        return EOUtilities.objectsWithQualifierFormat(ec, entityName(), qualifier, args);
    }

    /**
     * Fetchs an array of objects for a given fetch specification
     * and an array of bindings. The fetch specifiation is resolved
     * off of the entity corresponding to the current clazz.
     * @param ec editing content to fetch into
     * @param name fetch specification name
     * @param bindings used to resolve binding keys within the fetch 
     *     specification
     * @return array of objects fetched using the given fetch specification
     */
    public NSArray objectsWithFetchSpecificationAndBindings(EOEditingContext ec, String name, NSDictionary bindings) {
        return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, entityName(), name, bindings);
    }

    /**
     * Sets the entity name of the clazz.
     * @param name of the entity
     */
    public void setEntityName(String name) { _entityName = name; }
    
    /**
     * Gets the entity name of the clazz.
     * @return entity name of the clazz.
     */
    public String entityName() { return _entityName; }

    /**
     * Gets the entity corresponding to the entity
     * name of the clazz.
     * @return entity for the clazz
     */
    public EOEntity entity() {
        return entity(null);
    }

    /**
     * Gets the entity corresponding to the entity
     * name of the clazz.
     * @param ec an editing context
     * @return entity for the clazz
     */
    public EOEntity entity(EOEditingContext ec) {
        return ERXEOAccessUtilities.entityNamed(ec,entityName());
    }

    /**
     * Gets a fetch specification for a given name.
     * @param name of the fetch specification
     * @return fetch specification for the given name and the clazz's entity 
     *     name
     */
    public EOFetchSpecification fetchSpecificationNamed(String name) {
        return fetchSpecificationNamed(null,name);
    }

    /**
     * Gets a fetch specification for a given name.
     * @param ec editing context to use for finding the model group
     * @param name of the fetch specification
     * @return fetch specification for the given name and the clazz's entity 
     *     name
     */
    public EOFetchSpecification fetchSpecificationNamed(EOEditingContext ec, String name) {
        return entity(ec).fetchSpecificationNamed(name);
    }

    /**
     * Returns the number of objects matching the given
     * qualifier for the clazz's entity name. Implementation
     * wise this method will generate the correct sql to only
     * perform a count, i.e. all of the objects wouldn't be
     * pulled into memory.
     * @param ec editing context to use for the count qualification
     * @param qualifier to find the matching objects
     * @return number of matching objects
     */
    public Number objectCountWithQualifier(EOEditingContext ec, EOQualifier qualifier) {
        String entityName = entityName();
        
        NSArray results = null;

        EOAttribute attribute = EOEnterpriseObjectClazz.objectCountAttribute();
        EOEntity entity = entity(ec);
        EOQualifier schemaBasedQualifier = entity.schemaBasedQualifier(qualifier);
        EOFetchSpecification fs = new EOFetchSpecification(entityName, schemaBasedQualifier, null);
        synchronized (entity) {
            entity.addAttribute(attribute);

            fs.setFetchesRawRows(true);
            fs.setRawRowKeyPaths(new NSArray(attribute.name()));

            results = ec.objectsWithFetchSpecification(fs);

            entity.removeAttribute(attribute);
        }
        if ((results != null) && (results.count() == 1)) {
            NSDictionary row = (NSDictionary) results.lastObject();
            return (Number)row.objectForKey(attribute.name());
        }
        return null;
    }

    /**
     * Find the number of objects matching the given fetch
     * specification and bindings for the clazz's entity
     * name. Implementation wise the sql generated will
     * only return the count of the query, not all of the
     * rows matching the qualification.
     * @param ec ec used to perform the count in
     * @param fetchSpecName name of the fetch specification
     * @param bindings dictionary of bindings for the fetch 
     *     specification
     * @return number of objects matching the given fetch  specification and 
     *     bindings
     */
    public Number objectCountWithFetchSpecificationAndBindings(EOEditingContext ec, String fetchSpecName,  NSDictionary bindings) {
        String entityName = entityName();
        EOFetchSpecification unboundFetchSpec;
        EOFetchSpecification boundFetchSpec;

        unboundFetchSpec = fetchSpecificationNamed(ec, fetchSpecName);
        if (unboundFetchSpec == null) {
            return null;
        }
        boundFetchSpec = unboundFetchSpec.fetchSpecificationWithQualifierBindings(bindings);
        return objectCountWithQualifier(ec, boundFetchSpec.qualifier());
    }

    /**
     * Constructs a fetch specification that will only fetch the primary
     * keys for a given qualifier.
     * @param ec editing context, not used
     * @param eoqualifier to construct the fetch spec with
     * @param sortOrderings array of sort orderings to sort the result 
     *     set with.
     * @param additionalKeys array of additional key paths to construct
     *      the raw rows key paths to fetch.
     * @return fetch specification that can be used to fetch primary keys for 
     *     a given qualifier and sort orderings.
     */
    // FIXME: The ec parameter is not needed, nor used.
    public EOFetchSpecification primaryKeyFetchSpecificationForEntity(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings, NSArray additionalKeys) {
        String entityName = entityName();
        EOFetchSpecification fs = new EOFetchSpecification(entityName, eoqualifier, sortOrderings);
        fs.setFetchesRawRows(true);
        EOEntity entity = entity(ec);
        NSMutableArray keys = new NSMutableArray(entity.primaryKeyAttributeNames());
        if(additionalKeys != null) {
            keys.addObjectsFromArray(additionalKeys);
        }
        if(entity.restrictingQualifier() != null) {
            NSArray restrict = entity.restrictingQualifier().allQualifierKeys().allObjects();
            keys.addObjectsFromArray(restrict);
        }
        if(entity.isAbstractEntity()) {
            NSArray restrict = (NSArray)entity.subEntities().valueForKeyPath("restrictingQualifier.allQualifierKeys.allObjects.@flatten.@unique");
            keys.addObjectsFromArray(restrict);
        }
        fs.setRawRowKeyPaths(keys);
        return fs;
    }

    /**
     * Fetches an array of primary keys matching a given qualifier
     * and sorted with a given array of sort orderings.
     * @param ec editing context to fetch into
     * @param eoqualifier to restrict matching primary keys
     * @param sortOrderings array of sort orders to sort result set
     * @return array of primary keys matching a given qualifier
     */
    public NSArray primaryKeysMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings) {
        String entityName = entityName();
        EOFetchSpecification fs = primaryKeyFetchSpecificationForEntity(ec, eoqualifier, sortOrderings, null);
        //NSArray nsarray = EOUtilities.rawRowsForQualifierFormat(ec, fs.qualifier(), );
        NSArray nsarray = ec.objectsWithFetchSpecification(fs);
        return nsarray;
    }

    /**
     * Fetches an array of primary keys matching the values
     * in a given dictionary.
     * @param ec editing context to fetch into
     * @param nsdictionary dictionary of key value pairs to match 
     *     against.
     * @param sortOrderings array of sort orders to sort the result set
     *      by.
     * @return array of primary keys matching the given criteria.
     */
    public NSArray primaryKeysMatchingValues(EOEditingContext ec, NSDictionary nsdictionary, NSArray sortOrderings) {
        String entityName = entityName();
        return primaryKeysMatchingQualifier(ec, EOQualifier.qualifierToMatchAllValues(nsdictionary), sortOrderings);
    }

    /**
     * Constructs an array of faults for a given array
     * of primary keys in a given editing context for
     * the clazz's entity.
     * @param ec editing context to construct the faults in
     * @param nsarray array of primary key dictionaries
     * @return array of faults for an array of primary key dictionaries.
     */
    public NSArray faultsFromRawRows(EOEditingContext ec, NSArray nsarray) {
        String entityName = entityName();
        int count = nsarray.count();
        NSMutableArray faults = new NSMutableArray(count);
        for( int i = 0; i < count; i ++ ) {
            faults.addObject(objectFromRawRow(ec, (NSDictionary)nsarray.objectAtIndex(i)));
        }
        return faults;
    }
    
    /**
     * Fetches an array of faults matching a given qualifier.
     * @param ec editing context to use to fetch into
     * @param eoqualifier qualifier to match against
     * @return array of faults that match the given qualifier
     */
    public NSArray faultsMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier) {
        String entityName = entityName();
        NSArray nsarray = primaryKeysMatchingQualifier(ec, eoqualifier, null);
        return faultsFromRawRows(ec, nsarray);
    }

    /**
     * Fetches an array of faults matching a given qualifier
     * and sorted by an array of sort orderings.
     * @param ec editing context to use to fetch into
     * @param eoqualifier qualifier to match against
     * @param sortOrderings array of sort orderings to order the faults
     * @return array of faults that match the given qualifier
     */
    public NSArray faultsMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings) {
        String entityName = entityName();
        NSArray nsarray = primaryKeysMatchingQualifier(ec, eoqualifier, sortOrderings);
        return faultsFromRawRows(ec, nsarray);
    }

    /**
     * Fetches an array of faults for a given set of criteria.
     * @param ec editing context to use to fetch into
     * @param nsdictionary key value criteria to match against
     * @param sortOrderings array of sort orderings to order the faults
     * @return array of faults that match the given criteria
     */
    public NSArray faultsMatchingValues(EOEditingContext ec, NSDictionary nsdictionary, NSArray sortOrderings) {
        String entityName = entityName();
        NSArray nsarray = primaryKeysMatchingValues(ec, nsdictionary, sortOrderings);
        return faultsFromRawRows(ec, nsarray);
    }
}
