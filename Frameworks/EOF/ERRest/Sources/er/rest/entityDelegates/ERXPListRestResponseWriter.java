package er.rest.entityDelegates;

import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestException;
import er.rest.format.IERXRestResponse;

/**
 * Provides the output methods for generating PList responses to a REST request.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXPListRestResponseWriter implements IERXRestResponseWriter {
	private ERXKeyFilter _filter;
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

	/**
	 * Constructs an ERXPListRestResponseWriter.
	 * 
	 * @param filter
	 *            the filter to apply to the written results
	 */
	public ERXPListRestResponseWriter(ERXKeyFilter filter) {
		_filter = filter;
	}

	public void appendToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXDictionaryRestResponseWriter dictResponseWriter;
		if (_filter == null) {
			dictResponseWriter = new ERXDictionaryRestResponseWriter(_displayAllProperties, _displayAllToMany);
		}
		else {
			dictResponseWriter = new ERXDictionaryRestResponseWriter(_filter);
		}
		dictResponseWriter.appendToResponse(context, response, result);
		Object obj = dictResponseWriter.root();
		response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(obj));
	}

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param value
	 *            the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, value);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, entity, values);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEditingContext editingContext, String entityName, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, ERXRestEntityDelegateUtils.requiredEntityNamed(editingContext, entityName), values);
	}
}
