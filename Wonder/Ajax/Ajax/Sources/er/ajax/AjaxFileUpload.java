package er.ajax;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.NumberFormat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

import er.extensions.ERXComponentUtilities;
import er.extensions.ERXFileUtilities;
import er.extensions.ERXThreadStorage;
import er.extensions.ERXUnitAwareDecimalFormat;

/**
 * AjaxFileUpload provides an Ajax wrapper around the file upload process.  This works pretty differently than
 * WOFileUpload.  The AjaxFileUpload component itself provides its own form and autosubmits when the user
 * selects a file to upload.  The upload runs in a hidden iframe, with Ajax updates occurring in the main
 * window.  When the final ajax update occurs after the completion of the upload, the appropriate actions
 * fire.  This means that if the user navigates away during the upload, no completion/failure/etc notifications
 * will occur.
 * 
 * @binding cancelledText the text to display when the upload is cancelled
 * @binding succeededText the text to display when the upload succeeds
 * @binding failedText the text to display when the upload fails
 * @binding cancelText the text to display for the cancel link
 * @binding resetText the text to display for the reset link
 * @binding finishedAction the action to fire when the upload finishes (cancel, failed, or succeeded)
 * @binding cancelledAction the action to fire when the upload is cancelled
 * @binding succeededAction the action to fire when the upload succeeded
 * @binding failedAction the action to fire when the upload fails
 * @binding data the NSData that will be bound with the contents of the upload
 * @binding inputStream will be bound to an input stream on the contents of the upload
 * @binding outputStream the output stream to write the contents of the upload to
 * @binding streamToFilePath the path to write the upload to
 * @binding finalFilePath the final file path of the upload (when streamToFilePath is set) 
 * @binding filePath the name of the uploaded file
 * 
 * @author mschrag
 */
public class AjaxFileUpload extends WOComponent {
	private static boolean _requestHandlerRegistered = false;

	private String _id;
	private boolean _fileUploading;

