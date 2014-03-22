package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import er.extensions.foundation.ERXPropertyListSerialization;

/**
 * Simple Formatter relying on {@link ERXPropertyListSerialization} 
 * to format or parse Objects to and from JSON
 */

public class ERXJSONFormatter extends Format {

	private static final long serialVersionUID = 1L;
	private static ERXJSONFormatter _formatter;


	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		return buffer.append(applyFormat(obj));
	}


	@Override
	public Object parseObject(String string, ParsePosition pos) {
		int index = pos.getIndex();
		String substring = string.substring(index);
		String result;
		try {
			result = (String) parseObject(substring);
			pos.setIndex(string.length() + 1);
		}
		catch (java.text.ParseException e) {
			result = null;
		}
		return result;
	}

	
	/**
	 * Method used to retrieve the shared instance of the JSON formatter.
	 * 
	 * @return shared instance of the JSON formatter
	 */
	public static ERXJSONFormatter formatter() {
		if (_formatter == null)
			_formatter = new ERXJSONFormatter();
		return _formatter;
	}

	/**
	 * Converts a JSON string into an Object like NSDictionary, or NSArray.
	 * 
	 * @param inString
	 *            JSON string
	 * @return Object
	 * @see ERXPropertyListSerialization#propertyListFromJSONString(String)
	 */
	@Override
	public Object parseObject(String inString) throws java.text.ParseException {
		if (inString == null) {
			return null;
		}
		return ERXPropertyListSerialization.propertyListFromJSONString(inString);
	}

	/**
	 * Converts an Object into a JSON string
	 * 
	 * @param anObject
	 *            an Object
	 * @return String
	 * @see ERXPropertyListSerialization#jsonStringFromPropertyList(Object)
	 */
	public String applyFormat(Object anObject) {
		if (anObject == null) {
			return null;
		}
		return ERXPropertyListSerialization.jsonStringFromPropertyList(anObject);
	}

}
