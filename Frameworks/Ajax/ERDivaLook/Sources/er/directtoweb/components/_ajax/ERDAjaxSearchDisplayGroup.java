package er.directtoweb.components._ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.components.ERDCustomQueryComponent;

/**
 * QuickSerch or 'filter' feature
 * 
 * @author mendis
 *
 */
public class ERDAjaxSearchDisplayGroup extends ERDCustomQueryComponent {
	public String value;
	
    public ERDAjaxSearchDisplayGroup(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    public static final NSSelector selector = EOQualifier.QualifierOperatorCaseInsensitiveLike;		// FIXME: turn into property    
    
    // accessors    
    public String searchKey() {
    	String searchKey = (String) d2wContext().valueForKey("searchKey");
    	EODataSource dataSource = displayGroup().dataSource();
    	
    	if (searchKey == null && dataSource instanceof EODatabaseDataSource) {
    		searchKey = (String) ((EODatabaseDataSource) dataSource).entity().classPropertyNames().objectAtIndex(0);
    	}
    	return searchKey;
    }
    
    public String displayNameForSearchKey() {
    	String displayNameForSearchKey = (String) d2wContext().valueForKey("displayNameForSearchKey");
    	return (displayNameForSearchKey != null && !displayNameForSearchKey.equals("")) ? displayNameForSearchKey : searchKey();
    }
    
    // actions
    public WOActionResults search() {
    	EODataSource dataSource = displayGroup().dataSource();
    	
    	if (value != null && dataSource instanceof EODatabaseDataSource) {
    		EOQualifier _qualifier = new EOKeyValueQualifier(searchKey(), selector, "*" + value + "*");
    		((EODatabaseDataSource) dataSource).setAuxiliaryQualifier(_qualifier);
    	} else ((EODatabaseDataSource) dataSource).setAuxiliaryQualifier(null);
    	
		displayGroup().fetch();
		
    	return null;
    }
}