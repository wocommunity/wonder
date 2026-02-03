package er.r2d2w.components.misc;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;

import er.directtoweb.components.ERD2WStatelessCustomComponentWithArgs;

public class R2DSVGUseImage extends ERD2WStatelessCustomComponentWithArgs {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DSVGUseImage(WOContext context) {
        super(context);
    }
    
    public String href() {
    	return (String)valueForBinding("imageHref");
    }
    
    public String filename() {
    	return (String)valueForBinding("imageFilename");
    }
    
    public String framework() {
    	String framework = (String)valueForBinding("imageFramework");
    	return framework == null?"app":framework;
    }
    
    public String imageID() {
    	return (String)valueForBinding("imageID");
    }

	public String imageHref() {
		String url = href();
		if(url == null && filename() != null) {
			WOResourceManager rm = WOApplication.application().resourceManager();
			url = rm.urlForResourceNamed(filename(), framework(), null, context().request());
		}
		if(url == null) {
			url = "";
		}
		return url + "#" + imageID();
	}
}