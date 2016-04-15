package er.rest.format;

import java.io.UnsupportedEncodingException;

import com.webobjects.foundation.NSForwardException;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public abstract class ERXRestWriter implements IERXRestWriter {
	/** The HTTP header key for the content type. */
	protected static final String ContentTypeHeaderKey = "Content-Type";
	/** The default character encoding for the REST responses. */
	protected static String TheDefaultResponseEncoding = "UTF-8";
	protected String contentEncoding;

	public ERXRestWriter() {
		contentEncoding = defaultEncoding();
	}

	@Override
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		response.setHeader(contentTypeHeaderValue(), ContentTypeHeaderKey);
	}

	/**
	 * The default character encoding to use for REST responses. The default value for this is UTF-8.
	 *
	 * @return the default character encoding
	 */
	public static String defaultEncoding() {
		return TheDefaultResponseEncoding;
	}

	/**
	 * Lets you specify the default character encoding to be used for REST responses.
	 *
	 * @param encoding
	 *            the default character encoding
	 */
	public static void setDefaultEncoding(String encoding) {
		if (encoding != null && !encoding.equals(TheDefaultResponseEncoding)) {
			try {
				"test".getBytes(encoding);
			}
			catch (UnsupportedEncodingException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			TheDefaultResponseEncoding = encoding;
		}
	}

	/**
	 * The character encoding to use for this REST response.
	 * 
	 * @return the content's character encoding
	 */
	public String contentEncoding() {
		return contentEncoding;
	}

	/**
	 * Lets you specify the content's character encoding to be used for this REST response.
	 * 
	 * @param encoding
	 *            the content's character encoding
	 */
	public void setContentEncoding(String encoding) {
		if (encoding != null && !encoding.equals(contentEncoding)) {
			try {
				"test".getBytes(encoding);
			}
			catch (UnsupportedEncodingException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}

			contentEncoding = encoding;
		}
	}

	/**
	 * The corresponding HTTP header content type for this REST response.
	 *
	 * @return content type
	 */
	public abstract String contentType();

	/**
	 * The value to be used for the content type HTTP header.
	 * 
	 * @return content type header value
	 */
	protected String contentTypeHeaderValue() {
		StringBuilder sb = new StringBuilder();
		sb.append(contentType());
		sb.append("; charset=");
		sb.append(contentEncoding());
		return sb.toString();
	}
}
