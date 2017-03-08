package er.extensions.eof;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSet;

import er.extensions.foundation.ERXArrayUtilities;


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
    private static final Logger log = LoggerFactory.getLogger(ERXEntityOrder.class);
    protected NSMutableDictionary<String, Integer> groupedEntities = new NSMutableDictionary<>();
    protected NSArray<EOEntity> allEntities = null;


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
    public NSMutableDictionary<String, Integer> groupedEntities() {
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
    protected abstract NSDictionary<String, NSSet<String>> dependenciesByEntity();


    /**
     * Calls <code>dependenciesByEntity()</code> to determine dependencies and processes entities
     * in <code>allEntities()</code> to generate the <code>groupedEntities()</code> dictionary.
     */
    protected void generateOrdering() {

        NSDictionary<String, NSSet<String>> dependencies = dependenciesByEntity();
        NSMutableArray<EOEntity> entities = allEntities().mutableClone();
        int groupNum = 1;
        while (entities.count() > 0) {
            // Entities that are eligible for this group are NOT added to the master list
            // immediately to avoid dependencies between entities in the same group
            NSMutableDictionary<String,Integer> groupDictionary = new NSMutableDictionary<>();

            Integer group = Integer.valueOf(groupNum++);
            log.trace("Building group {}", group);

            // Examine each entity not already in a group and add it to this group if
            // all of its dependencies are in previously processed groups.
            int index = 0;
            while (index < entities.count()) {
                EOEntity entity = entities.objectAtIndex(index);
                log.trace("Processing entity {}", entity.name());

                if (hasDependenciesForEntity(dependencies, entity)) {
                    log.trace("Adding entity {} to group {}", entity.name(), group);
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
                log.error("Stopping, circular relationships found for {}", entities.valueForKey("name"));
                NSSet<String> remainingEntities = new NSSet<>((NSArray<String>)entities.valueForKey("name"));
                for (int i = 0; i < entities.count(); i++) {
                    EOEntity entity = entities.objectAtIndex(i);
                    NSSet<String> remainingDependencies = dependentEntities(dependencies, entity).setByIntersectingSet(remainingEntities);
                    log.error("{} has dependencies on {}", entity.name(), remainingDependencies);
                }
                throw new RuntimeException("Circular relationships found for " + entities.valueForKey("name"));
            }

            groupedEntities().addEntriesFromDictionary(groupDictionary);
        }

        if (log.isTraceEnabled()) {
            log.trace("Entity groups in dependency order:");
            for (int i = 1; i < groupNum; i++) {
                Integer iVal = Integer.valueOf(i);
                log.trace("Group {}: {}", iVal, groupedEntities().allKeysForObject(iVal));
                log.trace("");
            }
        }
     }


    /**
     * @param dependencies dictionary from <code>dependenciesByEntity()</code>
     * @param entity entity to check for dependencies
     *
     * @return true if <code>groupedEntities()</code> has all the entities named in <code>dependentEntities(dependencies, entity)</code>.
     */
    protected boolean hasDependenciesForEntity(NSDictionary<String, NSSet<String>> dependencies, EOEntity entity) {
        // Abstract entities etc may not have an entry
        if (dependentEntities(dependencies, entity) == null)
        {
            return true;
        }

        for (String entityName : dependentEntities(dependencies, entity)) {
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
    protected NSSet<String> dependentEntities(NSDictionary<String, NSSet<String>> dependencies, EOEntity entity) {
        return dependencies.objectForKey(dependencyKeyFor(entity));
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
     * Creates list of all entities (excluding prototype entities) in all models in <code>modelGroup</code>.
     *
     * @param modelGroup EOModelGroup to get list of all entities from
     */
    public void createListOfEntities(EOModelGroup modelGroup) {
        NSArray<NSArray<EOEntity>> arrayOfArrayOfEntites = (NSArray<NSArray<EOEntity>>) modelGroup.models().valueForKey("entities");
        NSArray<EOEntity> entities = ERXArrayUtilities.flatten(arrayOfArrayOfEntites);
        NSMutableArray<EOEntity> filteredEntities = new NSMutableArray(entities.count());

        for (EOEntity entity : entities) {
            if ( ! ERXModelGroup.isPrototypeEntity(entity)) {
                filteredEntities.addObject(entity);
            }
        }
        allEntities = filteredEntities.immutableClone();
    }


    /**
     * @return list of all entities in all models in the model group
     */
    public NSArray<EOEntity> allEntities() {
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

        @Override
        public int compare(Object object1, Object object2) throws NSComparator.ComparisonException {
            EOEntity entity1 = (EOEntity) object1;
            EOEntity entity2 = (EOEntity) object2;
            Number group1 = eRXEntityOrder.groupedEntities().objectForKey(entity1.name());
            Number group2 = eRXEntityOrder.groupedEntities().objectForKey(entity2.name());

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

        @Override
        public int compare(Object object1, Object object2) throws NSComparator.ComparisonException {
            EOEntity entity1 = (EOEntity) object1;
            EOEntity entity2 = (EOEntity) object2;
            Number group1 = eRXEntityOrder.groupedEntities().objectForKey(entity1.name());
            Number group2 = eRXEntityOrder.groupedEntities().objectForKey(entity2.name());

            return NSComparator.DescendingNumberComparator.compare(group1, group2);
        }
    }


}