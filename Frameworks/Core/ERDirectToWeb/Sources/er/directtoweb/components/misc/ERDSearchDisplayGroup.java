package er.directtoweb.components.misc;

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
 * For nesting inside list page nav bar
 * 
 * @author mendis
 * @d2wKey searchKey
 * @d2wKey displayNameForSearchKey
 */
public class ERDSearchDisplayGroup extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public String value;
	
    public ERDSearchDisplayGroup(WOContext context) {
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
		
    	return context().page();
    }
}