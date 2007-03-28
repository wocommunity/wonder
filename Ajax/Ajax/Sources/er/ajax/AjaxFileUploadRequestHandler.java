package er.ajax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMultipartIterator;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Provides the backend for Ajax uploads. This has to be implemented differently than a normal file upload because we
 * can't block the session while uploading.
 * 
 * @author mschrag
 */
public class AjaxFileUploadRequestHandler extends WORequestHandler {
	public static final String REQUEST_HANDLER_KEY = "upload";
	public static final String UPLOAD_STATUS_KEY = "_uploadStatus";
	public static final Logger log = Logger.getLogger(AjaxFileUploadRequestHandler.class);

	private File _tempFileFolder;

	public AjaxFileUploadRequestHandler() {
		this(null);
	}

	public AjaxFileUploadRequestHandler(File tempFileFolder) {
		_tempFileFolder = tempFileFolder;
	}

	public WOResponse handleRequest(WORequest request) {
		WOApplication application = WOApplication.application();
		application.awake();
		try {
			WOContext context = application.createContextForRequest(request);
			WOResponse response = application.createResponseInContext(context);

			String uploadIdentifier = null;
			String uploadFileName = null;
			InputStream uploadInputStream = null;
			int streamLength = -1;

			try {
				String wosid = request.cookieValueForKey("wosid");
				WOMultipartIterator multipartIterator = request.multipartIterator();
				if (multipartIterator == null) {
					response.appendContentString("Already Consumed!");
				}
				else {
					WOMultipartIterator.WOFormData formData = null;
					while ((formData = multipartIterator.nextFormData()) != null) {
						String name = formData.name();
						if ("wosid".equals(name)) {
							wosid = formData.formValue();
						}
						else if ("id".equals(name)) {
							uploadIdentifier = formData.formValue();
						}
						else if (formData.isFileUpload()) {
							uploadFileName = request.stringFormValueForKey(name + ".filename");
							streamLength = multipartIterator.contentLengthRemaining();
							uploadInputStream = formData.formDataInputStream();
							break;
						}
					}
					context._setRequestSessionID(wosid);
					WOSession session = null;
					if (context._requestSessionID() != null) {
						session = WOApplication.application().restoreSessionWithID(wosid, context);
					}

					File tempFile = File.createTempFile("AjaxFileUpload", ".tmp", _tempFileFolder);
					tempFile.deleteOnExit();
					UploadStatus status = new UploadStatus(tempFile, uploadFileName, streamLength);
					try {
						if (session != null) {
							NSMutableDictionary uploadStatuses = (NSMutableDictionary) session.objectForKey(AjaxFileUploadRequestHandler.UPLOAD_STATUS_KEY);
							if (uploadStatuses == null) {
								uploadStatuses = new NSMutableDictionary();
								session.setObjectForKey(uploadStatuses, AjaxFileUploadRequestHandler.UPLOAD_STATUS_KEY);
							}
							uploadStatuses.setObjectForKey(status, uploadIdentifier);
						}
					}
					finally {
						if (context._requestSessionID() != null) {
							WOApplication.application().saveSessionForContext(context);
						}
					}

					byte[] buffer = new byte[64 * 1024];
					try {
						FileOutputStream fos = new FileOutputStream(status.tempFile());
						try {
							boolean done = false;
							do {
								int bytesRead = uploadInputStream.read(buffer);
								if (bytesRead <= 0) {
									done = true;
								}
								else {
									status.incrementBytesRead(bytesRead);
									fos.write(buffer, 0, bytesRead);
								}
							}
							while (!done && !status.isCancelled());
						}
						finally {
							fos.flush();
							fos.close();
						}
						if (status.isCancelled()) {
							status.dispose();
						}
					}
					catch (Throwable e) {
						status.dispose();
						status.setFailure(e);
						throw e;
					}
					finally {
						status.setDone(true);
					}
				}
			}
			catch (Throwable t) {
				log.error(t);
				response.appendContentString("Failed: " + t.getMessage());
			}
			return response;
		}
		finally {
			application.sleep();
		}
	}

	public static void disposeStatus(WOSession session, String id) {
		NSMutableDictionary uploadStatuses = (NSMutableDictionary) session.objectForKey(AjaxFileUploadRequestHandler.UPLOAD_STATUS_KEY);
		if (uploadStatuses != null) {
			uploadStatuses.removeObjectForKey(id);
		}
	}

	public static AjaxFileUploadRequestHandler.UploadStatus uploadStatus(WOSession session, String id) {
		AjaxFileUploadRequestHandler.UploadStatus uploadStatus = null;
		NSDictionary uploadStatuses = (NSDictionary) session.objectForKey(AjaxFileUploadRequestHandler.UPLOAD_STATUS_KEY);
		if (uploadStatuses != null) {
			uploadStatus = (AjaxFileUploadRequestHandler.UploadStatus) uploadStatuses.objectForKey(id);
		}
		return uploadStatus;
	}

	public static class UploadStatus {
		private long _bytesRead;
		private long _streamLength;
		private boolean _done;
		private Throwable _failure;
		private File _tempFile;
		private boolean _cancelled;
		private String _fileName;

		public UploadStatus(File tempFile, String fileName, int streamLength) {
			_tempFile = tempFile;
			_fileName = fileName;
			_streamLength = streamLength;
		}

		public String fileName() {
			return _fileName;
		}

		public File tempFile() {
			return _tempFile;
		}

		public void incrementBytesRead(long count) {
			_bytesRead += count;
		}

		public long bytesRead() {
			return _bytesRead;
		}

		public long streamLength() {
			return _streamLength;
		}

		public boolean isStarted() {
			return _bytesRead > 0;
		}

		public void setDone(boolean done) {
			_done = done;
		}

		public boolean isDone() {
			return _done;
		}

		public void setFailure(Throwable failure) {
			_failure = failure;
		}

		public Throwable failure() {
			return _failure;
		}

		public void cancel() {
			_cancelled = true;
		}

		public boolean isCancelled() {
			return _cancelled;
		}

		public boolean isFailed() {
			return !_cancelled && _failure != null;
		}

		public boolean isSucceeded() {
			return !_cancelled && _failure == null;
		}

		public void dispose() {
			_tempFile.delete();
		}
	}
}
