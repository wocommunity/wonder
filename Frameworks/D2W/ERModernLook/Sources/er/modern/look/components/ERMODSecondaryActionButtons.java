package er.modern.look.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Re-usable block of secondary action buttons.
 * 
 * @author davidleber
 *
 */
public class ERMODSecondaryActionButtons extends ERMODComponent{
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public WODisplayGroup displayGroup;
	public EODataSource dataSource;
	public EOEnterpriseObject object;
	
	public ERMODSecondaryActionButtons(WOContext context) {
        super(context);
    }
	
}