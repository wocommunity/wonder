package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;

import er.extensions.ERXStringUtilities;

/**
 * Utilities for REST processing.
 * 
 * @author mschrag
 */
public class ERXRestUtils {
	/**
	 * Returns whether or not the given key value is the primary key of
	 * an EO.
	 * 
	 * @param key the possible EO key
	 * @return true if key is a primary key
	 */
	public static boolean isEOID(String key) {
		return key != null && ERXStringUtilities.isDigitsOnly(key);
	}

	/**
	 * Returns the EOGlobalID that represents the given key.
	 * 
	 * @param entity the entity of the key
	 * @param key the key as a string
	 * @return the globalID
	 */
	public static EOGlobalID gidForID(EOEntity entity, String key) {
		return EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { Integer.valueOf(key) });
	}
	
	/**
	 * Returns the string form of the primary key of the given EO.
	 * 
	 * @param eo the EO to get a primary key for
	 * @return the primary key
	 */
	public static String idForEO(EOEnterpriseObject eo) {
		EOKeyGlobalID gid = (EOKeyGlobalID) eo.editingContext().globalIDForObject(eo);
		Object id = gid.keyValues()[0];
		return String.valueOf(id);
	}
}
