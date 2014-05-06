package er.ajax;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.formatters.ERXUnitAwareDecimalFormat;

/**
 * AjaxProgress is the model for an AjaxProgressBar.  By holding
 * onto this, you can keep track of and control the progress
 * of whatever operation is bound to this progress object. 
 * 
 * @author mschrag
 */
public class AjaxProgress {
	private String _id;
	private long _value;
	private long _maximum;
	private boolean _done;
	private Throwable _failure;
	private boolean _canceled;
	private boolean _completionEventsFired;
	private boolean _reset;
	private String _status;

	/**
	 * Construct an AjaxProgress
	 * 
	 * @param maximum the maximum value of this progress
	 */
	public AjaxProgress(long maximum) {
		this("AjaxProgress" + System.currentTimeMillis(), maximum);
	}

	@Deprecated
	public AjaxProgress(int maximum) {
		this((long) maximum);
	}

	/**
	 * Construct an AjaxProgress
	 *
	 * @param id the id of this AjaxProgress (only useful if you're registering it with AjaxProgressBar's registry)
	 * @param maximum the maximum value of this progress
	 */
	public AjaxProgress(String id, long maximum) {
		_id = id;
		_maximum = maximum;
	}

	@Deprecated
	public AjaxProgress(String id, int maximum) {
		this(id, (long) maximum);
	}

	/**
	 * Returns the id of this progress model.
	 * 
	 * @return the id of this progress model
	 */
	public String id() {
		return _id;
	}

	/**
	 * Sets the current value of this progress model.  In the context of
	 * an upload, value would represent the number of bytes uploaded
	 * so far.
	 *  
	 * @param value the new value
	 */
	public void setValue(long value) {
		_value = value;
	}

	/**
	 * Returns the current value of this progress model.  If this
	 * model isSucceeded, then this will return maximum().
	 *  
	 * @return the current value of this progress model
	 */
	public long value() {
		long value;
		if (isSucceeded()) {
			value = maximum();
		}
		else {
			value = _value;
		}
		return value;
	}

	/**
	 * Increment value by the given amount.
	 * 
	 * @param count the mount to increment value by
	 */
	public void incrementValue(long count) {
		_value += count;
	}

	/**
	 * Sets the maximum value for this progress model.
	 * 
	 * @param maximum the maximum value for this progress model
	 */
	public void setMaximum(long maximum) {
		_maximum = maximum;
	}

	/**
	 * Returns the maximum value for this progress model.
	 * 
	 * @return the maximum value for this progress model
	 */
	public long maximum() {
		return _maximum;
	}

	/**
	 * Returns the percentage completion of this progress model (0.0 - 1.0).
	 * 
	 * @return the percentage completion of this progress model (0.0 - 1.0)
	 */
	public double percentage() {
		double value = value();
		double maximum = maximum();
		double percentage = 0;
		if (maximum > 0) {
			percentage = (value / maximum);
		}
		return percentage;
	}

	/**
	 * Returns whether or not this procedure has started.
	 * 
	 * @return whether or not this procedure has started
	 */
	public boolean isStarted() {
		return _value > 0 || isDone();
	}

	/**
	 * Sets whether or not this procedure is done.
	 * 
	 * @param done whether or not this procedure is done
	 */
	public void setDone(boolean done) {
		_done = done;
	}

	/**
	 * Returns whether or not this procedure is done.
	 * 
	 * @return whether or not this procedure is done
	 */
	public boolean isDone() {
		return _done;
	}

	/**
	 * Sets the exception that caused this procedure to fail.
	 * 
	 * @param failure the exception that caused this procedure to fail
	 */
	public void setFailure(Throwable failure) {
		_failure = failure;
	}

	/**
	 * Returns the exception that caused this procedure to fail.
	 * @return the exception that caused this procedure to fail
	 */
	public Throwable failure() {
		return _failure;
	}

	/**
	 * Cancels this procedure.
	 */
	public void cancel() {
		_canceled = true;
	}

	/**
	 * Returns true if this procedure was canceled.
	 * @return true if this procedure was canceled
	 */
	public boolean isCanceled() {
		return _canceled;
	}

	/**
	 * Returns true if this procedure failed (and was not canceled).
	 * @return true if this procedure failed (and was not canceled)
	 */
	public boolean isFailed() {
		return !_canceled && _failure != null;
	}

