package er.ajax;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.NumberFormat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.formatters.ERXUnitAwareDecimalFormat;
import er.extensions.foundation.ERXFileUtilities;

/**
 * AjaxFileUpload provides an Ajax wrapper around the file upload process. This works pretty differently than
 * WOFileUpload. The AjaxFileUpload component itself provides its own form and autosubmits when the user selects a file
 * to upload. The upload runs in a hidden iframe, with Ajax updates occurring in the main window. When the final ajax
 * update occurs after the completion of the upload, the appropriate actions fire. This means that if the user navigates
 * away during the upload, no completion/failure/etc notifications will occur.
 * 
 * @binding cancelText the text to display for the cancel link
 * @binding cancelingText the text to display when the progress is being canceled
 * @binding startingText the text to display when the progress is starting
 * @binding startedFunction the javascript function to execute when the progress is started
 * @binding canceledFunction the javascript function to execute when the upload is canceled
 * @binding succeededFunction the javascript function to execute when the upload succeeds
 * @binding failedFunction the javascript function to execute when the upload fails
 * @binding finishedFunction the javascript function to execute when the upload finishes (succeeded, failed, or
 *          canceled)
 * @binding finishedAction the action to fire when the upload finishes (cancel, failed, or succeeded)
 * @binding canceledAction the action to fire when the upload is canceled
 * @binding succeededAction the action to fire when the upload succeeded
 * @binding failedAction the action to fire when the upload fails
 * @binding data the NSData that will be bound with the contents of the upload
 * @binding inputStream will be bound to an input stream on the contents of the upload
 * @binding outputStream the output stream to write the contents of the upload to
 * @binding streamToFilePath the path to write the upload to, can be a directory
 * @binding finalFilePath the final file path of the upload (when streamToFilePath is set or keepTempFile = true)
 * @binding filePath the name of the uploaded file
 * @binding allowCancel if true, the cancel link is visible
 * @binding progressBarBeforeStart if true, the progress bar is visible before the upload is started
 * @binding progressBarAfterDone if true, the progress bar is visible after the upload is done
 * @binding refreshTime the number of milliseconds to wait between refreshes
 * @binding keepTempFile if true, don't delete the temp file that AjaxFileUpload creates
 * @binding uploadLabel the label to display on the Upload button ("Upload" by default)
 * @binding uploadFunctionName the upload button will instead be a function with the given name
 * @binding progressOfText the text to display for the word "of" in the "[size] of [totalsize]" string during upload
 * @binding mimeType set from the content-type of the upload header if available
 * @binding class the class attribute of the file input
 * @binding style the style attribute of the file input
 * @binding id the id attribute of the file input
 * @binding onFileSelected optional JS code that is called when the file selection changes. To auto-start the upload when 
 * 			a file is selected, set uploadFunctionName to e.g. "startUpload" and onFileSelected to "startUpload()"
 * @binding uploadProgress access to the underlying AjaxUploadProgress object
 * 
 * @author mschrag
 */
