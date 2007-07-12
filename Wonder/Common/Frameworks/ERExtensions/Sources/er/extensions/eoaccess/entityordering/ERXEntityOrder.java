package er.extensions.eoaccess.entityordering;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSet;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXModelGroup;


/**
 * Abstract class defining an ordering of EOEntities that also provides NSComparators to sort entities based on this ordering.
 * The ordering is based on groups of entities with entities within each group having no defined order.
 *
 * <p>This is implemented by creating a dictionary of entity name to group. Group 1 is entities with no dependencies. Group 2 is entities with
 * dependencies on entities in group 1. Group 3 is entities with dependencies on entities in groups 1 and 2. etc.
 * The dependencies between entities are determined by the abstract <code>NSDictionary dependenciesByEntity()</code>.</p>
 *
 * @author chill
 */
public abstract class ERXEntityOrder
{

    private static Logger logger = Logger.getLogger(ERXEntityOrder.class);
    protected NSMutableDictionary groupedEntities = new NSMutableDictionary();
    protected NSArray allEntities = null;


    /**
     * Designated constructor for implementing classes.
     *
     * @param modelGroup EOModelGroup to get list of all entities from
     */
    public ERXEntityOrder(EOModelGroup modelGroup) {
        super();
        createListOfEntities(modelGroup);
        generateOrdering();
    }


    /**
     * Convenience constructor for implementing classes.  Uses <code>EOModelGroup.defaultGroup()</code>.
     */
    public ERXEntityOrder() {
        this(EOModelGroup.defaultGroup());
    }


    /**
     * Returns dictionary of group numbers (<code>java.lang.Integer</code>) to entity names.
     * Group 1 is entities with no dependencies. Group 2 is entities with
     * dependencies on entities in group 1. Group 3 is entities with
     * dependencies on entities in groups 1 and 2. etc
     *
     * @return dictionary of group numbers to entity names
     */
    public NSMutableDictionary groupedEntities() {
        return groupedEntities;
    }


    /**
     * Processes <code>allEntities()</code> and returns a dictionary keyed on
     * <code>dependencyKeyFor(EOEntity)</code>. The keys are usually <code>entity.name()</code> but are
     * not required to be. The value associated with each key is an NSSet of the
     * entity names that have a dependency on the key. This dictionary is used
     * to determine the dependency ordering.
     *
     * @return a dictionary keyed on dependencyKeyFor(EOEntity)
     */
    protected abstract NSDictionary dependenciesByEntity();


    /**
     * Calls <code>dependenciesByEntity()</code> to determine dependencies and processes entities
     * in <code>allEntities()</code> to generate the <code>groupedEntities()</code> dictionary.
     */
    protected void generateOrdering() {

        NSDictionary dependencies = dependenciesByEntity();
        NSMutableArray entities = allEntities().mutableClone();
        int groupNum = 1;
        while (entities.count() > 0) {
            // Entities that are eligible for this group are NOT added to the master list
            // immediately to avoid dependencies between entities in the same group
            NSMutableDictionary groupDictionary = new NSMutableDictionary();

            Integer group = new Integer(groupNum++);
            logger.trace("Building group " + group);

            // Examine each entity not already in a group and add it to this group if
            // all of its dependencies are in previously processed groups.
            int index = 0;
            while (index < entities.count()) {
                EOEntity entity = (EOEntity) entities.objectAtIndex(index);
                logger.trace("Processing entity " + entity.name());

                if (hasDependenciesForEntity(dependencies, entity)) {
                    logger.trace("Adding entity " + entity.name() + " to group " + group);
                    groupDictionary.setObjectForKey(group, entity.name());
                    entities.removeObjectAtIndex(index);
                }
                else {
                    // This entity still has unresolved dependencies, it will get added to a later group
                    index++;
                }
            }

            // If an error is found, log out information to make debugging easier
            if (groupDictionary.count() == 0) {
                logger.error("Stopping, circular relationships found for " + entities.valueForKey("name"));
                NSSet remainingEntities = new NSSet((NSArray)entities.valueForKey("name"));
                for (int i = 0; i < entities.count(); i++) {
                    EOEntity entity = (EOEntity) entities.objectAtIndex(i);
                    NSSet remainingDependencies = dependentEntities(dependencies, entity).setByIntersectingSet(remainingEntities);
                    logger.error(entity.name() + " has dependencies on " + remainingDependencies);
                }
                throw new RuntimeException("Circular relationships found for " + entities.valueForKey("name"));
            }

            groupedEntities().addEntriesFromDictionary(groupDictionary);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Entity groups in dependency order:");
            for (int i = 1; i < groupNum; i++) {
                logger.trace("Group " + i + ": " + groupedEntities().allKeysForObject(new Integer(i)));
                logger.trace("");
            }
        }
     }


