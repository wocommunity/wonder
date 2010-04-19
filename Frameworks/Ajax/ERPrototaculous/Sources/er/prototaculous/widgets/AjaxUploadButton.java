package er.prototaculous.widgets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
