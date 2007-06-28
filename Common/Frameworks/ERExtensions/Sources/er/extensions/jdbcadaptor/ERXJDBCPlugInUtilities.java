package er.extensions.jdbcadaptor;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;

import er.extensions.eoaccess.entityordering.ERXEntityOrder;


/**
 * Collection of utility methods useful when writing JDBCPlugIns.
 *
 * @author chill
 */
public class ERXJDBCPlugInUtilities {


    /**
     * @param entityGroups array of arrays like this: ( (entity1), (entity2), (entity3)... )
     * @return EOModelGroup containing the models that contain the entities in entityGroups
     */
    public static EOModelGroup modelGroupForEntityGroups(NSArray entityGroups) {
        NSArray anEntityGroup = (NSArray) entityGroups.lastObject();
        EOEntity anEntity = (EOEntity) anEntityGroup.lastObject();
        return anEntity.model().modelGroup();
    }


    /**
     * Comparator to sort EOEntity instances based on an ERXEntityOrder ordering.
     */
    public static class EntityGroupDeleteOrderComparator extends NSComparator {
        protected ERXEntityOrder eRXEntityOrder;

        public EntityGroupDeleteOrderComparator(ERXEntityOrder ordering) {
            super();
            eRXEntityOrder = ordering;
        }

        public int compare(Object object1, Object object2) throws NSComparator.ComparisonException {
            EOEntity entity1 = (EOEntity) ((NSArray)object1).objectAtIndex(0);
            EOEntity entity2 = (EOEntity) ((NSArray)object2).objectAtIndex(0);
            Number group1 = (Number) eRXEntityOrder.groupedEntities().objectForKey(entity1.name());
            Number group2 = (Number) eRXEntityOrder.groupedEntities().objectForKey(entity2.name());

            return NSComparator.AscendingNumberComparator.compare(group1, group2);
        }
    }

}
