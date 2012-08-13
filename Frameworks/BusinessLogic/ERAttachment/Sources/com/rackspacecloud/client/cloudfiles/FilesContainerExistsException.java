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
public class FilesContainerExistsException extends FilesException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7282149064519490145L;

	/**
	 * @param message
	 * @param httpHeaders
	 * @param httpStatusLine
	 */
	public FilesContainerExistsException(String message, Header[] httpHeaders,
			StatusLine httpStatusLine) {
		super(message, httpHeaders, httpStatusLine);
	}

}
