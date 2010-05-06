package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * AjaxFlexibleFileUpload is an enhanced file upload component that uses a call to a hidden iFrame to handle a file
 * upload. It is based on the code in AjaxFileUpload but extends it by using Andrew Valums' ajaxupload.js (from 
 * http://valums.com/ajax-upload/). This dynamically creates the iFrame at the end of the current page content freeing
 * this component to be used in or out of a form. The component is fully styleable (including the upload button) and
 * supports upload progress and canceling.
 * 
 * @binding cancelLabel the label for for the cancel button (defaults to "Cancel")
 * @binding startingText the text to display when the progress is starting (defaults "Upload Starting...");
 * @binding selectFileLabel the label for the select file button (defaults to "Select File...")
 * @binding clearLabel the label for the button used to clear a selected file or uploaded file (defaults to "Clear")
 * @binding uploadLabel the label for the Upload button (defaults to "Upload")
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
 * @binding refreshTime the number of milliseconds to wait between refreshes (defaults to 2000)
 * @binding keepTempFile if true, don't delete the temp file that AjaxFileUpload creates
 * @binding uploadFunctionName the upload button will instead be a function with the given name
 * @binding autoSubmit should the upload start immediately after a file is selected (defaults to true)
 * @binding injectDefaultCSS inject the default stylesheet from the Ajax framework (defaults to true);
 * @binding selectFileButtonClass class for the select file button (defaults to "Button ObjButton SelectFileObjButton");
 * @binding uploadButtonClass class for the select file button (defaults to "Button ObjButton UploadFileObjButton")
 * @binding cancelButtonClass class for the select file button (defaults to "Button ObjButton CancelUploadObjButton")
 * @binding clearButtonClass class for the select file button (defaults to "Button ObjButton ClearUploadObjButton")
 * 
 * @author dleber
 * @author mschrag
 */
public class AjaxFlexibleFileUpload extends AjaxFileUpload {
	
	protected final Logger log = Logger.getLogger(getClass());
	
	public static interface Keys {
		public static final String name = "name";
		public static final String wosid = "wosid";
		public static final String selectFileLabel = "selectFileLabel";
		public static final String cancelLabel = "cancelLabel";
		public static final String clearLabel = "clearLabel";
		public static final String uploadLabel = "uploadLabel";
		public static final String refreshTime = "refreshTime";
		public static final String autoSubmit = "autoSubmit";
		public static final String startedFunction = "startedFunction";
		public static final String canceledFunction = "canceledFunction";
		public static final String finishedFunction = "finishedFunction";
		public static final String failedFunction = "failedFunction";
		public static final String succeededFunction = "succeededFunction";
		public static final String selectFileButtonClass = "selectFileButtonClass";
		public static final String uploadButtonClass = "uploadButtonClass";
		public static final String cancelButtonClass = "cancelButtonClass";
		public static final String clearButtonClass = "clearButtonClass";
		public static final String injectDefaultCSS = "injectDefaultCSS";
	}
	
	private String _fileName;
	private String _refreshTime;
	private String _clearLabel;
	private String _cancelLabel;
	private String _uploadLabel;
	private String _selectFileLabel;
	private String _selectFileButtonClass;
	private String _uploadButtonClass;
	private String _cancelButtonClass;
	private String _clearButtonClass;
	
	private boolean _clearUpload;
	private boolean _fileChosen;
	private Boolean _autoSubmit;
	
	public boolean testFlag = false;
	public enum UploadState { DORMANT, STARTED, INPROGRESS, CANCELED, FAILED, SUCCEEDED, FINISHED }
	public UploadState state = UploadState.DORMANT;
	
