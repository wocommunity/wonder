package er.neo4jadaptor.ersatz.neo4j;

import java.util.Date;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSTimestamp;

import er.neo4jadaptor.ersatz.Translator;
import er.neo4jadaptor.storage.neo4j.NodeStore;

/**
 * Encodes {@link java.util.Date} types as {@link java.lang.Long} values being number of milliseconds in the original date.
 * 
 * @author Jedrzej Sobanski
 */
public class Neo4JTranslator implements Translator {

	public static final Neo4JTranslator instance = new Neo4JTranslator();
	
	private Neo4JTranslator() {
		
	}
	
	public Object fromNeutralValue(Object value, EOAttribute att) {
		if (value instanceof Date) {
			Date date = (Date) value;

			return date.getTime();
		} else if (value instanceof NodeStore.NodeNumber) {
			return ((Number) value).longValue();
		} else {
			return value;
		}
	}
	
	
	private static final String NSTIMESTAMP_CANONICAL_CLASS_NAME = NSTimestamp.class.getCanonicalName();
	private static final String INTEGER_CANONICAL_CLASS_NAME = Integer.class.getCanonicalName();
	
	public Object toNeutralValue(Object value, EOAttribute att) {
		if (value == null) {
			return null;
		}
		if (att != null) {
			final String typeClassName = att.valueTypeClassName();
			
			if (NSTIMESTAMP_CANONICAL_CLASS_NAME.equals(typeClassName)) {
				long l = (Long) value;
				
				return new NSTimestamp(l);
			} else if (INTEGER_CANONICAL_CLASS_NAME.equals(typeClassName)) {
				return ((Number) value).intValue();
			}
		}
		
		return value;
	}

}
