package er.extensions.rest;

import java.text.ParseException;

import com.webobjects.appserver.WOResponse;

public interface IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestResult result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;
}
