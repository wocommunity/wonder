package er.ajax;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * AjaxValue provides a method for serializing Objects into a Javascript-compatible format
 * with hinting via AjaxOption.Type constants.
 * 
 * @author mschrag
 */
public class AjaxValue {
	private AjaxOption.Type _type;
	private Object _value;

	public AjaxValue(Object value) {
		this(AjaxOption.DEFAULT, value);
	}

	public AjaxValue(AjaxOption.Type type, Object value) {
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
			else {
				_type = type;
			}
			_value = value;
		}
		else {
			_type = type;
			_value = value;
		}
	}

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
			String escapedValue = String.valueOf(_value);
			escapedValue = escapedValue.replaceAll("\\\\", "\\\\\\\\");
			escapedValue = escapedValue.replaceAll("'", "\\\\'");
			strValue = "'" + escapedValue + "'";
		}
		else if (type == AjaxOption.NUMBER) {
			strValue = _value.toString();
		}
		else if (type == AjaxOption.ARRAY) {
			if (_value instanceof NSArray) {
				NSArray arrayValue = (NSArray) _value;
				StringBuffer sb = new StringBuffer();
				sb.append("[");
				Enumeration objEnum = arrayValue.objectEnumerator();
				while (objEnum.hasMoreElements()) {
					Object o = objEnum.nextElement();
					sb.append(new AjaxValue(o).javascriptValue());
					if (objEnum.hasMoreElements()) {
						sb.append(",");
					}
				}
				sb.append("]");
				strValue = sb.toString();
			}
			else {
				strValue = _value.toString();
			}
		}
		else if (type == AjaxOption.DICTIONARY) {
			if (_value instanceof NSDictionary) {
				NSDictionary dictValue = (NSDictionary) _value;
				StringBuffer sb = new StringBuffer();
				sb.append("{");
				Enumeration keyEnum = dictValue.keyEnumerator();
				while (keyEnum.hasMoreElements()) {
					Object key = keyEnum.nextElement();
					Object value = dictValue.objectForKey(key);
					sb.append(new AjaxValue(key).javascriptValue());
					sb.append(":");
					sb.append(new AjaxValue(value).javascriptValue());
					if (keyEnum.hasMoreElements()) {
						sb.append(",");
					}
				}
				sb.append("}");
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
					strValue = "'" + arrayValue.objectAtIndex(0).toString() + "'";
				}
				else if (count > 0) {
					StringBuffer sb = new StringBuffer();
					sb.append("[");
					Enumeration objEnum = arrayValue.objectEnumerator();
					while (objEnum.hasMoreElements()) {
						Object o = objEnum.nextElement();
						sb.append(new AjaxValue(AjaxOption.STRING, o).javascriptValue());
						if (objEnum.hasMoreElements()) {
							sb.append(",");
						}
					}
					sb.append("]");
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
		else {
			strValue = _value.toString();
		}
		return strValue;
	}
}
