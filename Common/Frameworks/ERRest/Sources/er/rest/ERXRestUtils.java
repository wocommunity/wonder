package er.rest;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXStringUtilities;

/**
 * Utilities for REST processing.
 * 
 * @author mschrag
 */
public class ERXRestUtils {
	/**
	 * Returns whether or not the given key value is the primary key of
	 * an EO.  This is crazy -- It tries to guess if it's looking at
	 * a key or not.
	 * 
	 * @param key the possible EO key
	 * @return true if key is a primary key
	 */
	public static boolean isEOID(ERXRestKey restKey) {
		boolean isID = false;
		String key = restKey.key();
		if (key != null) {
			EOEntity entity = restKey.entity();
			if (ERXStringUtilities.isDigitsOnly(key)) {
				isID = true;
			}
			else if (restKey.previousKey() == null) {
				if (!restKey.isKeyAll()) {
					isID = true;
				}
			}
			else {
				if (restKey.previousKey().isKeyAll()) {
					isID = true;
				}
				else {
					try {
						Object previousValue = restKey.previousKey()._value(false);
						if (previousValue instanceof NSArray) {
							isID = true;
						}
					}
					catch (ERXRestException e) {
						throw new RuntimeException("Failed to check key '" + key + "'.", e);
					}
					catch (ERXRestSecurityException e) {
						throw new RuntimeException("Failed to check key '" + key + "'.", e);
					}
					catch (ERXRestNotFoundException e) {
						throw new RuntimeException("Failed to check key '" + key + "'.", e);
					}
				}
			}
		}
		return isID;
	}

	/**
	 * Returns the EOGlobalID that represents the given key.
	 * 
	 * @param entity the entity of the key
	 * @param key the key as a string
	 * @return the globalID
	 */
	public static EOGlobalID gidForID(EOEntity entity, String key) {
		EOGlobalID gid;
		if (ERXStringUtilities.isDigitsOnly(key)) {
			gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { Integer.valueOf(key) });
		}
		else {
			NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
			if (primaryKeyAttributes.count() > 1) {
				throw new IllegalArgumentException("Compound primary keys (" + entity + ") are not currently supported.");
			}
			EOAttribute primaryKeyAttribute = (EOAttribute) primaryKeyAttributes.objectAtIndex(0);
			String valueType = primaryKeyAttribute.valueType();
			gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { key });
		}
		return gid;
	}
	
	/**
	 * Returns the string form of the primary key of the given EO.
	 * 
	 * @param eo the EO to get a primary key for
	 * @return the primary key
	 */
	public static String stringIDForEO(EOEnterpriseObject eo) {
		Object id = ERXRestUtils.idForEO(eo);
		String idStr;
		if (id instanceof Object[]) {
			throw new IllegalArgumentException(eo.entityName() + " has a compound primary key, which is currently not supported.");
		}
		else {
			idStr = String.valueOf(id);
		}
		return idStr;
	}
	
	/**
	 * Returns the primary key of the given EO.
	 * 
	 * @param eo the EO to get a primary key for
	 * @return the primary key
	 */
	public static Object idForEO(EOEnterpriseObject eo) {
		EOGlobalID gid = eo.editingContext().globalIDForObject(eo);
		if (!(gid instanceof EOKeyGlobalID)) {
			throw new IllegalArgumentException("Unsupported primary key type '" + gid + "'.");
		}
		EOKeyGlobalID keyGID = (EOKeyGlobalID) gid;
		Object[] keyValues = keyGID.keyValues();
		if (keyValues.length > 1) {
			throw new IllegalArgumentException("Compound primary keys (" + eo.entityName() + ") are not currently supported.");
		}
		Object id;
		if (keyValues.length == 1) {
			id = keyValues[0];
		}
		else {
			id = keyValues;
		}
		return id;
	}
}
