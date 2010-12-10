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
    
	public WODisplayGroup displayGroup;
	public EODataSource dataSource;
	public EOEnterpriseObject object;
	
	public ERMODSecondaryActionButtons(WOContext context) {
        super(context);
    }
	
}