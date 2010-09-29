package er.jquery.widgets;

import com.webobjects.appserver.WOContext;

/**
 * @see FileUploader
 * 
 * @author mendis
 *
 * A style-able file uploader 
 */
public class FileUploaderContainer extends FileUploader {
    public FileUploaderContainer(WOContext context) {
        super(context);
    }

	/*
	 * Bindings/API
	 */
	public static interface Bindings extends FileUploader.Bindings {
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