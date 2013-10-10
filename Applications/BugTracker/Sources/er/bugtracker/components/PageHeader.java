package er.bugtracker.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EODataSource;

import er.directtoweb.components.ERDCustomComponent;
import er.directtoweb.pages.ERD2WPage;

/**
 * Simple display of some info on the top of the page.
 * @author ak
 *
 */
public class PageHeader extends ERDCustomComponent {

    public PageHeader(WOContext context) {
        super(context);
    }
    
    public String explainationKey() {
        return valueForBinding("pageConfiguration") + ".explaination";
    }
    
    public String titleKey() {
        return valueForBinding("pageConfiguration") + ".title";
    }
    
    public String title() {
        return (String) valueForBinding("displayNameForPageConfiguration");
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    private Object tolerantValueForKeyPath(String key) {
        if (parent() instanceof ERD2WPage) {
            ERD2WPage page = (ERD2WPage) parent();
            return parent().valueForKeyPath(key);
        }        
        return null;
    }

    public EODataSource dataSource() {
        return (EODataSource) tolerantValueForKeyPath("dataSource");
    }

    public WODisplayGroup displayGroup() {
        return (WODisplayGroup) tolerantValueForKeyPath("displayGroup");
    }
}
