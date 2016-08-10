package er.neo4jadaptor.ersatz;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSDictionary;

import er.neo4jadaptor.ersatz.webobjects.NSDictionaryErsatz;


/**
 * <p>
 * Neutral form for communication with various forms of EO object storages. Single {@link Ersatz} instance represents one record.
 * It's a form of map, where keys are EO attributes, values are neutral java types (see package description) and if some key is 
 * not set then this value is not present.
 * </p>
 * 
 * <p>
 * It contains values only for EO attributes returned in {@link #attributes()} method. Values for all other attributes are assumed to be
 * empty.
 * </p>
 * 
 * @author Jedrzej Sobanski
 */
public abstract class Ersatz {
	/**
	 * Empty ersatz object with no attributes set.
	 */
	@SuppressWarnings("unchecked")
	public static final Ersatz EMPTY = NSDictionaryErsatz.fromDictionary(NSDictionary.EmptyDictionary);
	
	/**
	 * Gets neutral value for an EO attribute. It's not defined what implementations should do when
	 * given attribute that's not in {@link #attributes()}.
	 * 
	 * @param att EO attribute to retrieve value for
	 * @return neutral java value if attribute is known
	 */
	public abstract Object get(EOAttribute att);

	/**
	 * Sets neutral java value for an EO attribute.
	 * 
	 * @param att EO attribute to set the value for
	 * @param value library independent value
	 */
	public abstract void put(EOAttribute att, Object value);

	/**
	 * Clears value for an EO attribute.
	 * 
	 * @param att
	 */
	public abstract void remove(EOAttribute att);

	/**
	 * Gets all EO attributes that this ersatz object has values stored for.
	 * 
	 * @return EO attributes that it has values for
	 */
	public abstract Iterable<EOAttribute> attributes();
	
	/**
	 * Copy all EO attributes defined in source object to the destination
	 * 
	 * @param source object to copy from
	 * @param dest object to copy to
	 */
	public static void copy(Ersatz source, Ersatz dest) {
		for (EOAttribute att : source.attributes()) {
			Object val = source.get(att);
			
			dest.put(att, val);
		}
	}
	
	/**
	 * Delete record that this ersatz represents.
	 */
	public abstract void delete();
}
