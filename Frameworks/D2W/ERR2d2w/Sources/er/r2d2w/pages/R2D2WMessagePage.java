package er.r2d2w.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.pages.ERD2WMessagePage;

public class R2D2WMessagePage extends ERD2WMessagePage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2D2WMessagePage(WOContext context) {
        super(context);
    }
	
	public NSDictionary<String, String> branch() {
		return (NSDictionary<String, String>)d2wContext().valueForKeyPath("branch");
	}
    
}