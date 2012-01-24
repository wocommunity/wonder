package er.rest.entityDelegates;

import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.rest.ERXRestException;
import er.rest.format.ERXStringBufferRestResponse;

/**
 * Miscellaneous rest-related utility methods.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXRestEntityDelegateUtils {
	public static EOEntity requiredEntityNamed(EOEditingContext editingContext, String name) {
		EOEntity entity = EOUtilities.entityNamed(editingContext, name);
		if (entity == null) {
			throw new RuntimeException("Could not find the entity named '" + name + "'");
		}
		return entity;
	}
	
	public static EOEntity requiredEntityNamed(ERXRestContext context, String name) {
		return ERXRestEntityDelegateUtils.requiredEntityNamed(context.editingContext(), name);
	}

	/**
	 * Returns a String form of the given object using the given delegate.
	 * 
	 * @param context
	 *            the context to write within
	 * @param writer
	 *            the writer to write with
	 * @param value
	 *            the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXStringBufferRestResponse responseWriter = new ERXStringBufferRestResponse();
		if (value != null) {
			writer.appendToResponse(context, responseWriter, new ERXRestKey(context, ERXRestEntityDelegateUtils.requiredEntityNamed(context, ((EOEnterpriseObject)value).entityName()), null, value));
		}
		return responseWriter.toString();
	}

	/**
	 * Returns a String form of the given objects using the given delegate.
	 * 
	 * @param context
	 *            the context to write within
	 * @param writer
	 *            the writer to write with
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXStringBufferRestResponse responseWriter = new ERXStringBufferRestResponse();
		writer.appendToResponse(context, responseWriter, new ERXRestKey(context, entity, null, values));
		return responseWriter.toString();
	}

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param writer
	 *            the writer to write with
	 * @param value
	 *            the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(IERXRestResponseWriter writer, Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, value);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param writer
	 *            the writer to write with
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(IERXRestResponseWriter writer, EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, entity, values);
	}
}
