package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Better D2WQueryBoolean, which allows you to sprecify the choices names via a context key, 
 * containing the labels in a format like ("Don't care", "Yes", "No") or ("Yes", "No").
 * Also keeps the selected value. 
 * 
 * @created ak on Mon Dec 22 2003
 * @project ERDirectToWeb
 */

public class ERD2WCustomQueryBoolean extends D2WQueryBoolean {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERD2WCustomQueryBoolean.class);
    protected NSArray _choicesNames;
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WCustomQueryBoolean(WOContext context) {
        super(context);
    }

    public NSArray choicesNames() {
        if (_choicesNames == null)
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    public void reset(){
        super.reset();
        _choicesNames = null;
    }
    
    public Object value() {
    	int index = 0;
    	if(">".equals(displayGroup().queryOperator().valueForKey(propertyKey()))) {
    		index = 1;
    	} else {
    		index = displayGroup().queryMatch().valueForKey(propertyKey()) != null ? 2 : 0;
    	}
     	return queryNumbers.objectAtIndex(index);
    }

    public void setValue(Object obj) {
    	displayGroup().queryOperator().removeObjectForKey(propertyKey());
    	displayGroup().queryMatch().removeObjectForKey(propertyKey());
    	if(obj.equals(queryNumbers.objectAtIndex(0))) {
    		log.debug("Don't care");
    	} else {
    		displayGroup().queryMatch().takeValueForKey(ERXConstant.ZeroInteger, propertyKey());
    		if(obj.equals(queryNumbers.objectAtIndex(1))) {
    			displayGroup().queryOperator().takeValueForKey(">", propertyKey());
    			log.debug("True");
    		} else {
    			log.debug("False");
    		}
     	}
    }
    
    public String displayString() {
        NSArray choicesNames = choicesNames();
        String result;
        if(choicesNames == null) {
            result = super.displayString();
        }
        int choicesIndex = index == 0 ? 2 : index - 1;
        if(choicesIndex >= choicesNames.count()) {
            result = super.displayString();
        } else {
        	result = (String)choicesNames.objectAtIndex(choicesIndex);
        }
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(result);
    }
}
