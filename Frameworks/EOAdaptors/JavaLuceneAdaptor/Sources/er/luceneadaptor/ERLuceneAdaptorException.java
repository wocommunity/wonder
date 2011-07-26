package er.luceneadaptor;

import com.webobjects.eoaccess.EOGeneralAdaptorException;

public class ERLuceneAdaptorException extends EOGeneralAdaptorException {

	public ERLuceneAdaptorException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ERLuceneAdaptorException(String message) {
		super(message);
	}

}
