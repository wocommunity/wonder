package er.directtoweb.components.strings;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXKeyValuePair;
import er.extensions.localization.ERXLocalizer;

/**
 * @d2wKey possibleChoices
 */
public class ERD2WQueryStringWithChoices extends ERD2WQueryStringOperator {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERDEditStringWithChoices.class);
    public ERXKeyValuePair currentChoice;
    public NSArray _choices;
    
    public ERD2WQueryStringWithChoices(WOContext context) {
        super(context);
    }
    
    public NSArray choices(){
        if(_choices==null){
            Object choices = d2wContext().valueForKey("possibleChoices");
            if(choices != null) {
                NSMutableArray keyChoices = new NSMutableArray();
                if(choices instanceof NSArray) {
                    for(Enumeration e = ((NSArray)choices).objectEnumerator(); e.hasMoreElements(); ) {
                        NSDictionary dict = (NSDictionary)e.nextElement();
                        String key = (String)dict.allKeys().lastObject();
                        String value = (String)dict.objectForKey(key);
                        keyChoices.addObject(new ERXKeyValuePair(key, ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(value)));
                    }
                } else if(choices instanceof NSDictionary) {
                    NSArray keys = ((NSDictionary)choices).allKeys();
                    keys = ERXArrayUtilities.sortedArraySortedWithKey(keys, "toString");
                    for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                        String key = (String)e.nextElement();
                        String value = (String)((NSDictionary)choices).objectForKey(key);
                        keyChoices.addObject(new ERXKeyValuePair(key, ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(value)));
                    }
                }
                _choices = keyChoices;
            }
            if(log.isDebugEnabled()) log.debug("availableElements = "+_choices);
        }
        return _choices;
    }
    
    @Override
    public void reset(){
        super.reset();
        _choices = null;
        currentChoice = null;
    }
        
    public ERXKeyValuePair selectedChoice() {
        String value = (String)value();
        if(value == null) {
        	return null;
        }
        String choice = (String) ERXLocalizer.currentLocalizer().valueForKey(value);
        if(choice == null) {
            choice = value;
        }
        return new ERXKeyValuePair(value, choice);        
    }
    
    public void setSelectedChoice(ERXKeyValuePair value) {
        setValue( value != null ? value.key() : null );
    }
    
}
