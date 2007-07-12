package er.ajax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMultipartIterator;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;

import er.extensions.ERXProperties;
import er.extensions.ERXUnitAwareDecimalFormat;

/**
 * Provides the backend for Ajax uploads. This has to be implemented differently than a normal file upload because we
 * can't block the session while uploading.
 * 
 * @property er.ajax.AjaxFileRequestHandler.tempFileFolder the location of the temp file folder. If not specified, this
 *           will go to Java's default temporary folder (/tmp on Mac OS X)
 * @property er.ajax.AjaxFileRequestHandler.maxUploadSize the maximum size in bytes of the file 
 * @author mschrag
 */
public class AjaxFileUploadRequestHandler extends WORequestHandler {
	public static final String UPLOAD_FINISHED_KEY = "ajaxFileUploadFinished";
	public static final String REQUEST_HANDLER_KEY = "upload";
	public static final Logger log = Logger.getLogger(AjaxFileUploadRequestHandler.class);

	private File _tempFileFolder;
	private long _maxUploadSize;

	public AjaxFileUploadRequestHandler() {
		this(ERXProperties.stringForKey("er.ajax.AjaxFileRequestHandler.tempFileFolder"), ERXProperties.longForKeyWithDefault("er.ajax.AjaxFileRequestHandler.maxUploadSize", -1));
	}

	protected AjaxFileUploadRequestHandler(String tempFilePath, long maxUploadSize) {
		this(tempFilePath == null ? null : new File(tempFilePath), maxUploadSize);
	}

	public AjaxFileUploadRequestHandler(File tempFileFolder, long maxUploadSize) {
		_tempFileFolder = tempFileFolder;
		_maxUploadSize = maxUploadSize;
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
					AjaxUploadProgress progress = new AjaxUploadProgress(uploadIdentifier, tempFile, uploadFileName, streamLength);
					try {
						AjaxProgressBar.registerProgress(session, progress);
					}
					finally {
						if (context._requestSessionID() != null) {
							WOApplication.application().saveSessionForContext(context);
						}
					}

					try {
						if (_maxUploadSize >= 0 && streamLength > _maxUploadSize) {
							IOException e = new IOException("You attempted to upload a file larger than the maximum allowed size of " + new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE).format(_maxUploadSize) + ".");
							progress.setFailure(e);
							progress.dispose();
							throw e;
						}
						FileOutputStream fos = new FileOutputStream(progress.tempFile());
						try {
							progress.copyAndTrack(uploadInputStream, fos);
						}
						finally {
							fos.flush();
							fos.close();
						}
						if (!progress.isCanceled() && !progress.shouldReset()) {
							downloadFinished(progress);
						}
					}
					finally {
						progress.setDone(true);
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
	
	protected void downloadFinished(AjaxUploadProgress progress) {
	}

	/**
	 * Type-safe wrapper around AjaxProgressBar.progress.
	 *  
	 * @param session the session
	 * @param id the id of the progress model to retrieve
	 * @return the AjaxUploadProgress
	 */
	public static AjaxUploadProgress ajaxUploadProgress(WOSession session, String id) {
		return (AjaxUploadProgress) AjaxProgressBar.progress(session, id);
	}
}
