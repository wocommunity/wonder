package er.extensions.rest;

import java.text.ParseException;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;

public interface IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, EOEntity entity, Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;
}
