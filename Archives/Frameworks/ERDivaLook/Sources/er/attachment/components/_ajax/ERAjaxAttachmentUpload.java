package er.attachment.components._ajax;

import java.io.IOException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSForwardException;

import er.attachment.components.ERAttachmentUpload;

/**
 * ERAttachment component for er.prototaculous.AjaxUpload
 * 
 * @author mendis
 *
 */
public class ERAjaxAttachmentUpload extends ERAttachmentUpload {

	public ERAjaxAttachmentUpload(WOContext context) {
		super(context);
	}
	
	// accessors
	public String uploadName() {
		return hasBinding("name") ? (String) valueForBinding("name") :  "userfile";
	}

	// R/R
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results = super.invokeAction(request, context);
		if (request.formValueForKey(uploadName()) != null) {
			try {
				_uploadSucceeded();
			}
			catch (IOException e) {
				throw new NSForwardException(e, "Failed to process uploaded attachment.");
			}
		} return results;
	}

}
