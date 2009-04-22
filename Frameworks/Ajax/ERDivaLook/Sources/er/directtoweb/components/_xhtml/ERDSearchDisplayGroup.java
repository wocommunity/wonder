package er.directtoweb.components._xhtml;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.components.ERDCustomQueryComponent;

public class ERDSearchDisplayGroup extends ERDCustomQueryComponent {
	public String value;
	
    public ERDSearchDisplayGroup(WOContext context) {
        super(context);
    }
    
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    public static final NSSelector selector = EOQualifier.QualifierOperatorCaseInsensitiveLike;		// FIXME: turn into property    
    
    // accessors    
    public String searchKey() {
    	String searchKey = (String) d2wContext().valueForKey("searchKey");
    	return (searchKey != null) ? searchKey : (String) dataSource().entity().classPropertyNames().objectAtIndex(0);
    }
    
    private EODatabaseDataSource dataSource() {
		return (EODatabaseDataSource) displayGroup().dataSource();
    }
    
    public String displayNameForSearchKey() {
    	String displayNameForSearchKey = (String) d2wContext().valueForKey("displayNameForSearchKey");
    	return (displayNameForSearchKey != null && !displayNameForSearchKey.equals("")) ? displayNameForSearchKey : searchKey();
    }
    
    // actions
    public WOActionResults search() {
		EOFetchSpecification fetchSpec = dataSource().fetchSpecification();
		
    	if (value != null) {
    		EOQualifier _qualifier = new EOKeyValueQualifier(searchKey(), selector, "*" + value + "*");
    		dataSource().setAuxiliaryQualifier(_qualifier);
    	} else dataSource().setAuxiliaryQualifier(null);
    	
		displayGroup().fetch();
		
    	return null;
    }
}