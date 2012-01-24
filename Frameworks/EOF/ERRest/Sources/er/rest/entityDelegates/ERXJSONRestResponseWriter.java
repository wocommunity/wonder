package er.rest.entityDelegates;

import java.text.ParseException;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestException;
import er.rest.format.IERXRestResponse;

/**
 * Provides the output methods for generating JSON responses to a REST request.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXJSONRestResponseWriter implements IERXRestResponseWriter {
	private ERXKeyFilter _filter;
	private boolean _displayAllProperties;
	private boolean _displayAllToMany;

	public ERXJSONRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXJSONRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXJSONRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		_displayAllProperties = displayAllProperties;
		_displayAllToMany = displayAllToMany;
	}

	/**
	 * Constructs an ERXJSONRestResponseWriter.
	 * 
	 * @param filter
	 *            the filter to apply to the written results
	 */
	public ERXJSONRestResponseWriter(ERXKeyFilter filter) {
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
		if (obj instanceof List) {
			JSONArray json = JSONArray.fromObject(obj);
			response.appendContentString(json.toString());
		}
		else {
			JSONObject json = JSONObject.fromObject(obj);
			response.appendContentString(json.toString());
		}
	}

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param value the value to write
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
	 * @param values the values to write
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
	 * @param values the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEditingContext editingContext, String entityName, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, EOUtilities.entityNamed(editingContext, entityName), values);
	}
}
