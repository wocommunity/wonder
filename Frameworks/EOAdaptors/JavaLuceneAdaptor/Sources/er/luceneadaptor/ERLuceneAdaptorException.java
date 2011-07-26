package er.luceneadaptor;

import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.foundation.NSDictionary;

public class ERLuceneAdaptorException extends EOGeneralAdaptorException {

	public ERLuceneAdaptorException(String message, Throwable throwable) {
		super(message, throwable == null ? NSDictionary.emptyDictionary() : new NSDictionary(throwable, "originalException"));

	}

	public ERLuceneAdaptorException(String message) {
		super(message);
	}

}
