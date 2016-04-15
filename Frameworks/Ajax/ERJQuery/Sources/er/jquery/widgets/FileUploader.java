package er.jquery.widgets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Encapsulation of @see <a href="http://valums.com/ajax-upload/">Ajax Upload</a>
 * @binding			onChange
 * @binding			onSubmit
 * @binding			onComplete
 * @binding			allowedExtensions           E.g: "['jpg', 'jpeg', 'png', 'gif']"
 * 
 * @property		useUnobtrusively			For Unobtrusive Javascript programming. Default it is ON.
 * @property 		WOFileUpload.sizeLimit		Max file upload size permitted
 * 
 * @author mendis
 * 
 * NOTES:
 * 
 * 1. The progress indicator doesn't work properly with WODefaultAdaptor. If you want a progress % indicator, you may
 * need to use an alternative WOAdaptor. e.g: ERWOAdaptor
 * 
 * 2. Use of ERXSession breaks IE6-8 compatibility. Use WOSession instead.
 *
 */
public abstract class FileUploader extends WOComponent {
	private static final Logger log = LoggerFactory.getLogger(FileUploader.class);
	
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.jquery.useUnobtrusively", true);

	public FileUploader(WOContext aContext) {
		super(aContext);
	}
	
	private RuntimeException exception;
	
	/*
	 * Bindings/API
	 */
	public static interface Bindings {
		public static final String id = "id";
		public static final String onChange = "onChange";
		public static final String onSubmit = "onSubmit";
		public static final String onComplete = "onComplete";
		public static final String filePath = "filePath";
		public static final String data = "data";
		public static final String inputStream = "inputStream";
		public static final String outputStream = "outputStream";
		public static final String finalFilePath = "finalFilePath";
		public static final String streamToFilePath = "streamToFilePath";
		public static final String allowedExtensions = "allowedExtensions";
	}
	
	/*
	 * Form keys
	 */
	public static interface FormKeys {
		public static final String qqfile = "qqfile";
		public static final String qqfilename = "qqfile.filename";
		public static final String _forceFormSubmitted = "_forceFormSubmitted";
	}
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	// accessors
	public String id() {
		return _id() == null ? "fu" + ERXStringUtilities.safeIdentifierName(context().elementID()) : _id();
	}
	
	private String _id() {
		return (String) valueForBinding(Bindings.id);
	}
	
	private String _script() {
		return "new qq.FileUploader({" + options() + "});";
	}
	
	public String script() {
		return isAjax() ? _script() : "jQuery(function(){ " + _script() + " });";
	}
	
	private boolean isAjax() {
		return ERXAjaxApplication.isAjaxRequest(context().request());
	}
	
	private String finalFilePath() {
		return (String) valueForBinding(Bindings.finalFilePath);
	}
	
	public static Integer maxFileSize() {
		 return Integer.getInteger("WOFileUpload.sizeLimit", 1024*1024*100);
	}
	
    /*
     * An array of options for File Uploader
     */
    protected NSArray<String> _options() {
    	NSMutableArray<String> _options = new NSMutableArray<String>("action:'" + href() + "'");
    	
    	// add options
    	_options.add("element: $('#" + id() + "')[0]");
    	_options.add("params: { " + FormKeys._forceFormSubmitted + ": '" + id() + "'}"); 	// TODO params binding
    	_options.add("sizeLimit: " + maxFileSize());
    	if (hasBinding(Bindings.onChange)) _options.add("onChange:" + valueForBinding(Bindings.onChange));
    	if (hasBinding(Bindings.onComplete)) _options.add("onComplete:" + valueForBinding(Bindings.onComplete));
    	if (hasBinding(Bindings.onSubmit)) _options.add("onSubmit:" + valueForBinding(Bindings.onSubmit));
    	if (hasBinding(Bindings.allowedExtensions)) _options.add("allowedExtensions:" + valueForBinding(Bindings.allowedExtensions));
    	
    	return _options.immutableClone();
    }
    
    public String options() {
    	return _options().componentsJoinedByString(", ");
    }
	
	protected void setFilePath(String aPath) {
		setValueForBinding(aPath, Bindings.filePath);
	}
	
