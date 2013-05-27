package er.ajax;

// http://jquery.com/demo/thickbox/

import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

/**
 * @see AjaxModalContainer
 * @see AjaxModalDialog
 * @deprecated use {@link AjaxModalContainer} or {@link AjaxModalDialog}
 */
@Deprecated
public class AjaxIBoxComponentContent extends AjaxModalContainer {

	public AjaxIBoxComponentContent(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
	}
}