public class AjaxFileUpload extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static volatile boolean _requestHandlerRegistered = false;

	private String _id;
	protected boolean _uploadStarted;
	protected AjaxUploadProgress _progress;
	protected boolean _triggerUploadStart;
	private String _requestHandlerKey;

	public AjaxFileUpload(WOContext context) {
		super(context);
		_requestHandlerKey = AjaxFileUploadRequestHandler.REQUEST_HANDLER_KEY;
		if (!_requestHandlerRegistered) {
			synchronized (AjaxFileUpload.class) {
				if (!_requestHandlerRegistered) {
					if (WOApplication.application().requestHandlerForKey(AjaxFileUploadRequestHandler.REQUEST_HANDLER_KEY) == null) {
						WOApplication.application().registerRequestHandler(new AjaxFileUploadRequestHandler(), AjaxFileUploadRequestHandler.REQUEST_HANDLER_KEY);
					}
					_requestHandlerRegistered = true;
				}
			}
		}
	}

	public void setRequestHandlerKey(String requestHandlerKey) {
		_requestHandlerKey = requestHandlerKey;
	}
	
	public String requestHandlerKey() {
		return _requestHandlerKey;
	}
	
	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendToResponse(aResponse, aContext);
		AjaxUtils.addScriptResourceInHead(aContext, aResponse, "prototype.js");
		AjaxUtils.addScriptResourceInHead(aContext, aResponse, "effects.js");
		AjaxUtils.addScriptResourceInHead(aContext, aResponse, "wonder.js");
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String uploadLabel() {
		String uploadLabel = (String)valueForBinding("uploadLabel");
		if (uploadLabel == null) {
			uploadLabel = "Upload";
		}
		return uploadLabel;
	}
	
	public boolean progressBarBeforeStart() {
		boolean progressBarBeforeStart = false;
		if (hasBinding("progressBarBeforeStart")) {
			progressBarBeforeStart = ERXComponentUtilities.booleanValueForBinding(this, "progressBarBeforeStart");
		}
		return progressBarBeforeStart;
	}

	public boolean progressBarAfterDone() {
		boolean progressBarAfterDone = false;
		if (hasBinding("progressBarAfterDone")) {
			progressBarAfterDone = ERXComponentUtilities.booleanValueForBinding(this, "progressBarAfterDone");
		}
		return progressBarAfterDone;
	}

	public void setUploadProgress(AjaxUploadProgress progress) {
		_progress = progress;
		if (hasBinding("uploadProgress")) {
			setValueForBinding(progress, "uploadProgress");
		}
		if (progress == null && !hasBinding("uploadStarted")) {
			_uploadStarted = false;
		}
	}

	public AjaxUploadProgress uploadProgress() {
		return _progress;
	}

	public String id() {
		String id = _id;
		if (id == null) {
			id = (String) valueForBinding("id");
			if (id == null) {
				id = ERXWOContext.safeIdentifierName(context(), true);
			}
			_id = id;
		}
		return id;
	}

	public String uploadUrl() {
		String uploadUrl = context().urlWithRequestHandlerKey(_requestHandlerKey, "", null);
		return uploadUrl;
	}

	public String bytesReadSize() {
		String bytesReadSize = null;
		AjaxUploadProgress progress = uploadProgress();
		if (progress != null) {
			NumberFormat formatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
			formatter.setMaximumFractionDigits(2);
			bytesReadSize = formatter.format(progress.value());
		}
		return bytesReadSize;
	}

	public String streamLengthSize() {
		String streamLengthSize = null;
		AjaxUploadProgress progress = uploadProgress();
		if (progress != null) {
			NumberFormat formatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
			formatter.setMaximumFractionDigits(2);
			streamLengthSize = formatter.format(progress.maximum());
		}
		return streamLengthSize;
	}

	public String uploadFrameName() {
		return id() + "UploadFrame";
	}

	public String startUploadName() {
		return id() + "StartUpload";
	}

	public String startUploadFunctionCall() {
		return "form.submit(true); " + startUploadName() + "();";
	}

	public boolean triggerStartUpload() {
		boolean triggerUploadStart = _triggerUploadStart;
		if (triggerUploadStart) {
			_triggerUploadStart = false;
		}
		return triggerUploadStart;
	}

	public String uploadFormID() {
		return id() + "Form";
	}

	public String progressBarID() {
		return id() + "ProgressBar";
	}

	public String startingText() {
		String startingText = (String) valueForBinding("startingText");
		if (startingText == null) {
			startingText = "Upload Starting ...";
		}
		return startingText;
	}

	public String cancelText() {
		String cancelText = (String) valueForBinding("cancelText");
		if (cancelText == null) {
			cancelText = "cancel";
		}
		return cancelText;
	}

	public String cancelingText() {
		String cancelingText = (String) valueForBinding("cancelingText");
		if (cancelingText == null) {
			cancelingText = "Canceling Upload ...";
		}
		return cancelingText;
	}

	public WOActionResults startUpload() {
		_triggerUploadStart = true;
		if (_progress != null) {
			_progress.reset();
		}
		_progress = null;
		if (hasBinding("uploadProgress")) {
			setValueForBinding(null, "uploadProgress");
		}

		_uploadStarted = true;
		if (hasBinding("uploadStarted")) {
			setValueForBinding(Boolean.TRUE, "uploadStarted");
		}
		
		AjaxResponse response = AjaxUtils.createResponse(context().request(), context());
		AjaxUtils.appendScriptHeaderIfNecessary(context().request(), response);
		response.appendContentString("document." + uploadFormID() + ".submit();");
		AjaxUtils.appendScriptFooterIfNecessary(context().request(), response);
		return response;
	}

	public boolean isUploadStarted() {
		boolean uploadStarted;
		if (hasBinding("uploadStarted")) {
			uploadStarted = ERXComponentUtilities.booleanValueForBinding(this, "uploadStarted");
		}
		else {
			AjaxUploadProgress progress = uploadProgress();
			if (progress != null && progress.shouldReset()) {
				_uploadStarted = false;
				setValueForBinding(Boolean.FALSE, "uploadStarted");
			}
			uploadStarted = _uploadStarted;
		}
		return uploadStarted;
	}

	public void uploadFinished() {
		valueForBinding("finishedAction");
	}

	public WOActionResults uploadCanceled() {
		uploadFinished();
		WOActionResults results = (WOActionResults) valueForBinding("canceledAction");
		return results;
	}

	public WOActionResults uploadSucceeded() {
		AjaxUploadProgress progress = uploadProgress();
		try {
			boolean deleteFile = true;
			if (hasBinding("filePath")) {
				setValueForBinding(progress.fileName(), "filePath");
			}

			if (hasBinding("data")) {
				NSData data = new NSData(progress.tempFile().toURI().toURL());
				setValueForBinding(data, "data");
			}
			
			if (hasBinding("mimeType")) {
				String contentType = progress.contentType();
				if (contentType != null) {
					setValueForBinding(contentType, "mimeType");
				}
			}

			if (hasBinding("inputStream")) {
				setValueForBinding(new FileInputStream(progress.tempFile()), "inputStream");
				deleteFile = false;
			}

			if (hasBinding("outputStream")) {
				OutputStream outputStream = (OutputStream) valueForBinding("outputStream");
				if (outputStream != null) {
					ERXFileUtilities.writeInputStreamToOutputStream(new FileInputStream(progress.tempFile()), true, outputStream, true);
				}
			}

			String finalFilePath = progress.tempFile().getAbsolutePath();
			if (hasBinding("streamToFilePath")) {
				File streamToFile = new File((String) valueForBinding("streamToFilePath"));
				boolean renamedFile;
				boolean renameFile;
				if (streamToFile.exists()) {
					boolean overwrite = ERXComponentUtilities.booleanValueForBinding(this, "overwrite");
					if (streamToFile.isDirectory()) {
						File parentDir = streamToFile;
						String fileName = ERXFileUtilities.fileNameFromBrowserSubmittedPath(progress.fileName());
						streamToFile = ERXFileUtilities.reserveUniqueFile(new File(parentDir, fileName), overwrite);
						renameFile = true;
					}
					else {
						renameFile = overwrite;
					}
				}
				else {
					renameFile = true;
				}

				if (renameFile && !streamToFile.isDirectory()) {
					ERXFileUtilities.renameTo(progress.tempFile(), streamToFile);
					renamedFile = true;
				}
				else {
					renamedFile = false;
					progress.setFailure(new Exception("Could not rename file."));
					return uploadFailed();
				}
				
				if (renamedFile) {
					finalFilePath = streamToFile.getAbsolutePath();
				}
				
				deleteFile = false;
			}
			else if (hasBinding("keepTempFile") && deleteFile) {
				deleteFile = !ERXComponentUtilities.booleanValueForBinding(this, "keepTempFile");
			}

			if (deleteFile) {
				progress.dispose();
			}
			else if (hasBinding("finalFilePath")) {
				setValueForBinding(finalFilePath, "finalFilePath");
			}

		}
		catch (Throwable t) {
			t.printStackTrace();
			progress.setFailure(t);
			return uploadFailed();
		}
		finally {
			uploadFinished();
		}
		WOActionResults results = (WOActionResults) valueForBinding("succeededAction");
		return results;
	}

	public WOActionResults uploadFailed() {
		uploadFinished();
		WOActionResults results = (WOActionResults) valueForBinding("failedAction");
		return results;
	}

	public String srcUrl() {
		return context()._directActionURL("ERXDirectAction/empty", null, ERXRequest.isRequestSecure(context().request()), 0, false);
	}
}
