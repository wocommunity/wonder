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
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public NSDictionary<?,?> errorMessages;
	public String errorMessage;
	
    public ERMODErrorBlock(WOContext context) {
        super(context);
    }
    
	public boolean showErrorBlock() {
    	return (errorMessages != null && errorMessages.allKeys().count() > 0) || (errorMessage != null && errorMessage.length() > 0);
    }
    
}