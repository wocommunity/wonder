package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.extensions.components.ERXSortOrder;
import er.extensions.localization.ERXLocalizer;

public class R2DSortOrder extends ERXSortOrder {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DSortOrder(WOContext context) {
        super(context);
    }

    public String helpString() {
    	D2WContext c = (D2WContext)valueForBinding("d2wContext");
    	ERXLocalizer loc = ERXLocalizer.currentLocalizer();
    	int state = currentState();
    	String helpString = (state == SortedAscending)?
    			loc.localizedTemplateStringForKeyWithObject("R2DSortOrder.sortDesc", c):
    			loc.localizedTemplateStringForKeyWithObject("R2DSortOrder.sortAsc", c);
    	return helpString;
    }
    
	public String sortClass() {
		StringBuilder sb = new StringBuilder("sort");
		switch(currentState()) {
		case Unsorted:
			sb.append(" uns");
			break;
		case SortedAscending:
			sb.append(" asc");
			break;
		case SortedDescending:
			sb.append(" dsc");
			break;
		default:
		}
		return sb.toString();
	}
    
}