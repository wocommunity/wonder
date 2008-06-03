package er.directtoweb;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WHead;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSSelector;

/**
 * Better D2WHead component which provides the title as a human readable name and 
 * allows for component content. Drop it in your page wrapper.
 *
 * @author ak
 */
public class ERD2WHead extends D2WHead {
    static final Logger log = Logger.getLogger(ERD2WHead.class);

    protected static D2WContext _d2wContext;

    public ERD2WHead(WOContext context) {
        super(context);
    }

    public String displayNameForPageConfiguration() {
        NSSelector sel = new NSSelector("d2wContext");
        if(sel.implementedByObject(context().page())) {
            D2WContext context = (D2WContext) context().page().valueForKey("d2wContext");
            return (String) context.valueForKey("displayNameForPageConfiguration");
        } else {
            if(_d2wContext == null)
                _d2wContext = new D2WContext((WOSession)null);
            synchronized(_d2wContext) {
                _d2wContext.setDynamicPage(ERD2WFactory.pageConfigurationFromPage(context().page()));
                return (String)_d2wContext.valueForKey("displayNameForPageConfiguration");
            }
        }
    }
}
