package er.modern.movies.test.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;

import er.extensions.components.ERXComponent;

public class PageWrapper extends ERXComponent {
	
    public PageWrapper(WOContext context) {
        super(context);
    }
    
    public D2WContext d2wContext() {
    	if (context().page() instanceof D2WPage) {
			D2WPage d2wPage = (D2WPage) context().page();
			return d2wPage.d2wContext();
		}
    	return null;
    }

	public String bodyClass() {
		String result = null;
		String pageConfig = (String)d2wContext().valueForKey("pageConfiguration");
		if (pageConfig != null && pageConfig.length() > 0) {
			result = pageConfig + "Body";
		}
		return result;
	}
}