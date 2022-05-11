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
// version:   2.1
// date:      04/19/2006

package com.amazon.s3;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.silvasoftinc.s3.S3StreamObject;

/**
 * A Response object returned from AWSAuthConnection.get(). Exposes the
 * attribute object, which represents the retrieved object.
 */
public class GetStreamResponse extends Response {
	public S3StreamObject object;

	/**
	 * Pulls a representation of an S3Object out of the HttpURLConnection
	 * response.
	 */
	public GetStreamResponse(HttpURLConnection connection) throws IOException {
		super(connection);
		if (connection.getResponseCode() < 400) {
			Map<String, List<String>> metadata = extractMetadata(connection);
			object = new S3StreamObject(connection.getInputStream(),
					metadata);
		}
	}

	/**
	 * Examines the response's header fields and returns a Map from String to
	 * List of Strings representing the object's metadata.
	 */
	private Map<String, List<String>> extractMetadata(HttpURLConnection connection) {
		TreeMap<String, List<String>> metadata = new TreeMap<>();
		Map<String, List<String>> headers = connection.getHeaderFields();
		for (String key : headers.keySet()) {
			if (key == null)
				continue;
			if (key.startsWith(Utils.METADATA_PREFIX)) {
				metadata.put(key.substring(Utils.METADATA_PREFIX.length()),
						headers.get(key));
			}
		}

		return metadata;
	}
}
