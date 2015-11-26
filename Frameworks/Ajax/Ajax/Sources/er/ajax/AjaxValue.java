package er.ajax;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * AjaxValue provides a method for serializing Objects into a JavaScript-compatible format
 * with hinting via AjaxOption.Type constants.
 * 
 * @author mschrag
 */
public class AjaxValue {
	private AjaxOption.Type _type;
	private Object _value;

	/**
	 * @param obj Object to convert to String and escape
	 * @return obj converted to a string and escaped for use as a quoted JS string
	 */
	public static String javaScriptEscaped(Object obj) {
		String escapedValue = String.valueOf(obj);
		escapedValue = escapedValue.replaceAll("\\\\", "\\\\\\\\");
		escapedValue = escapedValue.replaceAll("'", "\\\\'");

		// Handle line breaks
        escapedValue = escapedValue.replaceAll("\\r\\n", "\\\\n");
        escapedValue = escapedValue.replaceAll("\\n", "\\\\n");

		escapedValue = "'" + escapedValue + "'";
		return escapedValue;
	}
	
	/**
	 * Creates AjaxValue for value with the type guessed at.
	 * 
	 * @see #AjaxValue(er.ajax.AjaxOption.Type, Object)
	 *
	 * @param value the value to make into an AjaxValue
	 */
	public AjaxValue(Object value) {
		this(AjaxOption.DEFAULT, value);
	}

	/**
	 * Creates AjaxValue for value with the indicated type.  If type is AjaxOption.DEFAULT, then
	 * the actual type will be inferred if value is String, Number, Boolean, NSArray, NSDictionary, 
	 * or AjaxValue (if value is an AjaxValue then both type and value are taken from value).
	 * 
	 * @see #AjaxValue(Object)
	 * @see AjaxOption.Type
	 * 
	 * @param type one of AjaxOption.Type constants from AjaxOption
	 * @param value the value to make into an AjaxValue
	 */
	public AjaxValue(AjaxOption.Type type, Object value) {
		_type = type;
		_value = value;
		
		if (type == AjaxOption.DEFAULT) {
			if (value instanceof String) {
				_type = AjaxOption.STRING;
			}
			else if (value instanceof Number) {
				_type = AjaxOption.NUMBER;
			}
			else if (value instanceof Boolean) {
				_type = AjaxOption.BOOLEAN;
			}
			else if (value instanceof NSArray) {
				_type = AjaxOption.ARRAY;
			}
			else if (value instanceof NSDictionary) {
				_type = AjaxOption.DICTIONARY;
			}
			else if (value instanceof AjaxValue) {
				_type = ((AjaxValue)value)._type;
				_value = ((AjaxValue)value)._value;
			}
		}
	}

	/**
	 * @return a String representing this AjaxValue in a form suitable for use in JavaScript
	 */
	public String javascriptValue() {
		String strValue;

		AjaxOption.Type type = _type;

		if (type == AjaxOption.STRING_OR_ARRAY) {
			if (_value == null) {
				type = AjaxOption.STRING;
			}
			else if (_value instanceof NSArray) {
				type = AjaxOption.ARRAY;
			}
			else if (_value instanceof String) {
				strValue = (String) _value;
				if (strValue.startsWith("[")) {
					type = AjaxOption.ARRAY;
				}
				else {
					type = AjaxOption.STRING;
				}
			}
		}

		if (_value == null || _value == NSKeyValueCoding.NullValue) {
			strValue = null;
		}
		else if (type == AjaxOption.STRING) {
			strValue = javaScriptEscaped(_value);
		}
		else if (type == AjaxOption.NUMBER) {
			strValue = _value.toString();
		}
		else if (type == AjaxOption.ARRAY) {
			if (_value instanceof NSArray) {
				NSArray arrayValue = (NSArray) _value;
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				Enumeration objEnum = arrayValue.objectEnumerator();
				while (objEnum.hasMoreElements()) {
					Object o = objEnum.nextElement();
					sb.append(new AjaxValue(o).javascriptValue());
					if (objEnum.hasMoreElements()) {
						sb.append(',');
					}
				}
				sb.append(']');
				strValue = sb.toString();
			}
			else {
				strValue = _value.toString();
			}
		}
		else if (type == AjaxOption.DICTIONARY) {
			if (_value instanceof NSDictionary) {
				NSDictionary dictValue = (NSDictionary) _value;
				StringBuilder sb = new StringBuilder();
				sb.append('{');
				Enumeration keyEnum = dictValue.keyEnumerator();
				while (keyEnum.hasMoreElements()) {
					Object key = keyEnum.nextElement();
					Object value = dictValue.objectForKey(key);
					sb.append(new AjaxValue(key).javascriptValue());
					sb.append(':');
					sb.append(new AjaxValue(value).javascriptValue());
					if (keyEnum.hasMoreElements()) {
						sb.append(',');
					}
				}
				sb.append('}');
				strValue = sb.toString();
			}
			else {
				strValue = _value.toString();
			}
		}
		else if (type == AjaxOption.STRING_ARRAY) {
			if (_value instanceof NSArray) {
				NSArray arrayValue = (NSArray) _value;
				int count = arrayValue.count();
				if (count == 1) {
					strValue = new AjaxValue(AjaxOption.STRING, arrayValue.objectAtIndex(0)).javascriptValue();
				}
				else if (count > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					Enumeration objEnum = arrayValue.objectEnumerator();
					while (objEnum.hasMoreElements()) {
						Object o = objEnum.nextElement();
						sb.append(new AjaxValue(AjaxOption.STRING, o).javascriptValue());
						if (objEnum.hasMoreElements()) {
							sb.append(',');
						}
					}
					sb.append(']');
					strValue = sb.toString();
				}
				else {
					strValue = "[]";
				}
			}
			else {
				strValue = _value.toString();
			}
		}
		else if (type == AjaxOption.SCRIPT) {
			strValue = _value.toString();
		}
		else if (type == AjaxOption.FUNCTION) {
			strValue = "function() {" + _value.toString() + "}";
		}
		else if (type == AjaxOption.FUNCTION_1) {
			strValue = "function(v) {" + _value.toString() + "}";
		}
		else if (type == AjaxOption.FUNCTION_2) {
			strValue = "function(v1, v2) {" + _value.toString() + "}";
		}else {
			strValue = _value.toString();
		}
		return strValue;
	}
}
