package er.extensions.appserver;

import com.webobjects.appserver.WOMessage;

/**
 * Bunch of static values for the HTTP status code that are not in WOMessage
 * 
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">W3C HTTP/1.1 status code definitions</a>
 * @author probert
 *
 */
public class ERXHttpStatusCodes {
	
	/** 
	 * 200 OK 
	 * The request has succeeded.
	 * This is the most common status code.
	 */
	public static final int OK = WOMessage.HTTP_STATUS_OK;
	
	/** 
	 * 201 Created 
	 * The request has been fulfilled and resulted in a new resource being created.
	 * Use this status code when responding to a POST request that created a resource.
	 */
	public static final int CREATED = 201;
	
	/** 
	 * 202 Accepted 
	 * The request has been accepted for processing, but the processing has not been completed.
	 * Use this status code when accepting a request that will create a long running task
	 */
	public static final int ACCEPTED = 202;
	
	/** 203 Non-Authoritative Information */
	public static final int NON_AUTHORITATIVE_INFORMATION = 203;
	
	/** 
	 * 204 No Content 
	 * The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation.
	 * The 204 response MUST NOT include a message-body, and thus is always terminated by the first empty line after the header fields.
	 */
	public static final int NO_CONTENT = WOMessage.HTTP_STATUS_NO_CONTENT;
	
	/** 
	 * 205 Reset Content 
	 */
	public static final int RESET_CONTENT = 205;
	
	/** 206 Partial Content */
	public static final int PARTIAL_CONTENT = 206;
	
	/** 300 Multiple Choices */
	public static final int MULTIPLE_CHOICES = 300;
	
	/** 
	 * 301 Moved Permanently 
	 * The requested resource has been assigned a new permanent URI and any future references to this resource SHOULD use one of the returned URIs. 
	 * The new permanent URI SHOULD be given by the Location field in the response.
	 */
	public static final int MOVED_PERMANENTLY = WOMessage.HTTP_STATUS_MOVED_PERMANENTLY;
	
	/** 
	 * 302 Found 
	 * The requested resource resides temporarily under a different URI. Since the redirection might be altered on occasion, the client SHOULD continue to use the Request-URI for future requests.
	 * The temporary URI SHOULD be given by the Location field in the response. 
	 */
	public static final int FOUND = WOMessage.HTTP_STATUS_FOUND;
	
	/** 
	 * 303 See Other
	 * The response to the request can be found under a different URI and SHOULD be retrieved using a GET method on that resource. 
	 * The different URI SHOULD be given by the Location field in the response. 
	 */
	public static final int SEE_OTHER = 303;
	
	/** 
	 * 304 Not Modified 
	 * If the client has performed a conditional GET request and access is allowed, but the document has not been modified, the server SHOULD respond with this status code.
	 */
	public static final int NOT_MODIFIED = 304;
	
	/** 305 Use Proxy */
	public static final int USE_PROXY = 305;
	
	/** 307 Temporary Redirect */
	public static final int TEMPORARY_REDIRECT = 307;
	
	/** 
	 * 400 Bad Request 
	 * The request could not be understood by the server due to malformed syntax. The client SHOULD NOT repeat the request without modifications.
	 */
	public static final int BAD_REQUEST = 400;
	
	/** 
	 * 401 Unauthorized 
	 * The request requires user authentication. The response MUST include a WWW-Authenticate header field (section 14.47) containing a challenge applicable to the requested resource.
	 */
	public static final int UNAUTHORIZED = 401;
	
	/** 
	 * 403 Forbidden 
	 * The server understood the request, but is refusing to fulfill it. Authorization will not help and the request SHOULD NOT be repeated.
	 */
	public static final int FORBIDDEN = WOMessage.HTTP_STATUS_FORBIDDEN;

	/** 
	 * 404 Not Found 
	 * The server has not found anything matching the Request-URI.
	 */
	public static final int NOT_FOUND = WOMessage.HTTP_STATUS_NOT_FOUND;
	
	/** 
	 * 405 Method Not Allowed 
	 * The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. 
	 * The response MUST include an Allow header containing a list of valid methods for the requested resource.
	 */
	public static final int METHOD_NOT_ALLOWED = 405;
	
	/** 406 Not Acceptable */
	public static final int NOT_ACCEPTABLE = 406;
	
	/** 407 Proxy Authentication Required */
	public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
	
	/** 408 Request Timeout */
	public static final int REQUEST_TIMEOUT = 408;
	
	/** 
	 * 409 Conflict 
	 * The request could not be completed due to a conflict with the current state of the resource. 
	 * This code is only allowed in situations where it is expected that the user might be able to resolve the conflict and resubmit the request.
	 */
	public static final int CONFLICT = 409;
	
	/** 
	 * 410 Gone 
	 * The requested resource is no longer available at the server and no forwarding address is known. 
	 * This condition is expected to be considered permanent.
	 */
	public static final int GONE = 410;
	
	/** 411 Length Required */
	public static final int LENGTH_REQUIRED = 411;
	
	/** 
	 * 412 Precondition Failed
	 * The precondition given in one or more of the request-header fields evaluated to false when it was tested on the server. 
	 */
	public static final int PRECONDITION_FAILED = 412;
	
	/** 413 Request Entity Too Large */
	public static final int REQUEST_ENTITY_TOO_LARGE = 413;
	
	/** 414 Request-URI Too Long */
	public static final int REQUEST_URI_TOO_LONG = 414;
	
	/** 
	 * 415 Unsupported Media Type 
	 * The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method.
	 */
	public static final int UNSUPPORTED_MEDIA_TYPE = 415;
	
	/** 416 Requested Range Not Satisfiable */
	public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	
	/** 417 Requested Range Not Satisfiable */
	public static final int EXPECTATION_FAILED = 417;

	/** 500 Internal Server Error */
	public static final int INTERNAL_ERROR = WOMessage.HTTP_STATUS_INTERNAL_ERROR;
	
	/** 
	 * 501 Not Implemented 
	 * The server does not support the functionality required to fulfill the request. 
	 * This is the appropriate response when the server does not recognize the request method and is not capable of supporting it for any resource.
	 */
	public static final int NOT_IMPLEMENTED = 501;
	
	/** 502 Bad Gateway */
	public static final int BAD_GATEWAY = 502;
	
	/** 
	 * 503 Service Unavailable 
	 * The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.
	 * The implication is that this is a temporary condition which will be alleviated after some delay. 
	 */
	public static final int SERVICE_UNAVAILABLE = 503;
	
	/** 504 Gateway Timeout */
	public static final int GATEWAY_TIMEOUT = 504;
}
