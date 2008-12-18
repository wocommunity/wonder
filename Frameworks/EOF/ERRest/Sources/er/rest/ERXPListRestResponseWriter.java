package er.rest;

import java.text.ParseException;

import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Provides the output methods for generating PList responses to a REST request.
 * 
 * @author mschrag
 */
public class ERXPListRestResponseWriter implements IERXRestResponseWriter {
	private boolean _displayAllProperties;
	private boolean _displayAllToMany;

	public ERXPListRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXPListRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXPListRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		_displayAllProperties = displayAllProperties;
		_displayAllToMany = displayAllToMany;
	}

	public void appendToResponse(ERXRestContext context, IERXResponseWriter response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXDictionaryRestResponseWriter dictResponseWriter = new ERXDictionaryRestResponseWriter(_displayAllProperties, _displayAllToMany);
		dictResponseWriter.appendToResponse(context, response, result);
		Object obj = dictResponseWriter.root();
		response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(obj));
	}
}
