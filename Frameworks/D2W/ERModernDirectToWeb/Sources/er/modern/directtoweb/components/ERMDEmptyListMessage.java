package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.misc.ERDEmptyListMessage;

/**
 * Modern empty list message component
 * 
 * @d2wKey emptyListMessage
 * @d2wKey showCreateObjectLink 
 * 
 * @author davidleber
 *
 */
public class ERMDEmptyListMessage extends ERDEmptyListMessage {
	
    public ERMDEmptyListMessage(WOContext context) {
        super(context);
    }
    
}