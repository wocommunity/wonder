package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.components.ERXSortOrder;
import er.extensions.foundation.ERXValueUtilities;

public class R2DSortImage extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DSortImage(WOContext context) {
        super(context);
    }
    
    public int sortState() {
    	return ERXValueUtilities.intValue(valueForBinding("sortState"));
    }

	public String imageID() {
		switch(sortState()) {
		case ERXSortOrder.SortedAscending:
			return "sort_asc";
		case ERXSortOrder.SortedDescending:
			return "sort_desc";
		default:
			return "sort_unsort";
		}
	}
}