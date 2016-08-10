package er.neo4jadaptor.ersatz.webobjects;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSKeyValueCoding;

import er.neo4jadaptor.ersatz.Translator;

/**
 * Encodes <code>null</code> as {@link NSKeyValueCoding#NullValue}, while all other values remain unchanged.
 * 
 * @author Jedrzej Sobanski
 */
public class NSTranslator implements Translator {
	public static final NSTranslator instance = new NSTranslator();
	
	private NSTranslator() {
		
	}

	public Object fromNeutralValue(Object value, EOAttribute att) {
		if (value != null) {
			return value;
		} else {
			return NSKeyValueCoding.NullValue;
		}
	}

	public Object toNeutralValue(Object value, EOAttribute att) {
		if (NSKeyValueCoding.NullValue.equals(value)) {
			return null;
		} else {
			return value;
		}
	}
}
