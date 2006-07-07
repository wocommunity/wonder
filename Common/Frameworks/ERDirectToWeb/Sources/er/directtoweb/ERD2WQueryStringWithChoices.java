package er.directtoweb;

import java.util.*;

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.*;

public class ERD2WQueryStringWithChoices extends ERD2WQueryStringOperator {
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
    
    public void reset(){
        super.reset();
        _choices = null;
        currentChoice = null;
    }
        
    public ERXKeyValuePair selectedChoice() {
        return new ERXKeyValuePair(value(), ERXLocalizer.currentLocalizer()
                                   .localizedValueForKeyWithDefault((String)value()));        
    }
    
    public void setSelectedChoice(ERXKeyValuePair value) {
        setValue( value != null ? value.key() : null );
    }
    
}
