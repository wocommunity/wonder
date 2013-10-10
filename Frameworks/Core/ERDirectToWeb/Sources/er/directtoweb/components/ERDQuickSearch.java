package er.directtoweb.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOControlUtilities;

/**
 * Simple search for that can be dropped on a page wrapper and pops up a list page.
 * You can either let it create a list page or bind queryDatasource and searchAction and let the parent do the work.
 * @author ak
 *
 */
public class ERDQuickSearch extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERDQuickSearch(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public String searchValue;
    
    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
    	searchValue = defaultValue();
    	super.appendToResponse(r, c);
    }

    public WOComponent searchAction() {
        ListPageInterface lpi = null;
        if(searchValue != null) {
            String listConfigurationName = (String) valueForBinding("listConfigurationName");
            String entityName = (String) valueForBinding("entityName");
            EODatabaseDataSource ds = queryDatasource(entityName);
            if(hasBinding("queryDatasource")) {
                setValueForBinding(ds, "queryDatasource");
                return (WOComponent) performParentAction("searchAction");
            }
            if(listConfigurationName != null) {
                lpi = (ListPageInterface) D2W.factory().pageForConfigurationNamed(listConfigurationName, session());
            } else {
                lpi = D2W.factory().listPageForEntityNamed(entityName, session());
            }
            lpi.setDataSource(ds);
            lpi.setNextPage(context().page());
        }
        return (WOComponent)lpi;
    }

    private EODatabaseDataSource queryDatasource(String entityName) {
        EOEditingContext ec = session().defaultEditingContext();
        EODatabaseDataSource ds = new EODatabaseDataSource(ec, entityName);
        NSArray searchKeys = (NSArray) valueForBinding("searchKeys");
        if(searchKeys == null) {
            searchKeys = ERXEOControlUtilities.stringAttributeListForEntityNamed(ec, entityName);
        }
        EOQualifier qualifier = ERXEOControlUtilities.qualifierMatchingAnyKey(searchKeys, EOQualifier.QualifierOperatorCaseInsensitiveLike, "*" + searchValue + "*");
        EOQualifier restrictingQualifier = (EOQualifier) valueForBinding("extraRestrictingQualifier");
        if(restrictingQualifier != null) {
            qualifier = new EOAndQualifier(new NSArray(new Object[]{qualifier, restrictingQualifier}));
        }
        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
        ds.setFetchSpecification(fs);
        return ds;
    }

    public boolean disabled() {
    	return valueForBinding("entityName") == null;
    }
    
    public String defaultValue() {
        return (String) valueForBinding("defaultValue");
    }
}
