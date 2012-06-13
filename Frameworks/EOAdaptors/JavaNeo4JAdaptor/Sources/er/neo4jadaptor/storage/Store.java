package er.neo4jadaptor.storage;


import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Interface for creating, updating, deleting and querying for records of one entity, 
 * using ersatz objects for communication. 
 * 
 * @author Jedrzej Sobanski
 *
 * @param <PassedType> type for description object on inserts
 * @param <StoredType> type for stored ersatzs
 */
public interface Store <PassedType extends Ersatz, StoredType extends Ersatz> {
	/**
	 * Creates a new record in the store.
	 * 
	 * @param toInsert inserted object description
	 * @return saved object corresponding to <code>toInsert</code>
	 */
	public StoredType insert(PassedType toInsert);
	
	/**
	 * Update record with the given values.
	 * 
	 * @param newValues new values, can be partial ersatz
	 * @param toUpdate ersatz representing record to be updated
	 */
	public void update(Ersatz newValues, StoredType toUpdate);
	
	/**
	 * Delete record from the store.
	 * 
	 * @param toDelete ersatz representing record to be deleted
	 */
	public void delete(StoredType toDelete);
	
	/**
	 * Generate new primary key.
	 * 
	 * @return primary key partial ersatz
	 */
	public Ersatz newPrimaryKey();
	
	/**
	 * Query for records matching EO qualifier.
	 * 
	 * @param q qualifier
	 * @return query results
	 */
	public Cursor<StoredType> query(EOQualifier q);
}
