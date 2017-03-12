package er.extensions.eof;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.foundation.ERXArrayUtilities;



/**
 * Creates ordering based on foreign key dependencies.
 *
 * @author chill
 */
public class ERXEntityFKConstraintOrder extends ERXEntityOrder
{
    private static final Logger log = LoggerFactory.getLogger(ERXEntityFKConstraintOrder.class);


    /**
     * Designated constructor for implementing classes.
     *
     * @param modelGroup EOModelGroup to get list of all entities from
     */
    public ERXEntityFKConstraintOrder(EOModelGroup modelGroup)
    {
        super(modelGroup);
    }


    /**
     * Convenience constructor for implementing classes.  Uses <code>EOModelGroup.defaultGroup()</code>.
     */
    public ERXEntityFKConstraintOrder() {
        super();
    }


    /**
     * Processes the list of entities, creating the ordering dictionary based on foreign key constraints.
     *
     * @return a dictionary keyed on dependencyKeyFor(EOEntity)
     */
    @Override
    protected NSDictionary dependenciesByEntity() {
        log.debug("Building dependency list");

        NSMutableDictionary dependencyList = new NSMutableDictionary(allEntities().count());
        for (Enumeration entityEnum = allEntities().objectEnumerator(); entityEnum.hasMoreElements();) {
            EOEntity entity = (EOEntity) entityEnum.nextElement();
            log.trace("Finding dependencies of {}", entity.name());

            for (Enumeration relationshipEnum = entity.relationships().objectEnumerator(); relationshipEnum.hasMoreElements();) {
                EORelationship relationship = (EORelationship) relationshipEnum.nextElement();

                if (hasForeignKeyConstraint(relationship)) {
                    EOEntity destinationEntity = relationship.destinationEntity();
                    log.trace("Recording dependency on {}", destinationEntity.name());
                    entitiesDependentOn(dependencyList, destinationEntity).addObject(entity.name());
                }
                else {
                    log.trace("Ignoring, is not FK relationship or vertical inheritance parent");
                }
            }
        }
        log.debug("Finished building dependency list");

        if (log.isTraceEnabled()) {
            for (int i = 0; i < allEntities().count(); i++) {
                EOEntity entity = allEntities().objectAtIndex(i);
                log.trace("Entity {} is referenced by {}", entity.name(), entitiesDependentOn(dependencyList, entity));
            }
        }

        return dependencyList;
    }


    /**
     * @param relationship EORelationship to test
     * @return <code>true</code> if relationship models a relation that will have a foreign key constraint in the database
     */
    protected boolean hasForeignKeyConstraint(EORelationship relationship) {
        log.trace("Examining relationshp {}", relationship.name());

        // Reflexive relationships (circular dependencies) can't be accommodated by entity ordering, 
        // these require ordering within the operations for an entity. Check the externalName() rather than
        // entity name so that it will handle relationships to a super or subclass in a Single-Table inheritance structure
		if (relationship.entity().externalName() != null &&
			relationship.entity().externalName().equals(relationship.destinationEntity().externalName())) {
            log.trace("Ignoring: reflexive relationship");
            return false;
        }

        if ( ! ERXArrayUtilities.arraysAreIdenticalSets(relationship.destinationAttributes(),
                                                        relationship.destinationEntity().primaryKeyAttributes()) ) {
            log.trace("No FK constraint: found non-PK attributes in destination");
            return false;
        }
        // Primary key to primary key relationships are excluded.
        else if (ERXArrayUtilities.arraysAreIdenticalSets(relationship.sourceAttributes(),
                                                          relationship.entity().primaryKeyAttributes()) ) {
            // PK - PK relationships for vertical inheritance (child to parent) also need to be considered in ordering
            if (relationship.destinationEntity().equals(relationship.entity().parentEntity())) {
                log.trace("Is vertical inheritance PK to PKconstraint");
                return true;
            }

            // Bug?  Do these need to be included?
            log.trace("No FK constraint: Is PK to PK");
            return false;
        }

        log.trace("Is FK constraint");
        return true;
    }


    /**
     * This implementation returns <code>entity.externalName()</code> as the dependcy is actually on tables not EOEntities
     * .
     * @param entity EOEntity to return key into dependency dictionary for
     *
     * @return key for <code>entity</code> into dependency dictionary returned by <code>dependenciesByEntity()</code>
     */
    @Override
    protected String dependencyKeyFor(EOEntity entity) {
        if (entity.externalName() == null) {
            return "Abstract Dummy Entity";
        }
        return entity.externalName();
    }


    /**
     * Returns the list of the names of the entities that reference (depend on)
     * this entity. This list is populated by <code>builddependencyList()</code>.
     * If <code>builddependencyList()</code> has not finished executing, the
     * list returned by this method may not be complete.
     *
     * @param dependencies
     *            list of dependencies being built by
     *            <code>builddependencyList()</code>
     * @param entity
     *            EOEntity to return list of referencing entities for
     * @return list of names of entities previously recorded as referencing this
     *         entity
     */
    protected NSMutableSet entitiesDependentOn(NSMutableDictionary dependencies, EOEntity entity) {
        NSMutableSet referencingEntities = (NSMutableSet) dependencies.objectForKey(dependencyKeyFor(entity));
        if (referencingEntities == null) {
            referencingEntities = new NSMutableSet();
            dependencies.setObjectForKey(referencingEntities, dependencyKeyFor(entity));
        }
        return referencingEntities;
    }

}
