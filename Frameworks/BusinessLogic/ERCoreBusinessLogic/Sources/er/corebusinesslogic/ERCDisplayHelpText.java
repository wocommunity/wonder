package er.corebusinesslogic;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.components.ERDCustomComponent;
import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXThreadStorage;

public class ERCDisplayHelpText extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERCDisplayHelpText(WOContext context) {
        super(context);
    }

    @Override
    public boolean isStateless() {
     	return true;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
     	return false;
    }

    public ERCHelpText helpText() {
    	ERCHelpText text = ERCHelpText.clazz.helpTextForKey(session().defaultEditingContext(), key());
    	if(key() != null && !textsOnPage().containsObject(key())) {
    		textsOnPage().addObject(key());
    	}
    	return text;
    }

    public boolean showCreate() {
    	return showActions() && helpText() == null;
    }

    public boolean showEdit() {
    	return showActions() && helpText() != null;
    }
    
    public boolean showActions() {
    	return booleanValueForBinding("showActions", ERXApplication.isDevelopmentModeSafe());
    }
    
	private String prefix() {
		return (String) valueForBinding("prefix");
	}

	@Override
	public String key() {
		String prefix = prefix();
		String key = super.key();
		if(prefix != null) {
			return prefix + "." + key;
		}
		return key;
	}
	
	public WOComponent createHelpText() {
		EditPageInterface page = D2W.factory().editPageForNewObjectWithEntityNamed(ERCHelpText.ENTITY, session());
		((WOComponent) page).takeValueForKeyPath(key(), "object." + ERCHelpText.Key.KEY);
		((WOComponent) page).takeValueForKeyPath(defaultValue(), "object." + ERCHelpText.Key.VALUE);
		page.setNextPage(context().page());
		return (WOComponent) page;
	}
	
	public WOComponent editHelpText() {
		EditPageInterface page = D2W.factory().editPageForEntityNamed(ERCHelpText.ENTITY, session());
		EOEnterpriseObject eo = ERXEOControlUtilities.editableInstanceOfObject(helpText(), false);
		page.setObject(eo);
		page.setNextPage(context().page());
		return (WOComponent) page;
	}
	
	public String defaultValue() {
		String value = (String) valueForBinding("defaultValue");
		if(value == null) {
			value = "";
		}
		return value;
	}

	public static NSMutableArray textsOnPage() {
		String key = "ERCDisplayHelpText.textsOnPage";
		NSMutableArray textsOnPage = (NSMutableArray) ERXThreadStorage.valueForKey(key);
		if(textsOnPage == null) {
			textsOnPage = new NSMutableArray();
			ERXThreadStorage.takeValueForKey(textsOnPage, key);
		}
		return textsOnPage;
	}
}
