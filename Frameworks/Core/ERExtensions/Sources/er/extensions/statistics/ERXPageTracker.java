package er.extensions.statistics;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;

import er.extensions.appserver.ERXComponentActionRedirector;
import er.extensions.components.ERXStatelessComponent;


/**
 * Integration into Google analytics. Supports custom url values and server names via bindings. 
 * Beware that this is a severe privacy invasion against your users...
 * 
 * <ul><li>binding - key Google tracking key. If omitted or null, leaves out the content alltogether.</li>
 * <li>href - url to give to Google. If omitted or null, first looks if context.page is an {@link ERXComponentActionRedirector.Restorable} or 
 * if not uses the normal URL which will not make a lot of sense in case of a component url.</li>
 * <li>omitQuotes - if set, does not quote the href value. This is useful if you want to use javascript values.</li>
 * <li>server -  server to give to Google. If omitted or null, leaves the default.</li></ul>
 * 
 * @author ak, privacy invader extra-ordinaire
 */
public class ERXPageTracker extends ERXStatelessComponent {
	
    public ERXPageTracker(WOContext context) {
        super(context);
    }
    
    public String href() {
    	String result = stringValueForBinding("href");
    	if(result == null) {
    		if (context().page() instanceof ERXComponentActionRedirector.Restorable) {
				ERXComponentActionRedirector.Restorable restorable = (ERXComponentActionRedirector.Restorable) context().page();
				result = restorable.urlForCurrentState();
			}
    	}
		if(result != null && !booleanValueForBinding("omitQuotes", false)) {
			result = "\"" + WOMessage.stringByEscapingHTMLString(result) + "\"";
		}
    	return result;
    }
    
    public String server() {
    	String result = stringValueForBinding("server");
    	if(result != null) {
    		result = "\"" + result + "\"";
    	}
        return result;
    }
    
}
