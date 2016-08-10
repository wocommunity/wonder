/**
 * 
 */
package com.rackspacecloud.client.cloudfiles;

import org.apache.http.Header;
import org.apache.http.StatusLine;

/**
 * @author lvaughn
 *
 */
@SuppressWarnings("serial")
public class FilesContainerNotEmptyException extends FilesException {
	/**
	 * @param message
	 * @param httpHeaders
	 * @param httpStatusLine
	 */
	public FilesContainerNotEmptyException(String message,
			Header[] httpHeaders, StatusLine httpStatusLine) {
		super(message, httpHeaders, httpStatusLine);
	}
	
}