	public AjaxFileUpload(WOContext context) {
		super(context);
		synchronized (AjaxFileUpload.class) {
			if (!_requestHandlerRegistered) {
				WOApplication.application().registerRequestHandler(new AjaxFileUploadRequestHandler(), AjaxFileUploadRequestHandler.REQUEST_HANDLER_KEY);
				_requestHandlerRegistered = true;
			}
		}
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendToResponse(aResponse, aContext);
		AjaxUtils.addScriptResourceInHead(aContext, aResponse, "prototype.js");
		AjaxUtils.addScriptResourceInHead(aContext, aResponse, "scriptaculous.js");
		AjaxUtils.addScriptResourceInHead(aContext, aResponse, "wonder.js");
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public AjaxFileUploadRequestHandler.UploadStatus uploadStatus() {
		AjaxFileUploadRequestHandler.UploadStatus status = AjaxFileUploadRequestHandler.uploadStatus(session(), id());
		return status;
	}

	public String id() {
		String id = _id;
		if (id == null) {
			id = (String) valueForBinding("id");
			if (id == null) {
				id = AjaxUtils.toSafeElementID(context().elementID());
			}
			_id = id;
		}
		return id;
	}

	public String finishedClass() {
		String finishedClass;
		String percentage = percentage();
		if ("0".equals(percentage)) {
			finishedClass = "percentageUnfinished";
		}
		else {
			finishedClass = "percentageFinished";
		}
		return finishedClass;
	}

	public String percentage() {
		AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
		String percentageStr;
		if (status == null) {
			percentageStr = "0";
		}
		else {
			double bytesRead = status.bytesRead();
			double streamLength = status.streamLength();
			double percentage = 0;
			if (streamLength > 0) {
				percentage = (bytesRead / streamLength) * 100;
			}
			if (percentage < 5) {
				percentageStr = "0";
			}
			else {
				percentageStr = NumberFormat.getIntegerInstance().format(percentage) + "%";
			}
		}
		return percentageStr;
	}

	public String uploadUrl() {
		String uploadUrl = context().urlWithRequestHandlerKey(AjaxFileUploadRequestHandler.REQUEST_HANDLER_KEY, "", null);
		return uploadUrl;
	}

	public String bytesReadSize() {
		String bytesReadSize = null;
		AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
		if (status != null) {
			NumberFormat formatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
			formatter.setMaximumFractionDigits(2);
			bytesReadSize = formatter.format(status.bytesRead());
		}
		return bytesReadSize;
	}

	public String streamLengthSize() {
		String streamLengthSize = null;
		AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
		if (status != null) {
			NumberFormat formatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
			formatter.setMaximumFractionDigits(2);
			streamLengthSize = formatter.format(status.streamLength());
		}
		return streamLengthSize;
	}

	public boolean isUploading() {
		return uploadStatus() != null || _fileUploading;
	}

	public String cancelledText() {
		String cancelledText = (String) valueForBinding("cancelledText");
		if (cancelledText == null) {
			cancelledText = "Cancelled!";
		}
		return cancelledText;
	}

	public String succeededText() {
		String succeededText = (String) valueForBinding("succeededText");
		if (succeededText == null) {
			succeededText = "Finished!";
		}
		return succeededText;
	}

	public String failedText() {
		String failedText = (String) valueForBinding("failedText");
		if (failedText == null) {
			failedText = "Failed!";
		}
		return failedText;
	}

	public String cancelText() {
		String cancelText = (String) valueForBinding("cancelText");
		if (cancelText == null) {
			cancelText = "cancel";
		}
		return cancelText;
	}

	public String resetText() {
		String resetText = (String) valueForBinding("resetText");
		if (resetText == null) {
			resetText = "reset";
		}
		return resetText;
	}

	public WOActionResults fileUploading() throws MalformedURLException, IOException {
		if (ERXThreadStorage.valueForKey("resetting") == null) {
			_fileUploading = true;
			AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
			if (status != null) {
				if (status.isDone()) {
					if (status.isCancelled()) {
						uploadCancelled();
					}
					else if (status.isFailed()) {
						uploadFailed();
					}
					else if (status.isSucceeded()) {
						uploadSucceeded();
					}
				}
			}
		}
		return null;
	}

	public WOActionResults resetUpload() {
		_fileUploading = false;
		ERXThreadStorage.takeValueForKey(Boolean.TRUE, "resetting");
		AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
		if (status != null) {
			AjaxFileUploadRequestHandler.disposeStatus(session(), id());
		}
		return null;
	}

	public WOActionResults cancel() {
		AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
		if (status != null) {
			status.cancel();
		}
		return null;
	}

	protected void finished() {
		_fileUploading = false;
		valueForBinding("finishedAction");
	}

	protected void uploadCancelled() {
		finished();
		valueForBinding("cancelledAction");
	}

	protected void uploadSucceeded() throws MalformedURLException, IOException {
		AjaxFileUploadRequestHandler.UploadStatus status = uploadStatus();
		try {
			boolean deleteFile = true;
			if (hasBinding("data")) {
				NSData data = new NSData(status.tempFile().toURL());
				setValueForBinding(data, "data");
			}
			if (hasBinding("inputStream")) {
				setValueForBinding(new FileInputStream(status.tempFile()), "inputStream");
				deleteFile = false;
			}
			if (hasBinding("outputStream")) {
				OutputStream outputStream = (OutputStream) valueForBinding("outputStream");
				if (outputStream != null) {
					ERXFileUtilities.writeInputStreamToOutputStream(new FileInputStream(status.tempFile()), outputStream);
				}
			}
			if (hasBinding("streamToFilePath")) {
				File streamToFile = new File((String) valueForBinding("streamToFilePath"));
				boolean renamedFile;
				boolean renameFile;
				if (streamToFile.exists()) {
					renameFile = ERXComponentUtilities.booleanValueForBinding(this, "overwrite");
				}
				else {
					renameFile = true;
				}
				if (renameFile) {
					ERXFileUtilities.renameTo(status.tempFile(), streamToFile);
					renamedFile = true;
				}
				else {
					renamedFile = false;
				}
				if (hasBinding("finalFilePath")) {
					String finalFilePath;
					if (renamedFile) {
						finalFilePath = streamToFile.getAbsolutePath();
					}
					else {
						finalFilePath = status.tempFile().getAbsolutePath();
					}
					setValueForBinding(finalFilePath, "finalFilePath");
				}
				deleteFile = false;
			}
			if (hasBinding("filePath")) {
				setValueForBinding(status.fileName(), "filePath");
			}
			if (deleteFile) {
				status.dispose();
			}
		}
		finally {
			finished();
		}
		valueForBinding("succeededAction");
	}

	protected void uploadFailed() {
		finished();
		valueForBinding("failedAction");
	}
}