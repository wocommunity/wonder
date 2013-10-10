package er.corebusinesslogic;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.components.ERXStatelessComponent;
import er.extensions.eof.ERXEC;

public class ERCListHelpText extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


	public String key;
	
    public ERCListHelpText(WOContext context) {
        super(context);
    }

    public boolean showList() {
        return booleanValueForBinding("showList", ERXApplication.isDevelopmentModeSafe());
    }
    
    public ERCHelpText text() {
    	return ERCHelpText.clazz.helpTextForKey(session().defaultEditingContext(), key);
    } 
    
    public WOComponent edit() {
       	EOEditingContext ec = ERXEC.newEditingContext();
       	ec.lock();
       	try {
       		ERCHelpText text = ERCHelpText.clazz.helpTextForKey(ec, key);
       		if(text == null) {
       			text = ERCHelpText.clazz.createAndInsertObject(ec);
       			text.setKey(key);
       		}
       		EditPageInterface page = D2W.factory().editPageForEntityNamed(ERCHelpText.ENTITY, session());
       		page.setObject(text);
       		page.setNextPage(context().page());
       		return (WOComponent)page;
       	} finally {
       		ec.unlock();
       	}
    }
    
    public NSArray keys() {
		return ERCDisplayHelpText.textsOnPage();
    }
}