    /**
     * @param dependencies dictionary from <code>dependenciesByEntity()</code>
     * @param entity entity to check for dependencies
     *
     * @return true if <code>groupedEntities()</code> has all the entities named in <code>dependentEntities(dependencies, entity)</code>.
     */
    protected boolean hasDependenciesForEntity(NSDictionary dependencies, EOEntity entity) {
        // Abstract entities etc may not have an entry
        if (dependentEntities(dependencies, entity) == null)
        {
            return true;
        }

        for (Iterator nameIterator = dependentEntities(dependencies, entity).iterator(); nameIterator.hasNext();) {
            String entityName = (String) nameIterator.next();
            if (groupedEntities().objectForKey(entityName) == null) {
                return false;
            }
        }

        return true;
    }


    /**
     * @param dependencies result from <code>dependenciesByEntity()</code>
     * @param entity EOEntity to return dependencies set for
     *
     * @return set of names of entities that are dependent on entity
     */
    protected NSSet dependentEntities(NSDictionary dependencies, EOEntity entity) {
        return (NSSet) dependencies.objectForKey(dependencyKeyFor(entity));
    }


    /**
     * This implementation returns <code>entity.name()</code>.
     * @param entity EOEntity to return key into dependency dictionary for
     *
     * @return key for <code>entity</code> into dependency dictionary returned by <code>dependenciesByEntity()</code>
     */
    protected String dependencyKeyFor(EOEntity entity) {
        return entity.name();
    }


    /**
     * Creates list of all entities (ecluding prototype entities) in all models in <code>modelGroup</code>.
     *
     * @param modelGroup EOModelGroup to get list of all entities from
     */
    public void createListOfEntities(EOModelGroup modelGroup) {
        NSArray arrayOfArrayOfEntites = (NSArray) modelGroup.models().valueForKey("entities");
        NSArray entities = ERXArrayUtilities.flatten(arrayOfArrayOfEntites);
        NSMutableArray filteredEntities = new NSMutableArray(entities.count());

        for (Enumeration entityEnum = entities.objectEnumerator(); entityEnum.hasMoreElements();) {
            EOEntity entity = (EOEntity) entityEnum.nextElement();
            if ( ! ERXModelGroup.isPrototypeEntity(entity)) {
                filteredEntities.addObject(entity);
            }
        }
        allEntities = filteredEntities.immutableClone();
    }


    /**
     * @return list of all entities in all models in the model group
     */
    public NSArray allEntities() {
        return allEntities;
    }


    /**
     * NSComparator to sort on the ascending EOEntity group number from ordering.entityOrdering().
     * This produces an ordering suitable for deleting data.
     */
    public static class EntityDeleteOrderComparator extends NSComparator {
        protected ERXEntityOrder eRXEntityOrder;

        public EntityDeleteOrderComparator(ERXEntityOrder ordering) {
            super();
            eRXEntityOrder = ordering;
        }

        public int compare(Object object1, Object object2) throws NSComparator.ComparisonException {
            EOEntity entity1 = (EOEntity) object1;
            EOEntity entity2 = (EOEntity) object2;
            Number group1 = (Number) eRXEntityOrder.groupedEntities().objectForKey(entity1.name());
            Number group2 = (Number) eRXEntityOrder.groupedEntities().objectForKey(entity2.name());

            return NSComparator.AscendingNumberComparator.compare(group1, group2);
        }
    }


    /**
     * NSComparator to sort on the descending EOEntity group number from ordering.entityOrdering().
     * This produces an ordering suitable for inserting data.
     */
    public static class EntityInsertOrderComparator extends NSComparator {
        protected ERXEntityOrder eRXEntityOrder;

        public EntityInsertOrderComparator(ERXEntityOrder ordering) {
            super();
            eRXEntityOrder = ordering;
        }

        public int compare(Object object1, Object object2) throws NSComparator.ComparisonException {
            EOEntity entity1 = (EOEntity) object1;
            EOEntity entity2 = (EOEntity) object2;
            Number group1 = (Number) eRXEntityOrder.groupedEntities().objectForKey(entity1.name());
            Number group2 = (Number) eRXEntityOrder.groupedEntities().objectForKey(entity2.name());

            return NSComparator.DescendingNumberComparator.compare(group1, group2);
        }
    }


}