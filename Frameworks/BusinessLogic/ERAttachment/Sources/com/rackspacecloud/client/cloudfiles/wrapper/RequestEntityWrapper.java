/**
 * 
 */
package com.rackspacecloud.client.cloudfiles.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import com.rackspacecloud.client.cloudfiles.IFilesTransferCallback;

/**
 * @author lvaughn
 *
 */
public class RequestEntityWrapper implements HttpEntity {
	private HttpEntity entity;
	private IFilesTransferCallback callback = null;
	
	public RequestEntityWrapper(HttpEntity entity, IFilesTransferCallback callback) {
		this.entity = entity;
		this.callback = callback;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#getContentLength()
	 */
	public long getContentLength() {
		return entity.getContentLength();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#getContentType()
	 */
	public Header getContentType() {
		return entity.getContentType();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#isRepeatable()
	 */
	public boolean isRepeatable() {
		return entity.isRepeatable();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#writeRequest(java.io.OutputStream)
	 *
	public void writeRequest(OutputStream stream) throws IOException {
		((RequestEntityWrapper) entity).writeRequest(new OutputStreamWrapper(stream, callback));
		
	} */

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#consumeContent()
	 */
	@SuppressWarnings("deprecation")
	public void consumeContent() throws IOException {
		entity.consumeContent();
		
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#getContent()
	 */
	public InputStream getContent() throws IOException, IllegalStateException {
		return entity.getContent();
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#getContentEncoding()
	 */
	public Header getContentEncoding() {
		return entity.getContentEncoding();
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#isChunked()
	 */
	public boolean isChunked() {
		return entity.isChunked();
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#isStreaming()
	 */
	public boolean isStreaming() {
		return entity.isStreaming();
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream os) throws IOException {
		entity.writeTo(new OutputStreamWrapper(os, callback));
		
	}
	
}
