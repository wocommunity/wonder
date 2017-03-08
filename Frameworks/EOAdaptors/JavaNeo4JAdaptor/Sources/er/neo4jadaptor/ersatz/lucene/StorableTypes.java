package er.neo4jadaptor.ersatz.lucene;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.util.NumericUtils;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSTimestamp;

import er.neo4jadaptor.ersatz.lucene.LuceneTranslator.Coder;




/**
 * Encodes and decodes java types as {@link String} values with the intention to be used for Lucene.
 * <p>
 * Numbers are padded with zeros so that textual representation of a lower number would be alphanumerically
 * lower then textual representation of a higher number:
 * <pre>
 * STRING.encode(10).compareTo(STRING.encode(2)) &lt; 0
 * 
 * // which translates to...
 * "00000000000000000010".compareTo("00000000000000000002") &lt; 0
 * </pre>
 * <p>
 * <b>TODO: Floating point types are not correctly represented</b>
 * 
 * @author Jedrzej Sobanski
 */
public enum StorableTypes implements Coder {
	STRING(String.class, 1) {
		public Object decode(String encoded) {
			return encoded;
		}
		public String encode(Object value) {
			if (value == null) {
				return "";
			}
			return value.toString();
		}
	}, 
	INTEGER(Integer.class, 2) {
		public Object decode(String encoded) {
			return Integer.parseInt(encoded);
		}
		public String encode(Object value) {
			return LONG.encode(value);
		}
	}, 
	LONG(Long.class, 2) {			// XXX: the same typeId as for INTEGER
		public Object decode(String encoded) {
			return Long.parseLong(encoded);
		}
		public String encode(Object value) {
			return padded(NUMBER_LENGTH, '0', Long.toString(((Number) value).longValue()));
		}
	}, 
	FLOAT(Float.class, 4) {
		public Object decode(String encoded) {
			return Float.parseFloat(encoded);
		}
		public String encode(Object value) {
			int l = NumericUtils.floatToSortableInt(((Number) value).floatValue());
			
			return Integer.toString(l);
		}
	}, 
	DOUBLE(Double.class, 5) {
		public Object decode(String encoded) {
			return Double.parseDouble(encoded);
		}
		public String encode(Object value) {
			long l = NumericUtils.doubleToSortableLong(((Number) value).doubleValue());
			
			return Long.toString(l);
		}
	},
	BOOL(Boolean.class, 6) {
		public Object decode(String encoded) {
			return Boolean.parseBoolean(encoded);
		}
		public String encode(Object value) {
			return Boolean.toString((Boolean) value);
		}
	},
	TIMESTAMP(NSTimestamp.class, 7) {
		public Object decode(String encoded) {
			try {
				Date date = DATE_FORMAT.parse(encoded);
				
				return new NSTimestamp(date);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		public String encode(Object value) {
			return DATE_FORMAT.format(value);
		}
		
	}
	;

	/**
	 * How many digits (including padding zeros) should be used to store a number
	 */
	private static final int NUMBER_LENGTH = (int) Math.ceil(Math.log10(Long.MAX_VALUE));
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS");
	
	private static final String padded(int length, char prefixChar, String s) {
		StringBuilder b = new StringBuilder(length);
		
		for (int i=0; i<length - s.length(); i++) {
			b.append(prefixChar);
		}
		b.append(s);
		
		return b.toString();
	}
	
	/* default */ final int typeId;
	private final Class<?> type;
	
	private StorableTypes(Class<?> type, int typeId) {
		this.type = type;
		this.typeId = typeId;
	}

	private static Map<String, StorableTypes> map = new HashMap<>();
	
	static {
		for (StorableTypes type : values()) {
			map.put(type.type.getCanonicalName(), type);			
		}	
	}
	
	public static StorableTypes getForAttribute(EOAttribute att) {
		return map.get(att.valueTypeClassName());
	}
}
