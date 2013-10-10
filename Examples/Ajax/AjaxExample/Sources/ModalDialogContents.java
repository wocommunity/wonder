import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSLog;

import er.ajax.AjaxModalDialog;

public class ModalDialogContents extends WOComponent {
	
	boolean isSecondConfirmation = false;
	
    public ModalDialogContents(WOContext context) {
        super(context);
    }
    
    /** 
     * Ajax method that is called when deletion is confirmed in the Ajax Dialog
     * 
     * @return null
     */
    public WOActionResults deleteIt() {
    	NSLog.out.appendln(isSecondConfirmation ? "ModalDialogContents deleteIt called will delete" : "ModalDialogContents deleteIt called will reconfirm");
    	isSecondConfirmation = ! isSecondConfirmation;
    	if (isSecondConfirmation) {
        	AjaxModalDialog.update(context());
    	}
    	else  {
    		AjaxModalDialog.close(context());
    	}

    	return null;
    }
    
    public String confirmationMessage() {
    	return isSecondConfirmation ? "Are you really, really, really sure you want to delete this?" : "Are you sure you want to delete this?";
    }
}
