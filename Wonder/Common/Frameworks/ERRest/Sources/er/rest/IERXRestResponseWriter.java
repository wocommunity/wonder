package er.rest;

import java.text.ParseException;

import com.webobjects.appserver.WOResponse;

/**
 * IERXRestResponseWriter provides the interface for generating the output of a restful request.
 * 
 * @author mschrag
 */
public interface IERXRestResponseWriter {
	/**
	 * Called at the end of a request to produce the output to the user.
   * 
	 * @param context the rest context
	 * @param response the response to write into
	 * @param result the result of the rest request
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if there is a security violation
	 * @throws ERXRestNotFoundException if there is a missing entity
	 * @throws ParseException if there is a parse error
	 */
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;
}
