package er.neo4jadaptor.ersatz.lucene;

import com.webobjects.eoaccess.EOAttribute;

import er.neo4jadaptor.ersatz.Translator;

/**
 * Encodes any type of object into a String
 * 
 * @see StorableTypes
 * 
 * @author Jedrzej Sobanski
 */
public class LuceneTranslator implements Translator {
	public static interface Coder {
		public String encode(Object value);
		public Object decode(String encoded);
	}
	
	public static final LuceneTranslator instance = new LuceneTranslator();
	
	private static final String NULL_LITERAL = "";
	
	private LuceneTranslator() {
		
	}
	
	public String fromNeutralValue(Object value, EOAttribute att) {
		if (value == null) {
			return NULL_LITERAL;
		} else {
			Coder t = StorableTypes.getForAttribute(att);
			
			return t.encode(value);
		}
	}

	public Object toNeutralValue(Object value, EOAttribute att) {
		if (NULL_LITERAL.equals(value)) {
			return null;
		} else {
			Coder t = StorableTypes.getForAttribute(att);
			String s = (String) value;
			
			return t.decode(s);
		}
	}
}
