package er.jquery.widgets;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;

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
	
	// R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {    
    	// open tag with id
    	response.appendContentString("<" + elementName());
    	response._appendTagAttributeAndValue("id", id(), false);
    	response.appendContentCharacter('>');
    	
    	super.appendToResponse(response, context);
    	
    	// append javascript and close tag
    	response.appendContentString("<noscript><p>Please enable JavaScript to use file uploader.</p></noscript></div>\n");
    	response.appendContentString("<script type=\"text/javascript\">" + script() + "</script>");
    }
}