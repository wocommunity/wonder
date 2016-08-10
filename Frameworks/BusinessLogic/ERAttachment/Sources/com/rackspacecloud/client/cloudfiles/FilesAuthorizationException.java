/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles;

import org.apache.http.Header;
import org.apache.http.StatusLine;

public class FilesAuthorizationException extends FilesException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3142674319839157198L;

	/**
     * An exception generated when a client tries to do something they aren't authorized to do.  
     * 
     * @param message        The message
     * @param httpHeaders    The returned HTTP headers
     * @param httpStatusLine The HTTP Status lined returned
     */
    public FilesAuthorizationException(String message, Header [] httpHeaders, StatusLine httpStatusLine)
    {
    	super (message, httpHeaders, httpStatusLine);
    }

}
