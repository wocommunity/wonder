package com.webobjects.eoaccess;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

/**
 * Helper class to allow us to access protected stuff in EOAccess.
 * @author ak
 */
public class ERXEOAccessHelper {
	/*
	public static EOGlobalID _globalIDForRowIsFinal(EOEntity entity, NSDictionary dict, boolean flag) {
		return entity._globalIDForRowIsFinal(dict, flag);
	}
	public static boolean isMultiHop(EORelationship rel) {
		return rel.isMultiHop();
	}
	public static boolean foreignKeyInDestination(EORelationship rel) {
		return rel.foreignKeyInDestination();
	}
	public static EOQualifier auxiliaryQualifier(EORelationship rel) {
		return rel.auxiliaryQualifier();
	}
	public static boolean isToMany(EORelationship relationship) {
		return relationship.isToMany();
	}
	public static boolean isToManyToOne(EORelationship relationship) {
		return relationship.isToManyToOne();
	}
	public static EORelationship firstRelationship(EORelationship relationship) {
		return relationship.firstRelationship();
	}
	public static EORelationship lastRelationship(EORelationship relationship) {
		return relationship.lastRelationship();
	}
	public static NSDictionary primaryKeyForTargetRowFromSourceDBSnapshot(EORelationship lastrel, NSDictionary currentRow) {
		return lastrel.primaryKeyForTargetRowFromSourceDBSnapshot(currentRow);
	}
	public static EOQualifier _singleTableRestrictingQualifier(EOEntity entity) {
		return entity._singleTableRestrictingQualifier();
	}
*/
	/**
	 * Helper to fix prefetching fetch spec to abstract entities. when batch
	 * fetching across flattened relationships and targeting abstract entities,
	 * we end up with GIDs that contain the abstract, not the concrete. So the
	 * objects can't be instantiated. If we add the schema based qualifier to
	 * the relationship beforehand, everything works as expected.
	 * @author ak
	 * @param dbc database context
	 * @param fetchSpec original fetch specification
	 * @param relationshipName relationship name to abstract entity
	 * @param sourceObjects unused
	 * @param context unused
	 * @return adjusted fetch specification
	 */
	public static EOFetchSpecification adjustPrefetching(EODatabaseContext dbc, EOFetchSpecification fetchSpec, String relationshipName, NSArray sourceObjects, EOEditingContext context) {
		if (fetchSpec.prefetchingRelationshipKeyPaths().count() > 0) {
			EOEntity rootEntity = dbc.database().entityNamed(fetchSpec.entityName());
			EORelationship relationship = rootEntity.relationshipNamed(relationshipName);
			EOEntity destinationEntity = relationship.destinationEntity();
			if (relationship.isToManyToOne() && destinationEntity.isAbstractEntity() && fetchSpec.prefetchingRelationshipKeyPaths().containsObject(relationship.name())) {
				EOFetchSpecification newFetchSpec = (EOFetchSpecification) fetchSpec.clone();
				String inverseName = relationship.anyInverseRelationship().definition(); // from destination to intermediate
				EOQualifier singleTableRestrict = destinationEntity._singleTableRestrictingQualifier();
				EOQualifier migratedRestrict = EOQualifierSQLGeneration.Support._qualifierMigratedFromEntityRelationshipPath(singleTableRestrict, destinationEntity, inverseName);
				newFetchSpec.setQualifier(new EOAndQualifier(new NSArray(new Object[]{newFetchSpec.qualifier(),migratedRestrict})));
				fetchSpec = newFetchSpec;
			}
		}
		return fetchSpec;
	}
}
