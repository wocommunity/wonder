package er.extensions.eoaccess.entityordering;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;

import er.extensions.ERXArrayUtilities;



/**
 * <p>
 * Creates ordering based on foreign key dependencies.
 * </p>
 *
 * <p>
 * Group 1 is entities with no dependencies. Group 2 is entities with
 * dependencies on entities in group 1. Group 3 is entities with dependencies on
 * entities in groups 1 and 2. etc. The dependcies between entities are
 * determined by the abstract <code>buildDependancyList(NSMutableArray)</code>.
 * </p>
 *
 * @author Copyright (c) 2006-2006 Harte-Hanks Shoppers, Inc.
 */
public class ERXEntityFKConstraintOrder extends ERXEntityOrder
{

    private static Logger logger = Logger.getLogger(ERXEntityFKConstraintOrder.class);
    public static final String REFERENCING_ENTITIES = "ERXEntityFKConstraintOrder.REFERENCING_ENTITIES";


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
     * Convenience constructor for implementing classes.  Uses EOModelGroup.defaultGroup().
     */
    public ERXEntityFKConstraintOrder() {
        super();
    }


    /**
     * Processes the list of entities, creating the entitiesReferencing() list based on foreign key constraints.
     */
    protected void buildDependancyList() {
        logger.debug("Building dependancy list");

        for (Enumeration entityEnum = allEntities().objectEnumerator(); entityEnum.hasMoreElements();) {
            EOEntity entity = (EOEntity) entityEnum.nextElement();
            logger.trace("Finding dependencies of " + entity.name());

            for (Enumeration relationshipEnum = entity.relationships().objectEnumerator(); relationshipEnum.hasMoreElements();) {
                EORelationship relationship = (EORelationship) relationshipEnum.nextElement();

                if (hasForeignKeyConstraint(relationship)) {
                    EOEntity destinationEntity = relationship.destinationEntity();
                    logger.trace("Recording dependency on " + destinationEntity.name());
                    entitiesDependentOn(destinationEntity).addObject(entity.name());
                }
                else {
                    logger.trace("Ignoring, is not FK relationship");
                }
            }
        }
        logger.debug("Finished building dependancy list");

        if (logger.isTraceEnabled()) {
            for (int i = 0; i < allEntities().count(); i++) {
                EOEntity entity = (EOEntity) allEntities().objectAtIndex(i);
                logger.trace("Entity " + entity.name() + " is referenced by " + entitiesDependentOn(entity));
            }
        }
    }


    /**
     * @param relationship EORelationship to test
     * @return <code>true</code> if relationship models a relation that will have a foreign key constraint in the database
     */
    protected boolean hasForeignKeyConstraint(EORelationship relationship) {
        logger.trace("Examining relationshp " + relationship.name());

        // These can't be accomodated by entity ordering, these require record within the entity
        if (relationship.entity().equals(relationship.destinationEntity())) {
            logger.trace("Ignoring: self reflexive relationship");
            return false;
        }

        if ( ! ERXArrayUtilities.arraysAreIdenticalSets(relationship.destinationAttributes(),
                                                        relationship.destinationEntity().primaryKeyAttributes()) ) {
            logger.trace("No FK constraint: found non-PK attributes in destination");
            return false;
        }
        // Primary key to primary key relationships are excluded.
        else if (ERXArrayUtilities.arraysAreIdenticalSets(relationship.sourceAttributes(),
                                                          relationship.entity().primaryKeyAttributes()) ) {
            logger.trace("No FK constraint: Is PK to PK");
            return false;
        }

        return true;
    }


    /**
     * @return REFERENCING_ENTITIES
     */
    protected String userInfoKey() {
        return REFERENCING_ENTITIES;
    }

}
