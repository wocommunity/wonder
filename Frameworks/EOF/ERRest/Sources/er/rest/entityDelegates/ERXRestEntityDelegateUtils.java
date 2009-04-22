package er.rest.entityDelegates;

import java.text.ParseException;

import com.webobjects.foundation.NSArray;

import er.rest.ERXRestException;
import er.rest.format.ERXStringBufferRestResponse;
import er.rest.routes.model.IERXEntity;

/**
 * Miscellaneous rest-related utility methods.
 * 
 * @author mschrag
 */
public class ERXRestEntityDelegateUtils {
	// MS: Yes, this is wrong, but I'll fix it later ...
	public static IERXEntity getEntityNamed(ERXRestContext context, String name) {
		IERXEntity e = IERXEntity.Factory.entityNamed(context.editingContext(), name);
		if (e == null) {
			throw new RuntimeException("Could not find the entity named '" + name + "'");
		}
		return e;
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
			writer.appendToResponse(context, responseWriter, new ERXRestKey(context, IERXEntity.Factory.entityForObject(value), null, value));
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
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, IERXEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
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
	public static String toString(IERXRestResponseWriter writer, IERXEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, entity, values);
	}
}
