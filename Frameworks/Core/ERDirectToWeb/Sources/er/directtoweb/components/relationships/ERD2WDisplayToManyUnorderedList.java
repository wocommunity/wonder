package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayToManyTable;

/**
 * Display toMany relationship in <ul></ul>
 * 
 * @author mendis
 * @d2wKey disabled
 */
public class ERD2WDisplayToManyUnorderedList extends D2WDisplayToManyTable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERD2WDisplayToManyUnorderedList(WOContext aContext) {
		super(aContext);
	}

    // accessors
    public boolean hasItems() {
    	return (list()!= null && list().count() > 0);
    }
}