	protected void setData(NSData data) {
		setValueForBinding(data, Bindings.data);
	}
	
	private String href() {
		return context().componentActionURL(application().ajaxRequestHandlerKey());
	}
	
	// R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	if (!useUnobtrusively) {
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jquery-1.4.2.min.js");
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "fileuploader.js");
    	}
    }
    
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context) {
    	if (context.senderID().equals(context.elementID())) {
        	WOResponse response = WOApplication.application().createResponseInContext(context);

    		if (exception != null) {
    			response.appendContentString("{\"error\":" + exception.getMessage() + "}");
    		} else {
    			response.appendContentString("{\"success\":true}");
    		} return response;
    	} else return super.invokeAction(request, context);
    }
    
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
        
		if (context.senderID().equals(context.elementID())) {
			String aFileName;
			InputStream anInputStream;
			
	        if (request.formValueForKey(FormKeys.qqfilename) != null) {
	        	aFileName = (String) request.formValueForKey(FormKeys.qqfilename);
	        	NSData data = (NSData) request.formValueForKey(FormKeys.qqfile);
	        	anInputStream = data.stream();
	        } else if (request.formValueForKey(FormKeys.qqfile) != null) {
				aFileName = (String) request.formValueForKey(FormKeys.qqfile);
				anInputStream = (request.contentInputStream() != null) ? request.contentInputStream() : request.content().stream();
	        } else {
	        	log.error("Unable to obtain filename from form values: {}", request.formValueKeys());
	        	return;
	        }

			// filepath
			if (hasBinding(Bindings.filePath)) {
				setFilePath(aFileName);
			}

			// file data
			if (hasBinding(Bindings.data)) {
				if (hasBinding(Bindings.filePath))
					setData(request.content());

			} else {
				if (aFileName != null && aFileName.length() > 0) {
					if (hasBinding(Bindings.inputStream)) {
						setValueForBinding(anInputStream, Bindings.inputStream);
					} else {
						String localFilePath = null;
						File tempFile = null;
						if (hasBinding(Bindings.outputStream)) {
							OutputStream anOutputStream = (OutputStream) valueForBinding(Bindings.outputStream);
							try {
								ERXFileUtilities.writeInputStreamToOutputStream(anInputStream, anOutputStream);
							} catch (IOException e) {
								exception = new RuntimeException("Couldn't write input stream to output stream: " + e);
								throw exception;
							} finally {
								try { anOutputStream.close(); } catch (IOException e) {}
								try { anInputStream.close(); } catch (IOException e) {}
							}
						} else {
							if (hasBinding(Bindings.finalFilePath)) {
								localFilePath = finalFilePath();
								setValueForBinding(null, Bindings.finalFilePath);
							}
							try {
								tempFile = ERXFileUtilities.writeInputStreamToTempFile(anInputStream, context.session().sessionID(), ".tmp");
							} catch (IOException e) {
								exception = new RuntimeException("Couldn't write input stream to temp file: " + e);
								throw exception;
							} finally {
								try { anInputStream.close(); } catch (IOException e) {}
							}
						}

						if (hasBinding(Bindings.streamToFilePath)) {
							if (localFilePath == null) localFilePath = (String) valueForBinding(Bindings.streamToFilePath);
							
							try {
								ERXFileUtilities.renameTo(tempFile, new File(localFilePath));
								setValueForBinding(localFilePath, Bindings.finalFilePath);
							} catch (Exception e) {
								setValueForBinding(tempFile.getPath(), Bindings.finalFilePath);
								exception = new RuntimeException("Couldn't rename temp file: " + e);
								throw exception;
							}
						}
					}
				} else {
					if (hasBinding(Bindings.inputStream))
						setValueForBinding(null, Bindings.inputStream);
					if (hasBinding(Bindings.finalFilePath))
						setValueForBinding(null, Bindings.finalFilePath);
					byte buffer[] = new byte[128];
					try {
						while (anInputStream.read(buffer) != -1) ;
					}
					catch (IOException e) {
						exception = new RuntimeException("Error skipping empty file upload: " + e);
						throw exception;
					} finally {
						try { anInputStream.close(); } catch (IOException e) {}
					}
				}
			}

		}
	}
}