    public AjaxFlexibleFileUpload(WOContext context) {
        super(context);
    }
	
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	if (ERXComponentUtilities.booleanValueForBinding(this, Keys.injectDefaultCSS, true)) {
    		AjaxUtils.addStylesheetResourceInHead(context, response, "default_ajaxupload.css");
    	}
    	AjaxUtils.addScriptResourceInHead(context, response, "ajaxupload.js");
    	AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
    }
    
    // AJAX UPLOAD INIT
    
    /**
     * Generates the script to initialize a new AjaxUpload JS object
     * 
     * @return script to initialize a new AjaxUpload JS object
     */
	public String ajaxUploadScript() {
		String result = "AFU.create('" + id() + "', '" + uploadButtonId() + "', {" + ajaxUploadOptions() + "});";
		if (log.isDebugEnabled()) log.debug("AFU Create Script: " + result);
		NSLog.out.appendln("AFU Create Script: " + result);
		return result;
	}
	
	/**
	 * Builds the array of required additional AjaxUpload data items (wosid, id).
	 * 
	 * @return array of required additional AjaxUpload data items (wosid, id).
	 */
	protected NSArray<String> _ajaxUploadData() {
		NSMutableArray<String> _data = new NSMutableArray<String>("wosid:'" + this.session().sessionID() + "'");
		
		_data.addObject("id:'" + id() + "'");
		
		return _data.immutableClone();
	}
	
	/**
	 * Returns a comma separated string of AjaxUpload data items.
	 * 
	 * @return comma separated string of AjaxUpload data items.
	 */
	public String ajaxUploadData() {
		return _ajaxUploadData().componentsJoinedByString(", ");
	}
	
	/**
	 * Builds the array of AjaxUpload options
	 * 
	 * @return array of AjaxUpload options
	 */
    protected NSArray<String> _ajaxUploadOptions() {
    	NSMutableArray<String> _options = new NSMutableArray<String>("action:'" + uploadUrl() + "'");
    	
    	// add options
    	_options.addObject("data:{" + ajaxUploadData() + "}");
    	_options.addObject("name:'" + uploadName() + "'");
    	_options.add("iframeId:'"+ iframeId() +"'");
    	if ( !autoSubmit().booleanValue() ) {
    		_options.add("onChange:" + onChangeFunction());
    		_options.add("autoSubmit:false");
    	}
    	_options.add("onSubmit:" + onSubmitFunction());
    	_options.add("onComplete:" + onCompleteFunction());
    	return _options.immutableClone();
    }
    
    /**
     * Returns a comma separated string of AjaxUpload options.
     * 
     * @return comma separated string of AjaxUpload options.
     */
    public String ajaxUploadOptions() {
    	return _ajaxUploadOptions().componentsJoinedByString(", ");
    }
	
	// INLINE JS FUNCTIONS
	
	/**
	 * JS Function string to start the inner update container
	 * 
	 * @return string to start the inner update container
	 */
	public String startFunction() {
		return innerUpdateContainerId() + "PeriodicalUpdater.start();";
	}
	
	/**
	 * JS Function string to stop the inner update container
	 * 
	 * @return string to stop the inner update container
	 */
	public String stopFunction() {
		return innerUpdateContainerId() + "Stop();";
	}
	
	/**
	 * JS Function called when the AjaxUpload registers a change
	 * 
	 * @return string JS Function called when the AjaxUpload registers a change
	 */
	public String onChangeFunction() {
		String result = "function(file, extension) { " + innerUpdateContainerId() + "Update(); }";
		return result;
	}
	
	/**
	 * JS Function called when the AjaxUploader is completes.
	 * 
	 * @return string JS Function called when the AjaxUploader is completes.
	 */
	public String onCompleteFunction() {
		String result = "function(file, extension){ " + innerUpdateContainerId() + "Stop(); " + innerUpdateContainerId() + "Update(); }";
		return result;
	}
	
	/**
	 * JS Function called when the AjaxUploader submits
	 * 
	 * @return string JS Function called when the AjaxUploader submits
	 */
	public String onSubmitFunction() {
		String result = "function(file, extension){ " + startFunction() + " }";
		return result;
	}
	
	/**
	 * JS Function called when the inner container refreshes
	 * 
	 * @return string JS Function called when the inner container refreshes
	 */
	public String innerContainerRefreshFunction() {
		String additionalFunction = null;
		String finalFunction = null;
		switch (state) {
			case DORMANT:
				break;
			case STARTED: 
				additionalFunction = (String)this.valueForBinding(Keys.startedFunction);
				break;
			case INPROGRESS:
				break;
			case CANCELED: 
				// FIXME I don't actually think this case ever occurs here
				// rather it happens on the outerContainerRefresh below
				additionalFunction = (String)this.valueForBinding(Keys.canceledFunction);
				finalFunction = (String)this.valueForBinding(Keys.finishedFunction);
				state = UploadState.DORMANT;
				break;
			case FAILED:
				additionalFunction = (String)this.valueForBinding(Keys.failedFunction);
				finalFunction = (String)this.valueForBinding(Keys.finishedFunction);
				state = UploadState.DORMANT;
				break;
			case SUCCEEDED:
				additionalFunction = (String)this.valueForBinding(Keys.succeededFunction);
				finalFunction = (String)this.valueForBinding(Keys.finishedFunction);
				state = UploadState.DORMANT;
				break;
			case FINISHED:
				break;
		}
		String result = "AFU.progressUpdate( '" + id() + "', '" + fileNameId() + "', " + additionalFunction + ", " + finalFunction + ")";
		if (log.isDebugEnabled()) log.debug("Function: " + result);
		return result;
	}
	
	/**
	 * JS Function called when the outer container refreshes
	 * 
	 * @return string JS Function called when the outer container refreshes
	 */
	public String outerContainerRefreshCompleteFunction() {
		String additionalFunction = null;
		String finalFunction = null;
		switch (state) {
			case CANCELED:
				additionalFunction = (String)this.valueForBinding(Keys.canceledFunction);
				finalFunction = (String)this.valueForBinding(Keys.finishedFunction);
				state = UploadState.DORMANT;
				break;
		}
		String result = "AFU.executeCallbacks( '" + id() + "', " + additionalFunction + ", " + finalFunction + ")";
		if (log.isDebugEnabled()) log.debug("Function: " + result);
		return result;
	}
	
	/**
	 * JS Function called by the manual submit button.
	 * 
	 * @return string JS Function called by the manual submit button.
	 */
	public String submitUploadFunction() {
		String result = "AFU.submit( '" + id() + "', '" + innerUpdateContainerId() + "')";
		return result;
	}
	
	/**
	 * JS Function string to cancel the iframe upload (by changing it's src url).
	 * 
	 * @return string to cancel the iframe upload (by changing it's src url).
	 */
	public String cancelFunction() {
		String script = "AFU.cancelIFrame('" + iframeId() + "', '" + cancelUrl() + "')";
		return script;
	}
	
	// AJAX IDS
	
	/**
	 * Unique identifier for the inner update container
	 * 
	 * @return identifier for the inner update container
	 */
	public String innerUpdateContainerId() {
		return "AFUIC" + id();
	}
	
	/**
	 * Unique identifier for the outer update container 
	 * 
	 * @return identifier for the outer update container 
	 */
	public String outerUpdateContainerId() {
		return "AFUOC" + id();
	}
	
	/**
	 * Unique identifier for the select files button
	 * 
	 * @return identifier for the select files button
	 */
	public String uploadButtonId() {
		return "AFUUB" + id();
	}
	
	/**
	 * Unique identifier for the iframe generated by the AjaxUploader.js
	 * 
	 * @return identifier for the iframe generated by the AjaxUploader.js
	 */
	public String iframeId() {
		return "AFUIF" + id();
	}
	
	/**
	 * Unique identifier for the fileName container
	 * 
	 * @return identifier for the fileName container
	 */
	public String fileNameId() {
		return "AFUFN" + id();
	}
	
	/**
	 * Unique identifier for the upload name
	 * 
	 * @return identifier for the upload name
	 */
	public String uploadName() {
		return "AFUUN" + id();
	}
	
	// IFRAME URLS
	
	/**
	 * Returns a closeHTTPSession DA action URL passed to the iframe to cancel the client-side upload
	 * 
	 * @return url sent to the iframe to cancel
	 */
	@SuppressWarnings("unchecked")
	public String cancelUrl() {
		NSDictionary queryParams = new NSDictionary(Boolean.FALSE, Keys.wosid);
		String url = ERXWOContext._directActionURL(context(), "ERXDirectAction/closeHTTPSession", queryParams, ERXRequest.isRequestSecure(context().request()));
		if (log.isDebugEnabled()) log.debug("URL: " + url);
		return url;
	}
	
	// ACTIONS
	
	/**
	 * Action called when the either update container refreshes
	 * 
	 * @return results of action
	 */
	public WOActionResults containerRefreshed() {
		if (log.isDebugEnabled()) log.debug("** START ***");
		WOActionResults result = null;
		AjaxUploadProgress progress = this.uploadProgress();
		if (_fileName == null && progress != null) {
//			NSLog.out.appendln("ERMAjaxFileUpload.containerRefreshed: " + _fileName);
			_fileName = progress.fileName();
		}
		if (progress == null) {
			if (!_clearUpload && (autoSubmit().booleanValue() || _fileChosen)) {
				if (log.isDebugEnabled()) log.debug("Progress is null: " + autoSubmit());
				_uploadStarted = true;
				_triggerUploadStart = true;
				state = UploadState.STARTED;
			}
			if (!_clearUpload && !autoSubmit().booleanValue()) {
				if (log.isDebugEnabled()) log.debug("Setting file chosen flag - autoSubmit: " + autoSubmit());
				_fileChosen = true;
			}
		}
		if (_clearUpload) {
			_clearUpload = false;
		}
		if (progress != null) {
			_triggerUploadStart = false;
			_fileChosen = false;
			if (log.isDebugEnabled()) {
				log.debug("***HAS PROGRESS***");
				log.debug("Percentage: " + progress.percentage());
				log.debug("isDone: " + progress.isDone());
				log.debug("isStarted: " + progress.isStarted());
				log.debug("isCanceled: " + progress.isCanceled());
				log.debug("isFailed: " + progress.isFailed());
				log.debug("isSucceeded: " + progress.isSucceeded());
			}
			if (progress.isStarted()) {
				state = UploadState.INPROGRESS;
			}
			if (progress.isSucceeded()) {
				state = UploadState.SUCCEEDED;
				result = this.uploadSucceeded();
			}
			if (progress.isFailed()) {
				state = UploadState.FAILED;
				result = this.uploadFailed();
			}
			if (progress.isCanceled()) {
				state = UploadState.CANCELED;
				_fileName = null;
				result  = this.uploadCanceled();
			}
		}
		return result;
	}
	
	/**
	 * Action called by the cancel upload button
	 * 
	 * @return results of action
	 */
	public WOActionResults cancelUpload() {
		if (this.uploadProgress() != null) {
			this.uploadProgress().cancel();
		}
		state = UploadState.CANCELED;
		return null;
	}
	
	/**
	 * Action called by the clear button, resets the uploader for a new file selection
	 * 
	 * @return results of action
	 */
	public WOActionResults clearFileResults() {
		clearUploadProgress();
		_fileName = null;
		_clearUpload = true;
		_fileChosen = false;
		return null;
	}
	
	/**
	 * Hook for add-in action called when an upload is canceled
	 * 
	 * @return results of action
	 */
	@Override
	public WOActionResults uploadCanceled() {
		clearUploadProgress();
		return super.uploadCanceled();
	}
	
	/**
	 * Hook for add-in action called when an upload fails
	 * 
	 * @return results of action
	 */
	@Override
	public WOActionResults uploadFailed() {
		clearUploadProgress();
		return super.uploadFailed();
	}
	
	/**
	 * Helper to reset the uploader and unregister the AjaxProgress object
	 */
	public void clearUploadProgress() {
		if (_progress != null) {
			AjaxUploadProgress.unregisterProgress(session(), _progress);
		}
		_progress = null;
		_uploadStarted = false;
	}
	
	
	// CONTROL POINTS
	
	/**
	 * Is there an upload currently in progress.
	 * 
	 * @return boolean if an upload currently in progress.
	 */
	public boolean showProgressBar() {
		boolean result = _triggerUploadStart || (_progress != null && _progress.isStarted() && !_progress.isCanceled() && !_progress.isDone());
		return result;
	}

	/**
	 * Is there no file?
	 * 
	 * @return boolean true if there no file
	 */
	public boolean showFileSelect() {
		boolean result = (_progress == null || _progress.isCanceled()) && !_triggerUploadStart && !_fileChosen;
		return result;
	}

	/**
	 * Is the upload completed.
	 * 
	 * @return boolean true if the upload is complete
	 */
	public boolean showClearButton() {
		boolean result = _progress != null && _progress.isSucceeded();
		if (!autoSubmit().booleanValue()) {
			result = showUploadButton() || result;
		}
		return result;
	}
	
	/**
	 * Should the component show the upload starting text?
	 * 
	 * @return boolean should show the starting text
	 */
	public boolean showUploadStarting() {
		boolean result = _progress == null && _triggerUploadStart;
		return result;
	}
	
	/**
	 * Has a file been selected, but the upload not started.
	 * 
	 * @return boolean true if a file has been chosen
	 */
	public boolean fileChosen() {
		return _fileChosen;
	}
	
	/**
	 * Controls whether the upload button is displayed (this only occurs when the autoSubmit binding is false)
	 * 
	 * @return boolean controls whether the upload button is displayed
	 */
	public boolean showUploadButton() {
		return _progress == null && fileChosen() && !autoSubmit().booleanValue() && !showProgressBar();
	}
	
	
	// ACCESSORS
	
	/**
	 * Returns the upload progress for this uploader
	 */
	@Override
	public AjaxUploadProgress uploadProgress() {
		if (_progress == null) {
			_progress = (AjaxUploadProgress)AjaxUploadProgress.progress(session(), id()); 
		}
		return _progress;
	}


	/**
	 * Boolean which determines whether the upload should occur automatically after a file is selected.
	 * 
	 * @return value for 'autoSubmit' binding
	 */
	public Boolean autoSubmit() {
		if (_autoSubmit == null) {
			_autoSubmit = ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.autoSubmit), Boolean.TRUE);
		}
		return _autoSubmit;
	}
	
	/**
	 * Returns a style string containing the width of the progress element
	 * 
	 * @return string style applied to the progress element
	 */
	public String progressStyle() {
		String style = null;
		AjaxUploadProgress progress = this.uploadProgress();
		if (progress != null) {
			if (!progress.isSucceeded()) {
				int width = (int)(progress.percentage() * 100);
				style = "width:" + width + "%;";
			} else {
				style = "width:100%;";
			}
		}
		return style;
	}
	
	/**
	 * Returns the css class for the progress bar ('AMFUProgressAmount' or 'AMFUProgressAmount AMFUProgressAmountIndeterminate')
	 * 
	 * @return string class applied to the progress bar
	 */
	public String progressClass() {
		String result = "AFUProgressAmount";
		return showUploadStarting() ? result + " AFUProgressAmountIndeterminate" : result;
	}

	/**
	 * Returns the value for the binding 'refreshTime'
	 * 
	 * The binding takes milliseconds between refreshes, this returns seconds
	 * 
	 * @return value of the 'refreshTime' binding converted to seconds
	 */
	public String refreshTime() {
		if (_refreshTime == null) {
			double tempValue = ERXValueUtilities.doubleValueWithDefault(valueForBinding(Keys.refreshTime), 2000);
			_refreshTime = String.valueOf(tempValue / 1000);
		}
		return _refreshTime;
	}
	
	/**
	 * Accessor for the local fileName
	 * 
	 * @return string value of fileName
	 */
	public String fileName() {
		return _fileName;
	}
	
	/**
	 * Setter for the local fileName
	 * 
	 * @param fn
	 */
	public void setFileName(String fn) {
		_fileName = fn;
	}

	/**
	 * Label for the upload button
	 * 
	 * @return string value for 'uploadLabel' binding
	 */
	public String uploadLabel() {
		if (_uploadLabel == null) {
			_uploadLabel = (String) valueForBinding(Keys.uploadLabel);
			if (_uploadLabel == null) {
				_uploadLabel = "Upload";
			}
		}
		return _uploadLabel;
	}
	
	/**
	 * Label for the clear button
	 * 
	 * @return string value for 'clearLabel' binding
	 */
	public String clearLabel() {
		if (_clearLabel == null) {
			_clearLabel = (String) valueForBinding(Keys.clearLabel);
			if (_clearLabel == null) {
				_clearLabel = "Clear";
			}
		}
		return _clearLabel;
	}

	/**
	 * Label for the cancel button
	 * 
	 * @return string value for 'cancelLabel' binding
	 */
	public String cancelLabel() {
		if (_cancelLabel == null) {
			_cancelLabel = (String) valueForBinding(Keys.cancelLabel);
			if (_cancelLabel == null) {
				_cancelLabel = "Cancel";
			}
		}
		return _cancelLabel;
	}

	/**
	 * Label for the select file button
	 * 
	 * @return string value for 'selectFileLabel' binding
	 */
	public String selectFileLabel() {
		if (_selectFileLabel == null) {
			_selectFileLabel = (String) valueForBinding(Keys.selectFileLabel);
			if (_selectFileLabel == null) {
				_selectFileLabel = "Select File...";
			}
		}
		return _selectFileLabel;
	}
	
	/**
	 * CSS Class for the select file button
	 * 
	 * @return string value for 'selectFileButtonClass' binding
	 */
	public String selectFileButtonClass() {
		if (_selectFileButtonClass == null) {
			_selectFileButtonClass = (String)valueForBinding(Keys.selectFileButtonClass);
			if (_selectFileButtonClass == null) {
				_selectFileButtonClass = "Button ObjButton SelectFileObjButton";
			}
		}
		return _selectFileButtonClass;
	}
	
	/**
	 * CSS Class for the upload file button
	 * 
	 * @return string value for 'uploadButtonClass' binding
	 */
	public String uploadButtonClass() {
		if (_uploadButtonClass == null) {
			_uploadButtonClass = (String)valueForBinding(Keys.uploadButtonClass);
			if (_uploadButtonClass == null) {
				_uploadButtonClass = "Button ObjButton UploadFileObjButton";
			}
		}
		return _uploadButtonClass;
	}
	
	/**
	 * CSS Class for the cancel upload button
	 * 
	 * @return string value for 'cancelButtonClass' binding
	 */
	public String cancelButtonClass() {
		if (_cancelButtonClass == null) {
			_cancelButtonClass = (String)valueForBinding(Keys.cancelButtonClass);
			if (_cancelButtonClass == null) {
				_cancelButtonClass = "Button ObjButton CancelUploadObjButton";
			}
		}
		return _cancelButtonClass;
	}
	
	/**
	 * CSS Class for the clear upload button
	 * 
	 * @return string value for 'clearButtonClass' binding
	 */
	public String clearButtonClass() {
		if (_clearButtonClass == null) {
			_clearButtonClass = (String)valueForBinding(Keys.clearButtonClass);
			if (_clearButtonClass == null) {
				_clearButtonClass = "Button ObjButton ClearUploadObjButton";
			}
		}
		return _clearButtonClass;
	}
}