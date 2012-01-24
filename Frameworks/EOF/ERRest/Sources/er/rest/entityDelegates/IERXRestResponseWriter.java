package er.rest.entityDelegates;

import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.rest.ERXRestException;
import er.rest.format.IERXRestResponse;

/**
 * IERXRestResponseWriter provides the interface for generating the output of a restful request.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public interface IERXRestResponseWriter {
	public static final String REST_PREFIX = "ERXRest.";
	public static final String DETAILS_PREFIX = ".details";
	public static final String DETAILS_PROPERTIES_PREFIX = ".detailsProperties";

	/**
	 * Called at the end of a request to produce the output to the user.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response to write into
	 * @param result
	 *            the result of the rest request
	 * @throws ERXRestException
	 *             if there is a general failure
	 * @throws ERXRestSecurityException
	 *             if there is a security violation
	 * @throws ERXRestNotFoundException
	 *             if there is a missing entity
	 * @throws ParseException
	 *             if there is a parse error
	 */
	public void appendToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

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
	public String toString(Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

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
	public String toString(EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param editingContext the editingcontext to resolve the given entity name within
	 * @param entityName the entity name of the values of the array
	 * @param values the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEditingContext editingContext, String entityName, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;
}
