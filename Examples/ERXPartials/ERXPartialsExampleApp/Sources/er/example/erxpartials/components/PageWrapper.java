package er.example.erxpartials.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;

public class PageWrapper extends WOComponent {

	private static final long serialVersionUID = 1L;

	public PageWrapper(WOContext aContext) {
        super(aContext);
    }
    public D2WContext d2wContext() {
    	if (context().page() instanceof D2WPage) {
			D2WPage d2wPage = (D2WPage) context().page();
			return d2wPage.d2wContext();
		}
    	return null;
    }
    
	public String bodyClass()
	{
		String result = null;
		String pageConfig = (String) d2wContext().valueForKey("pageConfiguration");
		if (pageConfig != null && pageConfig.length() > 0)
		{
			result = pageConfig + "Body";
		}
		return result;
	}
}
