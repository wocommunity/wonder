package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayToManyTable;

/**
 * Display toMany relationship in <ul></ul>
 * 
 * @author mendis
 *
 */
public class ERD2WDisplayToManyUnorderedList extends D2WDisplayToManyTable {
	public ERD2WDisplayToManyUnorderedList(WOContext aContext) {
		super(aContext);
	}

    // accessors
    public boolean hasItems() {
    	return (list()!= null && list().count() > 0);
    }
}
