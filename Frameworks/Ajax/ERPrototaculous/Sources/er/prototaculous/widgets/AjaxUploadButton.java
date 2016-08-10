package er.prototaculous.widgets;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOFileUpload;

/**
 * Encapsulation of http://valums.com/ajax-upload/ implemented like WOFileUpload
 * API/binding compatible with WOFileUpload.
 * @see WOFileUpload
 * 
 * @author mendis
 */
public class AjaxUploadButton extends AjaxUpload {

	public AjaxUploadButton(WOContext aContext) {
		super(aContext);
	}
}
