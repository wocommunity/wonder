package er.extensions.eoaccess.entityordering;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXModelGroup;


/**
 * Abstract class defining an ordering of EOEntities that also provides NSComparators to sort entities based on this ordering.
 *
 * <p>This is implemented by creating a dictionary of entity name to group. Group 1 is entities with no dependencies. Group 2 is entities with
 * dependencies on entities in group 1. Group 3 is entities with dependencies on entities in groups 1 and 2. etc.
 * The dependencies between entities are determined by the abstract <code>buildDependancyList(NSMutableArray)</code>.</p>
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
        generateOrder();
    }


    /**
     * Convenience constructor for implementing classes.  Uses EOModelGroup.defaultGroup().
     */
    public ERXEntityOrder() {
        this(EOModelGroup.defaultGroup());
    }


    /**
     * Returns dictionary of entity name to group. Group 1 is entities with no dependencies. Group 2 is entities with
     * dependencies on entities in group 1. Group 3 is entities with dependencies on entities in groups 1 and 2. etc
     *
     * @return the groupedEntities dictionary of entity name to group
     */
    public NSMutableDictionary groupedEntities() {
        return groupedEntities;
    }


    /**
     * Processes <code>allEntities()</code> to determine the dependencies between them.
     */
    protected abstract void buildDependancyList();


    /**
     * @return the key into an EOEntity's <code>userInfo()</code> dictionary where the dependency information
     * for this class will be stored
     */
    protected abstract String userInfoKey();


    /**
     * Processes all entities in all models and generates the <code>groupedEntities()</code> dictionary.
     */
    protected void generateOrder() {
        buildDependancyList();

        NSMutableArray entities = allEntities().mutableClone();
        int groupNum = 1;
        while (entities.count() > 0) {
            // Entities that are eligible for this group are not added to the master list
            // immediately to avoid dependencies within the group
            NSMutableDictionary groupDictionary = new NSMutableDictionary();

            Integer group = new Integer(groupNum++);
            logger.trace("Building group " + group);
            int index = 0;
            while (index < entities.count()) {
                EOEntity entity = (EOEntity) entities.objectAtIndex(index);
                logger.trace("Processing entity " + entity.name());

                // Ignore Prototypes
                if (ERXModelGroup.isPrototypeEntity(entity)) {
                    logger.trace("Ignoring prototype entity " + entity.name());
                    entities.removeObjectAtIndex(index);
                }
                else if (hasDependenciesForEntity(entity)) {
                    logger.trace("Adding entity " + entity.name() + " to group " + group);
                    groupDictionary.setObjectForKey(group, entity.name());
                    entities.removeObjectAtIndex(index);
                }
                else {
                    // This entity can't be processed yet, it will get added to a later group
                    index++;
                }
            }

            if (groupDictionary.count() == 0) {
                logger.error("Stopping, circular relationships found for " + entities.valueForKey("name"));
                throw new RuntimeException("Circular relationships found for " + entities.valueForKey("name"));
            }

            groupedEntities().addEntriesFromDictionary(groupDictionary);
        }
    }


    /**
     * Returns the list of the names of the entities that reference (depend on) this entity. This list
     * is populated by buildDependancyList(). If buildDependancyList() has not finished executing, the list returned by
     * this method may not be complete. The list is stored in the entity's userInfo dictionary. This is a convenience
     * for accessing <code>entity.userInfo().objectForKey(userInfoKey())</code>.
     *
     * @param entity
     *            EOEntity to return list of referencing entities for
     * @return list of names of entities previously recorded as referencing this entity
     */
    protected NSMutableSet entitiesDependentOn(EOEntity entity) {
        NSMutableSet referencingEntities = (NSMutableSet) entity.userInfo().objectForKey(userInfoKey());
        if (referencingEntities == null) {
            referencingEntities = new NSMutableSet();
            NSMutableDictionary userInfo = new NSMutableDictionary(entity.userInfo());
            userInfo.setObjectForKey(referencingEntities, userInfoKey());
            entity.setUserInfo(userInfo);
        }
        return referencingEntities;
    }


    /**
     * @param entity entity to check for dependencies
     * @return true if <code>groupedEntities()</code> has all the entities named in <code>entitiesWithConstraintsOn(entity)</code>.
     */
    protected boolean hasDependenciesForEntity(EOEntity entity) {
        for (Iterator nameIterator = entitiesDependentOn(entity).iterator(); nameIterator.hasNext();) {
            String entityName = (String) nameIterator.next();
            if (groupedEntities().objectForKey(entityName) == null) {
                return false;
            }
        }

        return true;
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