package er.directtoweb.components.attachments.dialog._xhtml;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.attachment.model.ERAttachment;

/**
 * Dialog for file upload
 * 
 * @author mendis
 * 
 */
public class EditAttachmentDialog extends WOComponent {
	public D2WPage returnPage;
	public String configurationName;
	public EOEnterpriseObject object;
	public String key;

	public EditAttachmentDialog(WOContext context) {
		super(context);
	}
	
	public ERAttachment attachment() {
		return (ERAttachment) object.valueForKeyPath(key);
	}
	  
	public void setAttachment(ERAttachment attachment) {
	   	object.takeValueForKeyPath(attachment, key);
	}

	// actions
	public WOActionResults uploadFinished() {
		System.out.println("FileUploadDialog.uploadFinished: FINISHED!");
		
		// reset the d2w context
		//D2WContext d2wContext = new D2WContext(returnPage.d2wContext());
		//d2wContext.setPropertyKey(null);
		//returnPage.setLocalContext(d2wContext);
		
		return returnPage;
	}

}