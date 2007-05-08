//
// ERXLocalizedString.java: Class file for WO Component 'ERXLocalizedString'
// Project ERExtensions
//
// Created by ak on Sun May 05 2002
//

package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
/**

Examples:
 1) value = "Localize me" -> the localized value of "Localize me"
 2) keyPath = "componentName" (note that the path must be a String) -> localized name of the parent component
 3) object = bug, an EO -> localized version of bug.userPresentableDescription (may or may not be useful)
 4) object = bug, keyPath = "state" ->  localized version of the bugs state
 5) templateString = "You have @assignedBugs.count@ Bug(s) assigned to you", object = session.user
  -> localized template is evaluated 
 
Bindings:
 
 @binding object the object to derive the value of, if not given and keyPath is set, parent() is assumed
 @binding keyPath the keyPath to get of the object which is to be localized
 @binding value string to localize
 @binding templateString the key to the template to evaluate with object and otherObject
 @binding otherObject second object to use with templateString
 */
public class ERXLocalizedString extends ERXStatelessComponent {

    public ERXLocalizedString(WOContext context) {
        super(context);
    }

    private String objectToString(Object value) {
        String string = null;
        if(value != null) {
            if(value instanceof String)
                string = (String)value;
            else if(value instanceof EOEnterpriseObject)
                string = ((EOEnterpriseObject)value).userPresentableDescription();
            else
                string = value.toString();
        }
        return string;
    }

    public Object object() {
        Object value;
        if(hasBinding("object"))
            value = valueForBinding("object");
        else
            value = parent();
        return value;
    }
    
    public String value() {
        ERXLocalizer localizer = ERXLocalizer.currentLocalizer();
        String stringToLocalize = null, localizedString = null;
        if(!hasBinding("templateString")) {
            if(hasBinding("object") || hasBinding("keyPath")) {
                Object value = object();
                if(hasBinding("keyPath"))
                    value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, (String)valueForBinding("keyPath"));
                stringToLocalize = objectToString(value);
            } else if(hasBinding("value")) {
            	stringToLocalize = (String)valueForBinding("value");
            	if(booleanValueForBinding("omitWhenEmpty") && localizer.localizedStringForKey(stringToLocalize) == null) {
            		stringToLocalize = "";
            	}
            }
            if(stringToLocalize == null && hasBinding("valueWhenEmpty")) {
                stringToLocalize = (String)valueForBinding("valueWhenEmpty");
            }
            if(stringToLocalize != null) {
                localizedString = localizer.localizedStringForKeyWithDefault(stringToLocalize);
            }
        } else {
        	String templateString = (String)valueForBinding("templateString");
            Object otherObject = valueForBinding("otherObject");
        	localizedString = localizer.localizedTemplateStringForKeyWithObjectOtherObject(templateString, object(), otherObject);
        }
        return localizedString;
    }
}
