//
// EOEnterpriseObjectClazz.java
// Project ERExtensions
//
// Created by ak on Fri Apr 12 2002
//
package er.extensions.eof;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXPatcher;

/**
 * <h3>Adds class-level inheritance to EOF.</h3>
 * In Java, static methods are similar to class methods in Objective-C, but
 * one cannot use static methods in interfaces and static methods cannot be overridden 
 * by a subclass. Using the clazz pattern removes those limitations.
 * <p>
 * Instead of using a static method, we can use a static inner class (a clazz) 
 * instead. This allows for the methods on the clazz to be available statically to 
 * the class. The advantage is that static utility methods don't need to be 
 * generated for every subclass of an EOEnterpriseObject. It is generally sufficient to
 * simply use the utility methods available on the EOEnterpriseObjectClazz.
 * <p>
 * Every subclass of this class will get their own "ClazzObject" instance, so it's
 * OK to store things which might be different in superclasses. That is, the "User"'s
 * implementation can override the "Person"'s and because Person.clazz() will get
 * it's own instance, it will do only "Person" things.
 * <p>
 * Use subclasses of EOEnterpriseObjectClazz as inner classes in your EO subclasses
 * to work around the missing class object inheritance of java. They <b>must</b>
 * be named XX.XXClazz to work.
 * <p>
 * The methods from EOUtilities are mirrored here so you don't have to import EOAccess
 * in your subclasses, which is not legal for client-side classes. The implementation
 * for a client-side class could then be easily switched to use the server-side EOUtilites
 * implementation.
 * 
 * @param <T> 
 */
