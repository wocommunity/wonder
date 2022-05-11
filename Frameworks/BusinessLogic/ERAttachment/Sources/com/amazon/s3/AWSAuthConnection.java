//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

// Copyright (c) 2006 SilvaSoft, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.

// author:    http://www.silvasoftinc.com
// author:    Dominic Da Silva (dominic.dasilva@gmail.com)
// version:   1.1
// date:      04/09/2006

package com.amazon.s3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.silvasoftinc.s3.S3StreamObject;

/**
 * An interface into the S3 system. It is initially configured with
 * authentication and connection parameters and exposes methods to access and
 * manipulate S3 data.
 */
public class AWSAuthConnection {

	private String awsAccessKeyId;

	private String awsSecretAccessKey;

	private boolean isSecure;

	private String server;

	private int port;

	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey) {
		this(awsAccessKeyId, awsSecretAccessKey, true);
	}

	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey,
			boolean isSecure) {
		this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.DEFAULT_HOST);
	}

	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey,
			boolean isSecure, String server) {
		this(awsAccessKeyId, awsSecretAccessKey, isSecure, server,
				isSecure ? Utils.SECURE_PORT : Utils.INSECURE_PORT);
	}

	/**
	 * Create a new interface to interact with S3 with the given credential and
	 * connection parameters
	 * 
	 * @param awsAccessKeyId
	 *            The your user key into AWS
	 * @param awsSecretAccessKey
	 *            The secret string used to generate signatures for
	 *            authentication.
	 * @param isSecure
	 *            True if the data should be encrypted on the wire on the way to
	 *            or from S3.
	 * @param server
	 *            Which host to connect to. Usually, this will be
	 *            s3.amazonaws.com
	 * @param port
	 *            Which port to use.
	 */
	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey,
			boolean isSecure, String server, int port) {
		this.awsAccessKeyId = awsAccessKeyId;
		this.awsSecretAccessKey = awsSecretAccessKey;
		this.isSecure = isSecure;
		this.server = server;
		this.port = port;
	}

	/**
	 * Creates a new bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to create.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response createBucket(String bucket, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new Response(makeRequest("PUT", bucket, headers));
	}

	/**
	 * Lists the contents of a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to create.
	 * @param prefix
	 *            All returned keys will start with this string (can be null).
	 * @param marker
	 *            All returned keys will be lexographically greater than this
	 *            string (can be null).
	 * @param maxKeys
	 *            The maximum number of keys to return (can be null).
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public ListBucketResponse listBucket(String bucket, String prefix,
			String marker, Integer maxKeys, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		String path = Utils.pathForListOptions(bucket, prefix, marker, maxKeys);
		return new ListBucketResponse(makeRequest("GET", path, headers));
	}

	/**
	 * Deletes a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to delete.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response deleteBucket(String bucket, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new Response(makeRequest("DELETE", bucket, headers));
	}

	/**
	 * Writes an object to S3.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param key
	 *            The name of the key to use.
	 * @param object
	 *            An S3Object containing the data to write.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response put(String bucket, String key, S3Object object, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";

		HttpURLConnection request = makeRequest("PUT", bucket + pathSep
				+ Utils.urlencodePath(key), headers, object);

		request.setDoOutput(true);
		request.getOutputStream().write(
				object.data == null ? new byte[] {} : object.data);

		return new Response(request);
	}

	/**
	 * Writes an object to S3.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param key
	 *            The name of the key to use.
	 * @param object
	 *            An S3StreamObject containing the data stream to write.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response putStream(String bucket, String key, S3StreamObject object,
			Map<String, List<String>> headers) throws MalformedURLException, IOException {

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";
		HttpURLConnection request = makeStreamRequest("PUT", bucket + pathSep
				+ Utils.urlencodePath(key), headers, object);

		request.setDoOutput(true);
		if (object.length != 0) {
			request.setFixedLengthStreamingMode((int) object.length);
		}

		byte[] buf = new byte[1024];
		int bytesRead = 0;

		try (OutputStream out = request.getOutputStream()) {
			while ((bytesRead = object.stream.read(buf)) > 0) {
				out.write(buf, 0, bytesRead);
			}
		}

		return new Response(request);
	}

	/**
	 * Reads an object from S3.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public GetResponse get(String bucket, String key, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";

		return new GetResponse(makeRequest("GET", bucket + pathSep
				+ Utils.urlencodePath(key), headers));
	}

	/**
	 * Reads an object from S3 using streaming.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public GetStreamResponse getStream(String bucket, String key, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";

		return new GetStreamResponse(makeRequest("GET", bucket + pathSep
				+ Utils.urlencodePath(key), headers));
	}

	/**
	 * Reads a BitTorrent file (.torrent) for an object from S3.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public GetResponse getTorrent(String bucket, String key, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";

		return new GetResponse(makeRequest("GET", bucket + pathSep
				+ Utils.urlencodePath(key) + "?torrent", headers));
	}

	/**
	 * Deletes an object from S3.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response delete(String bucket, String key, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";
		return new Response(makeRequest("DELETE", bucket + pathSep
				+ Utils.urlencodePath(key), headers));
	}

	/**
	 * Get the ACL for a given bucket
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public GetResponse getBucketACL(String bucket, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return getACL(bucket, "", headers);
	}

	/**
	 * Get the ACL for a given object (or bucket, if key is null).
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public GetResponse getACL(String bucket, String key, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";
		return new GetResponse(makeRequest("GET", bucket + pathSep
				+ Utils.urlencodePath(key) + "?acl", headers));
	}

	/**
	 * Write a new ACL for a given bucket
	 * 
	 * @param aclXMLDoc
	 *            The xml representation of the ACL as a String
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response putBucketACL(String bucket, String aclXMLDoc, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return putACL(bucket, "", aclXMLDoc, headers);
	}

	/**
	 * Write a new ACL for a given object
	 * 
	 * @param aclXMLDoc
	 *            The xml representation of the ACL as a String
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public Response putACL(String bucket, String key, String aclXMLDoc,
			Map<String, List<String>> headers) throws MalformedURLException, IOException {
		S3Object object = new S3Object(aclXMLDoc.getBytes(), null);

		boolean isEmptyKey = (key == null) || (key.length() == 0)
				|| (key.trim().length() == 0);
		String pathSep = isEmptyKey ? "" : "/";

		if (key == null)
			key = "";
		HttpURLConnection request = makeRequest("PUT", bucket + pathSep
				+ Utils.urlencodePath(key) + "?acl", headers, object);

		request.setDoOutput(true);
		request.getOutputStream().write(
				object.data == null ? new byte[] {} : object.data);

		return new Response(request);
	}

	/**
	 * List all the buckets created by this account.
	 * 
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @return the response object
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public ListAllMyBucketsResponse listAllMyBuckets(Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new ListAllMyBucketsResponse(makeRequest("GET", "", headers));
	}

	/**
	 * Make a new HttpURLConnection without passing an S3Object parameter.
	 */
	private HttpURLConnection makeRequest(String method, String resource,
			Map<String, List<String>> headers) throws MalformedURLException, IOException {
		return makeRequest(method, resource, headers, null);
	}

	/**
	 * Make a new HttpURLConnection.
	 * 
	 * @param method
	 *            The HTTP method to use (GET, PUT, DELETE)
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @param object
	 *            The S3Object that is to be written (can be null).
	 */
	private HttpURLConnection makeRequest(String method, String resource,
			Map<String, List<String>> headers, S3Object object) throws MalformedURLException,
			IOException {
		URL url = makeURL(resource);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(3600000); // 1hr
		connection.setRequestMethod(method);

		addHeaders(connection, headers);
		if (object != null)
			addMetadataHeaders(connection, object.metadata);
		addAuthHeader(connection, method, resource);

		return connection;
	}

	/**
	 * Make a new HttpURLConnection.
	 * 
	 * @param method
	 *            The HTTP method to use (GET, PUT, DELETE)
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @param object
	 *            The S3StreamObject that is to be written (can be null).
	 */
	private HttpURLConnection makeStreamRequest(String method, String resource,
			Map<String, List<String>> headers, S3StreamObject object) throws MalformedURLException,
			IOException {
		URL url = makeURL(resource);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(3600000); // 1hr
		connection.setRequestMethod(method);

		addHeaders(connection, headers);
		if (object != null)
			addMetadataHeaders(connection, object.metadata);
		addAuthHeader(connection, method, resource);

		return connection;
	}

	/**
	 * Add the given headers to the HttpURLConnection.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the headers will be added.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	private void addHeaders(HttpURLConnection connection, Map<String, List<String>> headers) {
		addHeaders(connection, headers, "");
	}

	/**
	 * Add the given metadata fields to the HttpURLConnection.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the headers will be added.
	 * @param metadata
	 *            A Map of String to List of Strings representing the s3
	 *            metadata for this resource.
	 */
	private void addMetadataHeaders(HttpURLConnection connection, Map<String, List<String>> metadata) {
		addHeaders(connection, metadata, Utils.METADATA_PREFIX);
	}

	/**
	 * Add the given headers to the HttpURLConnection with a prefix before the
	 * keys.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the headers will be added.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @param prefix
	 *            The string to prepend to each key before adding it to the
	 *            connection.
	 */
	private void addHeaders(HttpURLConnection connection, Map<String, List<String>> headers,
			String prefix) {
		if (headers != null) {
			for (String key : headers.keySet()) {
				for (String value :headers.get(key)) {
					connection.addRequestProperty(prefix + key, value);
				}
			}
		}
	}

	/**
	 * Add the appropriate Authorization header to the HttpURLConnection.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the header will be added.
	 * @param method
	 *            The HTTP method to use (GET, PUT, DELETE)
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 */
	private void addAuthHeader(HttpURLConnection connection, String method,
			String resource) {
		if (connection.getRequestProperty("Date") == null) {
			connection.setRequestProperty("Date", httpDate());
		}
		if (connection.getRequestProperty("Content-Type") == null) {
			connection.setRequestProperty("Content-Type", "");
		}

		String canonicalString = Utils.makeCanonicalString(method, resource,
				connection.getRequestProperties());
		String encodedCanonical = Utils.encode(awsSecretAccessKey,
				canonicalString, false);
		connection.setRequestProperty("Authorization", "AWS "
				+ awsAccessKeyId + ":" + encodedCanonical);
	}

	/**
	 * Create a new URL object for a given resource.
	 * 
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 */
	private URL makeURL(String resource) throws MalformedURLException {
		String protocol = isSecure ? "https" : "http";
		return new URL(protocol, server, port, "/" + resource);
	}

	/**
	 * Generate an rfc822 date for use in the Date HTTP header.
	 * @return date as string
	 */
	public static String httpDate() {
		final String DateFormat = "EEE, dd MMM yyyy HH:mm:ss ";
		SimpleDateFormat format = new SimpleDateFormat(DateFormat, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(new Date()) + "GMT";
	}
}
