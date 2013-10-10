package er.jquery.widgets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMultipartIterator;
import com.webobjects.appserver.WOMultipartIterator.WOFormData;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Encapsulation of @see <a href="http://github.com/valums/ajax-upload">Ajax Upload</a>
 * @binding			name				
 * @binding			onChange			
 * @binding			onSubmit			
 * @binding			onComplete			
 * 
 * @property		useUnobtrusively			For Unobtrusive Javascript programming. Default it is ON.
 * 
 * @author mendis
 *
 */
public abstract class AjaxUpload extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.jquery.useUnobtrusively", true);

	public AjaxUpload(WOContext aContext) {
		super(aContext);
	}
	
	/*
	 * Bindings/API
	 */
	public static interface Bindings {
		public static final String id = "id";
		public static final String name = "name";
		public static final String onChange = "onChange";
		public static final String onSubmit = "onSubmit";
		public static final String onComplete = "onComplete";
		public static final String filePath = "filePath";
		public static final String data = "data";
		public static final String mimeType = "mimeType";
		public static final String inputStream = "inputStream";
		public static final String outputStream = "outputStream";
		public static final String finalFilePath = "finalFilePath";
		public static final String streamToFilePath = "streamToFilePath";

	}
	
	/*
	 * Headers of multipart form
	 */
	public static interface Headers {
		public static final String filename = "filename";
		public static final String contentType = "content-type";
		public static final String name = "name";
	}
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	// accessors	
	public String id() {
		return _id() == null ? "au" + ERXStringUtilities.safeIdentifierName(elementID()) : _id();
	}
	
	private String elementID;
	
	private String elementID() {
		if (elementID == null) elementID = context().elementID();
		return elementID;
	}
	
	private String _id() {
		return (String) valueForBinding(Bindings.id);
	}
	
	private String _script() {
		return "new AjaxUpload('" + id() + "', {" + options() + "});";
	}
	
	public String script() {
		return isAjax() ? _script() : "$(document).ready(function() { " + _script() + " });";
	}
	
	private boolean isAjax() {
		return ERXAjaxApplication.isAjaxRequest(context().request());
	}
	
    /*
     * An array of options for Ajax.Updater
     */
    protected NSArray<String> _options() {
    	NSMutableArray _options = new NSMutableArray("action:'" + href() + "'");
    	
    	// add options
    	_options.add("name:'" + uploadName() + "'");
    	if (hasBinding(Bindings.onChange)) _options.add("onChange:" + valueForBinding(Bindings.onChange));
    	if (hasBinding(Bindings.onComplete)) _options.add("onComplete:" + valueForBinding(Bindings.onComplete));
    	if (hasBinding(Bindings.onSubmit)) _options.add("onChange:" + valueForBinding(Bindings.onSubmit));

    	return _options.immutableClone();
    }
    
    public String options() {
    	return _options().componentsJoinedByString(", ");
    }
	
	private String _uploadName() {
		return (String) valueForBinding(Bindings.name);
	}
	
	public String uploadName() {
		return _uploadName() != null ? _uploadName() : "userfile";	// Default
	}
	
	protected void setFilePath(String aPath) {
		setValueForBinding(aPath, Bindings.filePath);
	}
	
	protected void setData(NSData data) {
		setValueForBinding(data, Bindings.data);
	}
	
	protected void setMimeType(String aType) {
		setValueForBinding(aType, Bindings.mimeType);
	}
	
	/*
	 * NOTE: this is a standard WO component action url.
	 * The file upload won't work as an ajax request.
	 * 
	 */
	private String href() {
		return context().componentActionURL();
	}
	
	// R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	if (!useUnobtrusively) {
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jquery-1.4.2.min.js");
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "ajaxupload.js");
    	}
    }
    
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);

		if (request.formValueForKey(uploadName()) != null) {
			// filepath
			if (hasBinding(Bindings.filePath)) {
				setFilePath((String) request.formValueForKey(uploadName() + ".filename"));
			}

			// file data
			if (hasBinding(Bindings.data)) {
				if (hasBinding(Bindings.filePath)) {
					NSArray aValue = request.formValuesForKey(uploadName());

					if (aValue != null) {
						NSData data = null;
						try {
							data = (NSData) aValue.objectAtIndex(0);
						} catch (ClassCastException e) {
							throw new ClassCastException("AjaxUploadButton: Value in request was of type '" + aValue.objectAtIndex(0).getClass().getName() + "' instead of NSData. Verify that the WOForm's 'enctype' binding is set to 'multipart/form-data'");
						}
						setData(data);
					}

					// mimetype
					if (hasBinding(Bindings.mimeType)) {
						setMimeType((String) request.formValueForKey(uploadName() + ".mimetype"));
					}
				}
			} else {
				// multipart data
				WOMultipartIterator multipartIterator = request.multipartIterator();
				WOFormData nextFormData = multipartIterator.nextFormData();
				NSDictionary<Object, String> contentDispositionHeaders;
				do {
					if (nextFormData == null)
						break;
					contentDispositionHeaders = nextFormData.contentDispositionHeaders();
					Object _name = contentDispositionHeaders.objectForKey(Headers.name);
					if (uploadName().equals(_name))
						break;
					nextFormData = multipartIterator.nextFormData();
				} while(true);

				if(nextFormData == null)
					throw new IllegalStateException("AjaxUploadButton: No form data left for WOFileUpload!");

				contentDispositionHeaders = nextFormData.contentDispositionHeaders();
				String aFileName = null;

				if(hasBinding(Bindings.filePath)) {
					aFileName = (String) contentDispositionHeaders.valueForKey(Headers.filename);
					setFilePath(aFileName);
				}

				if (hasBinding(Bindings.mimeType)) {
					setMimeType((String) contentDispositionHeaders.valueForKey(Headers.contentType));
				}

				InputStream anInputStream = nextFormData.formDataInputStream();
				if(aFileName != null && aFileName.length() > 0) {
					if(hasBinding(Bindings.inputStream)) {
						setValueForBinding(anInputStream, Bindings.inputStream);
					} else {
						String localFilePath = null;
						File tempFile = null;
						if (!hasBinding(Bindings.outputStream)) {
							if (hasBinding(Bindings.finalFilePath))
								setValueForBinding(null, Bindings.finalFilePath);
							try {
								tempFile = ERXFileUtilities.writeInputStreamToTempFile(anInputStream, context.session().sessionID(), ".tmp");
							} catch (IOException e) {
								throw new RuntimeException("Couldn't write input stream to temp file: " + e);
							} finally {
								try { anInputStream.close(); } catch (IOException e) {}
							}
						} else {
							OutputStream anOutputStream = (OutputStream) valueForBinding(Bindings.outputStream);
							try {
								ERXFileUtilities.writeInputStreamToOutputStream(anInputStream, anOutputStream);
							} catch (IOException e) {
								throw new RuntimeException("Couldn't write input stream to output stream: " + e);
							} finally {
								try { anOutputStream.close(); } catch (IOException e) {}
								try { anInputStream.close(); } catch (IOException e) {}
							}
						}

						if (hasBinding(Bindings.streamToFilePath)) {
							localFilePath = (String) valueForBinding(Bindings.streamToFilePath);
							try {
								ERXFileUtilities.renameTo(tempFile, new File(localFilePath));
								setValueForBinding(localFilePath, Bindings.finalFilePath);
							} catch (Exception e) {
								setValueForBinding(tempFile.getPath(), Bindings.finalFilePath);
								throw new RuntimeException("Couldn't rename temp file: " + e);
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
						throw new RuntimeException("Error skipping empty file upload: " + e);
					} finally {
						try { anInputStream.close(); } catch (IOException e) {}
					}
				}
			}
		}
	}
}
