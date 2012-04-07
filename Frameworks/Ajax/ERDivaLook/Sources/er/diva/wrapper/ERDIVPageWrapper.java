package er.diva.wrapper;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSDictionary;

public abstract class ERDIVPageWrapper extends WOComponent {
	public NSDictionary<String, String> errorMessages;
	
    public ERDIVPageWrapper(WOContext context) {
        super(context);
    }
    
    public D2WContext d2wContext() {
    	if (context().page() instanceof D2WPage) {
			D2WPage d2wPage = (D2WPage) context().page();
			return d2wPage.d2wContext();
		}
    	return null;
    }
}