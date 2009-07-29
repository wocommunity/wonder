package er.prototaculous.widgets;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

/**
 * @see AjaxUpload
 * 
 * @author mendis
 *
 *	This version is for uploads not in forms. And can be any element.
 *
 */
public class AjaxUploadContainer extends AjaxUpload {

	public AjaxUploadContainer(WOContext aContext) {
		super(aContext);
	}

	/*
	 * Bindings/API
	 */
	public static interface Bindings extends AjaxUpload.Bindings {
		public static final String elementName = "elementName";
	}
	
	// accessors
	private String _elementName() {
		return (String) valueForBinding(Bindings.elementName);
	}
	
	public String elementName() {
		return _elementName() != null ? _elementName() : "div";
	}
	
	// R/R
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults invokeAction = super.invokeAction(request, context);
		
		return invokeAction;
	}
}
