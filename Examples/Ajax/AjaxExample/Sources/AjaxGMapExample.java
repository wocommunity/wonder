

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class AjaxGMapExample extends WOComponent {

	public String _address = "3741 Westerre Parkway Suite A, Richmond VA, 23233";
	
    public AjaxGMapExample(WOContext context) {
        super(context);
    }
    
    public WOActionResults search() {
    	return null;
    }
    
}