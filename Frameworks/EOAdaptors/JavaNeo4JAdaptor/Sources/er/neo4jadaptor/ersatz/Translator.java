package er.neo4jadaptor.ersatz;

import com.webobjects.eoaccess.EOAttribute;

/**
 * Performs translation between library specific types and neutral types. Neutral type is the first choice pick 
 * for a class for java developer to represent some type. Additionally <code>null</code> represents value being
 * present, but empty. We could say that neutral type is framework agnostic type.
 * 
 * @author Jedrzej Sobanski
 */
public interface Translator {
	/**
	 * Converts library specific value representation to neutral type.
	 * 
	 * @param librarySpecific value representation object in some library
	 * @param att EO attribute that the given value represents
	 * @return corresponding neutral value
	 */
	public Object toNeutralValue(Object librarySpecific, EOAttribute att);
	
	/**
	 * Converts neutral value to library specific representation.
	 * 
	 * @param neutral neutral value
	 * @param att EO attribute that the given value represents
	 * @return corresponding library-specific value
	 */
	public Object fromNeutralValue(Object neutral, EOAttribute att);
}