	/**
	 * Returns true if this procedure is done, not canceled, and not failed.
	 * 
	 * @return true if this procedure is done, not canceled, and not failed
	 */
	public boolean isSucceeded() {
		return _done && !_reset && !_canceled && _failure == null;
	}

	/**
	 * Disposes any resources associated with this procedure.
	 */
	public void dispose() {
		_completionEventsFired	= false;
	}

	/**
	 * Sets whether or not this procedure has notified listeners of its completion.
	 * 
	 * @param completionEventsFired whether or not this procedure has notified listeners of its completion
	 */
	public void setCompletionEventsFired(boolean completionEventsFired) {
		_completionEventsFired = completionEventsFired;
	}
	
	/**
	 * Returns whether or not this procedure has notified listeners of its completion.
	 * @return whether or not this procedure has notified listeners of its completion
	 */
	public boolean completionEventsFired() {
		return _completionEventsFired;
	}
	
	/**
	 * Flags the attached procedure to reset the next time it is processed.
	 */
	public void reset() {
		_reset = true;
	}
	
	/**
	 * Returns whether or not the attached procedure should reset the next time it is processed.
	 * 
	 * @return whether or not the attached procedure should reset the next time it is processed
	 */
	public boolean shouldReset() {
		return _reset;
	}
	
	/**
	 * Sets the current status message for this process.
	 * 
	 * @param status the current status message for this process
	 */
	public void setStatus(String status) {
		_status = status;
	}
	
	/**
	 * Returns the current status message for this process.
	 * 
	 * @return the current status message for this process
	 */
	public String status() {
		return _status;
	}
	
	/**
	 * Convenience method for copying a stream and tracking it with this progress model.
	 * 
	 * @param inputStream the input stream to copy from
	 * @param outputStream the output stream to copy to
	 * @param maxSize the maximum size to read
	 * @throws IOException if there is a failure
	 */
	public void copyAndTrack(InputStream inputStream, OutputStream outputStream, long maxSize) throws IOException {
		byte[] buffer = new byte[64 * 1024];
		try {
			boolean done = false;
			do {
				int bytesRead = inputStream.read(buffer);
				if (bytesRead <= 0) {
					done = true;
				}
				else {
					incrementValue(bytesRead);
					if (maxSize > 0 && _value > maxSize) {
						throw new IOException("The provided stream exceeded the maximum length of " + new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE).format(maxSize) + " bytes.");
					}
					outputStream.write(buffer, 0, bytesRead);
				}
			}
			while (!done && !isCanceled() && !shouldReset());
			if (isCanceled() || shouldReset()) {
				dispose();
			}
		}
		catch (IOException e) {
			dispose();
			setFailure(e);
			throw e;
		}
		catch (RuntimeException e) {
			dispose();
			setFailure(e);
			throw e;
		}
	}
	

	/**
	 * Register a progress object in the registry.
	 * 
	 * @param session
	 *            the session
	 * @param progress
	 *            the progress object to register
	 */
	public static void registerProgress(WOSession session, AjaxProgress progress) {
		NSMutableDictionary progresses = (NSMutableDictionary) session.objectForKey(AjaxProgressBar.AJAX_PROGRESSES_KEY);
		if (progresses == null) {
			progresses = new NSMutableDictionary();
			session.setObjectForKey(progresses, AjaxProgressBar.AJAX_PROGRESSES_KEY);
		}
		progresses.setObjectForKey(progress, progress.id());
	}

	/**
	 * Unregister a progress object from the registry.
	 * 
	 * @param session
	 *            the session
	 * @param progress
	 *            the progress object to unregister
	 */
	public static void unregisterProgress(WOSession session, AjaxProgress progress) {
		NSMutableDictionary progresses = (NSMutableDictionary) session.objectForKey(AjaxProgressBar.AJAX_PROGRESSES_KEY);
		if (progresses != null && progress.id() != null) {
			progresses.removeObjectForKey(progress.id());
		}
	}

	/**
	 * Returns the progress object with the given id (or null if one does not exist).
	 * 
	 * @param session
	 *            the session
	 * @param id
	 *            the id of the progress to retrieve
	 * @return the matching progess object (or null)
	 */
	public static AjaxProgress progress(WOSession session, String id) {
		AjaxProgress progress = null;
		NSDictionary progresses = (NSDictionary) session.objectForKey(AjaxProgressBar.AJAX_PROGRESSES_KEY);
		if (progresses != null) {
			progress = (AjaxProgress) progresses.objectForKey(id);
		}
		return progress;
	}
}