public class EOEnterpriseObjectClazz<T extends EOEnterpriseObject> {
    /**
     * logging support
     */
    public static final Logger log = Logger.getLogger(EOEnterpriseObjectClazz.class);
    
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
        tmp.setReadFormat("count( distinct t0."+foo.columnName()+")");
        return tmp;
    }

    /**
     * caches the entity name
     */
    private String _entityName;

    /**
     * Default public constructor. In case you let your code generate with a template,
     * you can simply call:<pre><code>
     * public static FooClazz clazz = new FooClazz();
     * </code></pre> and the constructor will auto-discover your entity name. This only
     * works when you have a concrete subclass for the entity in question, though.
     */
    public EOEnterpriseObjectClazz() {
    	initialize();
    }
	
    /**
     * Called by the constructor.
     *
     */
    protected void initialize() {
	}
	
	protected void discoverEntityName() {
		// AK: If your class is enclosed by a EO subclass the constructor 
		// will auto-discover the corresponding entity name. Not sure we need this, though.
		if(_entityName == null) {
			String className = getClass().getName();
			int index = className.indexOf('$');
			if(index > 0) {
				className = className.substring(0, index);
				Class c = ERXPatcher.classForName(className);
				if(c != null) {
					// we should use the class description, but it's too early for that when we 
					// do this as a result of a static variable init.
					NSArray entities = (NSArray) EOModelGroup.defaultGroup().models().valueForKeyPath("entities.@flatten");
					EOQualifier q = new EOKeyValueQualifier("className", EOQualifier.QualifierOperatorEqual, className);
					NSArray candidates = EOQualifier.filteredArrayWithQualifier(entities, q);
					if(candidates.count() > 1) {
						log.warn("More than one entity found: " + candidates);
					}
					EOEntity entity = (EOEntity) candidates.lastObject();
					if(entity != null) {
						String entityName = entity.name();
						// HACK AK: this relies on you having set up your classes correctly,
						// meaning that you have exactly one final class var per EO class, with the correct
						// superclasses set up (so EOBase gets loaded before EOSubclass)
						if(allClazzes.containsKey(entityName)) {
							_entityName = entityName;
						} else {
							setEntityName(entityName);
						}
					}
				}
			}
		}
	}

    /**
     * Constructor that also supplies an entity name.
     * @param entityName
     */
    public EOEnterpriseObjectClazz(String entityName) {
    	setEntityName(entityName);
    }
    
    /**
     * Convenience init so you can chain constructor calls:<pre><code>
     * public static FooClazz clazz = (FooClazz)new FooClazz().init("Foo");
     * </code></pre>
     * without having to override the default constructor or the one 
     * that takes an entity name. Also useful when you don't have a special
     * clazz defined for your entity, but would rather take one from a superclass.
     * @param entityName
     */
    public EOEnterpriseObjectClazz init(String entityName) {
    	setEntityName(entityName);
		return this;
    }

    /**
     * Returns the class description for the entity.
     */
    
    public EOClassDescription classDescription() {
    	return entity().classDescriptionForInstances();
    }

    /**
     * Utility to return a new array datasource
     * @param ec
     */
    public EOArrayDataSource newArrayDataSource(EOEditingContext ec) {
    	return new EOArrayDataSource(classDescription(), ec);
    }
    
    /**
     * Utility to return a new database datasource
     * @param ec
     */
     public EODatabaseDataSource newDatabaseDataSource(EOEditingContext ec) {
    	return new EODatabaseDataSource(ec, entityName());
    }
    

    /**
     * Resets the clazz cache.
     */
    public static void resetClazzCache() { allClazzes.removeAllObjects(); }
    
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
            clazz = factory().classFromEntity(ERXEOAccessUtilities.entityNamed(null, entityName));
            clazz.setEntityName(entityName);
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
    public T createAndInsertObject(EOEditingContext ec) {
        T eo = (T) ERXEOControlUtilities.createAndInsertObject(ec,entityName());
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
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        dbc.lock();
        try {
        	return dbc.availableChannel().adaptorChannel().primaryKeysForNewRowsWithEntity(i, entity);
        } finally {
        	dbc.unlock();
        }
    }

    /**
     * Gets all of the objects for the clazz's entity.
     * Just a cover method for the {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * method <code>objectsForEntityNamed</code>.
     * @param ec editingcontext to fetch the objects into
     * @return array of all the objects for a given entity name.
     */
    public NSArray<T> allObjects(EOEditingContext ec) {
        return EOUtilities.objectsForEntityNamed(ec, entityName());
    }

    /**
     * Creates an enterprise object from a raw row
     * for the clazz's entity in the given editing context.
     * @param ec editing context to create the eo in
     * @param dict raw row dictionary
     * @return enterprise object for the raw row
     */
    public T objectFromRawRow(EOEditingContext ec, NSDictionary dict) {
        return (T) EOUtilities.objectFromRawRow(ec, entityNameFromRawRow(ec, dict), dict);
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
    public T objectWithPrimaryKeyValue(EOEditingContext ec, Object pk) {
        return (T) ERXEOControlUtilities.objectWithPrimaryKeyValue(ec, entityName(), pk, null);
    }

    /**
     * Fetches all of the objects matching the given qualifier
     * format corresponding to the clazz's entity using the
     * given editing context.
     *
     * @param ec editing context
     * @param qualifier qualifier string
     * @param args qualifier format arguments
     *
     * @return array of objects corresponding to the passed in parameters.
     */
    public NSArray<T> objectsWithQualifierFormat(EOEditingContext ec, String qualifier, NSArray args) {
        return EOUtilities.objectsWithQualifierFormat(ec, entityName(), qualifier, args);
    }
    
    /**
     * Fetches all of the objects matching the given key and value
     * corresponding to the clazz's entity using the
     * given editing context.
     * @param ec editing context
     * @param key key string
     * @param value value
     * @return array of objects corresponding to the passed in parameters.
     */
    public NSArray<T> objectsMatchingKeyAndValue(EOEditingContext ec, String key, Object value) {
        return EOUtilities.objectsMatchingKeyAndValue(ec, entityName(), key, value);
    }
    
    public NSArray<T> objectsMatchingQualifier(EOEditingContext ec, EOQualifier qualifier) {
        return objectsMatchingQualifier(ec, qualifier, null);
    }
    
    public NSArray<T> objectsMatchingQualifier(EOEditingContext ec, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrdering) {
        return ec.objectsWithFetchSpecification(new EOFetchSpecification(entityName(), qualifier, sortOrdering));
    }
    
    
    
    
    /**
     * Fetches the object matching the given key and value
     * corresponding to the clazz's entity using the
     * given editing context. If more than one matches, throws a EOMoreThanOneException, 
     * otherwise returns null or the match.
     * @param ec editing context
     * @param key key string
     * @param value value
     * @return array of objects corresponding to the passed in parameters.
     */
    public T objectMatchingKeyAndValue(EOEditingContext ec, String key, Object value) {
        NSArray<T> result = objectsMatchingKeyAndValue(ec, key, value);
        if(result.count() > 1) {
        	throw new EOUtilities.MoreThanOneException("More than one: " + key + "->" + value);
        }
        return result.lastObject();
    }

    /**
     * Fetches an array of objects for a given fetch specification
     * and an array of bindings. The fetch specifiation is resolved
     * off of the entity corresponding to the current clazz.
     * @param ec editing content to fetch into
     * @param name fetch specification name
     * @param bindings used to resolve binding keys within the fetch 
     *     specification
     * @return array of objects fetched using the given fetch specification
     */
    public NSArray<T> objectsWithFetchSpecificationAndBindings(EOEditingContext ec, String name, NSDictionary bindings) {
        return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, entityName(), name, bindings);
    }

    /**
     * Sets the entity name of the clazz. Also registers the clazz in the cache.
     * @param name of the entity
     */
    protected void setEntityName(String name) {
		_entityName = name;
    	allClazzes.setObjectForKey(this, _entityName);
	}
    
    /**
     * Gets the entity name of the clazz.
     * @return entity name of the clazz.
     */
	public String entityName() {
		discoverEntityName();
		return _entityName;
	}

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
     * Creates a fetch spec for the entity.
     * @return fetch specification for the given name and the clazz's entity 
     *     name
     */
    public ERXFetchSpecification<T> createFetchSpecification(EOQualifier qualifier, NSArray<EOSortOrdering> sortings) {
        return new ERXFetchSpecification(entityName(), qualifier, sortings);
    }

    /**
     * Filters an array with a given fetch spec.
     * @param array
     * @param spec
     * @param bindings
     */
    public NSArray<T> filteredArray(NSArray<T> array, EOFetchSpecification spec, NSDictionary bindings) {
    	EOQualifier qualifier;

        if (bindings != null) {
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);
        }

        NSArray<T> result = new NSArray(array);
        
        qualifier = spec.qualifier();
        
        if (qualifier != null) {
            result = EOQualifier.filteredArrayWithQualifier(result, qualifier);
        }
        NSArray sortOrderings = spec.sortOrderings(); 
        if (sortOrderings != null) {
            result = EOSortOrdering.sortedArrayUsingKeyOrderArray(result,sortOrderings);
        }

        return result;
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
    	return (Number) ERXEOControlUtilities._aggregateFunctionWithQualifierAndAggregateAttribute(ec, entityName(), qualifier, EOEnterpriseObjectClazz.objectCountAttribute());
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
     * @deprecated use {@link #primaryKeyFetchSpecificationForEntity(EOQualifier, NSArray, NSArray)} instead
     */
    @Deprecated
    public EOFetchSpecification primaryKeyFetchSpecificationForEntity(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings, NSArray additionalKeys) {
        return primaryKeyFetchSpecificationForEntity(eoqualifier, sortOrderings, additionalKeys);
    }

    /**
     * Constructs a fetch specification that will only fetch the primary
     * keys for a given qualifier.
     * 
     * @param qualifier to construct the fetch spec with
     * @param sortOrderings array of sort orderings to sort the result 
     *     set with.
     * @param additionalKeys array of additional key paths to construct
     *      the raw rows key paths to fetch.
     * @return fetch specification that can be used to fetch primary keys for 
     *     a given qualifier and sort orderings.
     */
    public EOFetchSpecification primaryKeyFetchSpecificationForEntity(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, NSArray<String> additionalKeys) {
        String entityName = entityName();
        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, sortOrderings);
        fs.setFetchesRawRows(true);
        EOEntity entity = entity();
        NSMutableArray<String> keys = new NSMutableArray<>(entity.primaryKeyAttributeNames());
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
        EOFetchSpecification fs = primaryKeyFetchSpecificationForEntity(eoqualifier, sortOrderings, null);
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
    public NSArray<T> faultsFromRawRows(EOEditingContext ec, NSArray nsarray) {
        int count = nsarray.count();
        NSMutableArray<T> faults = new NSMutableArray(count);
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
    public NSArray<T> faultsMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier) {
        NSArray<T> nsarray = primaryKeysMatchingQualifier(ec, eoqualifier, null);
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
    public NSArray<T> faultsMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier, NSArray<EOSortOrdering> sortOrderings) {
        NSArray<T> nsarray = primaryKeysMatchingQualifier(ec, eoqualifier, sortOrderings);
        return faultsFromRawRows(ec, nsarray);
    }

    /**
     * Fetches an array of faults for a given set of criteria.
     * @param ec editing context to use to fetch into
     * @param nsdictionary key value criteria to match against
     * @param sortOrderings array of sort orderings to order the faults
     * @return array of faults that match the given criteria
     */
    public NSArray<T> faultsMatchingValues(EOEditingContext ec, NSDictionary nsdictionary, NSArray<EOSortOrdering> sortOrderings) {
        NSArray nsarray = primaryKeysMatchingValues(ec, nsdictionary, sortOrderings);
        return faultsFromRawRows(ec, nsarray);
    }

    /**
     * Provides a hook to control how a clazz object is chosen from a given entity.
     */
    public static interface ClazzFactory {
        public EOEnterpriseObjectClazz classFromEntity(EOEntity entity);

    }

    private static ClazzFactory _factory = new DefaultClazzFactory();

    public static ClazzFactory factory() { return _factory; }
    public static void setFactory(ClazzFactory value) { _factory = value; }

    /**
     * Default factory implementation.
     * @author ak
     *
     */
    public static class DefaultClazzFactory implements ClazzFactory {

    	protected boolean classNameIsGenericRecord(final String className) {
            return className.equals("ERXGenericRecord");
        }

    	protected EOEnterpriseObjectClazz newInstanceOfDefaultClazz() {
            return new EOEnterpriseObjectClazz();
        }

    	protected EOEnterpriseObjectClazz newInstanceOfGenericRecordClazz() {
            return new ERXGenericRecord.ERXGenericRecordClazz();
        }
    	
    	protected String clazzNameForEntity(EOEntity entity) {
    		return entity.className() + "$" + entity.name() + "Clazz";
    	}

        /**
         * Creates a clazz object for a given entity.
         * Will look for a clazz object with the name:
         * &lt;entity name&gt;$&lt;entity name&gt;Clazz.
         * @param entity to generate the clazz for
         * @return clazz object for the given entity
         */
        public EOEnterpriseObjectClazz classFromEntity(EOEntity entity) {
            EOEnterpriseObjectClazz clazz = null;
            if(entity == null) {
                clazz = newInstanceOfDefaultClazz();
            } else {
                try {
                    String className = entity.className();
                    if(classNameIsGenericRecord(className)) {
                        clazz = newInstanceOfGenericRecordClazz();
                    } else {
                    	String clazzName = clazzNameForEntity(entity);
                        clazz = (EOEnterpriseObjectClazz)Class.forName(clazzName).newInstance();
                    }
                } catch (InstantiationException ex) {
                } catch (ClassNotFoundException ex) {
                } catch (IllegalAccessException ex) {
                }
            }
            if(clazz == null) return classFromEntity(entity.parentEntity());
            return clazz;
        }
    }
}
