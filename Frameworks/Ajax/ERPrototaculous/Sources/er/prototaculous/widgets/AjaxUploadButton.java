package er.prototaculous.widgets;

import com.webobjects.appserver.WOContext;

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
}
