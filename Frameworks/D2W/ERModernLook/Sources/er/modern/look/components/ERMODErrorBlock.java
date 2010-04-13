package er.modern.look.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

/**
 * Reusable component for the error message display
 * 
 * @author davidleber
 *
 */
public class ERMODErrorBlock extends ERMODComponent {
	
	public NSDictionary<?,?> errorMessages;
	public String errorMessage;
	
    public ERMODErrorBlock(WOContext context) {
        super(context);
    }
    
	public boolean showErrorBlock() {
    	return (errorMessages != null && errorMessages.allKeys().count() > 0) || (errorMessage != null && errorMessage.length() > 0);
    }
    
}