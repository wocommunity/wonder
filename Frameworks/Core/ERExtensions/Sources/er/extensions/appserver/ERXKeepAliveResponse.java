package er.extensions.appserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSForwardException;

/**
 * Special response that keeps the connection alive and pushes the data to
 * the client. It does this by opening a stream that has small buffer but
 * huge length.
 * 
 * @author ak
 */
public class ERXKeepAliveResponse extends ERXResponse {
	private static final Logger log = LoggerFactory.getLogger(ERXKeepAliveResponse.class);

	/**
	 * Queue to push the items into.
	 */
	protected Queue<byte[]> _queue = new ConcurrentLinkedQueue<>();

	/**
	 * Current data to write to client.
	 */
	protected byte[] _current = null;

	/**
	 * Current index in
	 */
	protected int _currentIndex = 0;

	public ERXKeepAliveResponse() {
		//setHeader("keep-alive", "connection");
		setContentStream(new InputStream() {
			@Override
			public int read() throws IOException {
				synchronized (_queue) {
					if (_current != null && _currentIndex >= _current.length) {
						_current = null;
						_currentIndex = 0;
					}
					if (_current == null) {
						try {
							if (log.isDebugEnabled()) {
								log.debug("waiting: {}", _queue.hashCode());
							}
							_queue.wait();
							if (log.isDebugEnabled()) {
								log.debug("got data: {}", _queue.hashCode());
							}
						}
						catch (InterruptedException e) {
							return -1;
						}
						_current = _queue.poll();
					}
					if (_current == null) {
						return -1;
					}
					log.debug("writing: {}", _currentIndex);
					return _current[_currentIndex++];
				}
			}

		}, 1, Long.MAX_VALUE); // MS: turning it up to 11
	}

	/**
	 * Enqueues the data for this string using the response encoding.
	 * 
	 * @param str the string to push
	 */
	public void push(String str) {
		try {
			push(str.getBytes(contentEncoding()));
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
	
	/**
	 * Enqueues the data.
	 * 
	 * @param data
	 */
	public void push(byte[] data) {
		if (log.isDebugEnabled()) {
			log.debug("pushing: " + _queue.hashCode());
		}
		synchronized (_queue) {
			_queue.offer(data);
			_queue.notify();
		}
	}

	/**
	 * Resets the response by clearing out the current item and notifying
	 * the queue.
	 */
	public void reset() {
		synchronized (_queue) {
			_current = null;
			_currentIndex = 0;
			_queue.notify();
		}
	}
}