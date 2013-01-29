package er.jquery.widgets;

import com.webobjects.appserver.WOContext;

/**
 * @see AjaxUpload
 * 
 * @author mendis
 *
 *	This version is for uploads not in forms. And can be any element.
 * @see er.prototaculous.widgets.AjaxUploadContainer 	jQuery compatible version
 *
 */
public class AjaxUploadContainer extends AjaxUpload {
    public AjaxUploadContainer(WOContext context) {
        super(context);
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
}