package er.diva.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;


/**
 * Page/dialog template for modalbox confirmations.
 * 
 * FIXME: for now the ok button redirects to the default page. This needs to be changed
 * 
 * @author mendis
 *
 */
public class ERDIVInspectConfirmPage extends ERDIVInspectPage {
    public ERDIVInspectConfirmPage(WOContext context) {
        super(context);
    }
    
    // actions
    public WOComponent okAction() {
    	WOComponent nextPage = context().page();
    	if (nextPage instanceof ERDIVEditPage) nextPage.takeValueForKey(Boolean.FALSE, "showConfirmationPanel");
    	return nextPage;
    }
}