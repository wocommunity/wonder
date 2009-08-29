package er.prototaculous.widgets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMultipartIterator;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOMultipartIterator.WOFormData;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXFileUtilities;

/**
 * Encapsulation of http://valums.com/ajax-upload/ implemented like WOFileUpload
 * @see WOFileUpload		API/binding compatible with WOFileUpload. Additionally:
 * 
 * @author mendis
 *
 */
public class AjaxUploadButton extends AjaxUpload {

	public AjaxUploadButton(WOContext aContext) {
		super(aContext);
	}

	// R/R
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
				Map<String, String> contentDispositionHeaders;
				do {
					if (nextFormData == null)
						break;
					contentDispositionHeaders = nextFormData.contentDispositionHeaders();
					Object _name = contentDispositionHeaders.get(Headers.name);
					if (uploadName().equals(_name))
						break;
					nextFormData = multipartIterator.nextFormData();
				} while(true);

				if(nextFormData == null)
					throw new IllegalStateException("AjaxUploadButton: No form data left for WOFileUpload!");

				contentDispositionHeaders = nextFormData.contentDispositionHeaders();
				String aFileName = null;

				if(hasBinding(Bindings.filePath)) {
					aFileName = (String) contentDispositionHeaders.get(Headers.filename);
					setFilePath(aFileName);
				}

				if (hasBinding(Bindings.mimeType)) {
					setMimeType((String) contentDispositionHeaders.get(Headers.contentType));
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
							}
						} else {
							OutputStream anOutputStream = (OutputStream) valueForBinding(Bindings.outputStream);
							try {
								ERXFileUtilities.writeInputStreamToOutputStream(anInputStream, anOutputStream);
							} catch (IOException e) {
								throw new RuntimeException("Couldn't write input stream to output stream: " + e);
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
					}
				}
			}
		}
	}
}
