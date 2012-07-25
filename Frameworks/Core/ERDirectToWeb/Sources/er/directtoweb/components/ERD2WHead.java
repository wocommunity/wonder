package er.directtoweb.components;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WHead;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.ERD2WFactory;

/**
 * Better D2WHead component which provides the title as a human readable name and 
 * allows for component content. Drop it in your page wrapper.
 *
 * @author ak
 */
public class ERD2WHead extends D2WHead {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
                _d2wContext = ERD2WContext.newContext();
            synchronized(_d2wContext) {
                _d2wContext.setDynamicPage(ERD2WFactory.pageConfigurationFromPage(context().page()));
                return (String)_d2wContext.valueForKey("displayNameForPageConfiguration");
            }
        }
    }
}
