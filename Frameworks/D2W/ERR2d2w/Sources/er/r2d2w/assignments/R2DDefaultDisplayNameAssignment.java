package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

public class R2DDefaultDisplayNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2DDefaultDisplayNameAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDefaultDisplayNameAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultDisplayNameAssignment(unarchiver);
	}

	public NSArray<Object> dependentKeys(String keyPath) {
		return new NSArray<Object>(new Object[] { D2WModel.PropertyKeyKey });
	}

    public Object displayNameForProperty(D2WContext c) {
    	String displayName = null;
    	
    	// Get the keys to localize
    	String propKey = c.propertyKey();
    	String propName = propKey.substring(0, propKey.length()-3);
    	String languageKey = propKey.substring(propKey.length()-2);
    	languageKey = languages().objectForKey(languageKey);    		
    	
    	// Localize the attribute name
    	ERXLocalizer loc = ERXLocalizer.currentLocalizer();
        displayName = loc.localizedDisplayNameForKey("PropertyKey", propName);

        // Localize the language name if necessary
        if(ERXValueUtilities.booleanValue(c.valueForKey("displayAllLocalesForAttribute"))) {
            NSMutableDictionary<String, String> dict = new NSMutableDictionary<String, String>();
        	dict.put("displayNameForProperty", displayName);
        	dict.put("language", loc.localizedStringForKey(languageKey));
        	displayName = loc.localizedTemplateStringForKeyWithObject("R2D2W.propertyWithLocale", dict);
        }
        
    	return displayName;
    }

	private static NSDictionary<String,String> langauges;
	
	private NSDictionary<String,String> languages() {
		if(langauges == null) {
			langauges = ERXDictionaryUtilities.dictionaryFromPropertyList("Languages", NSBundle.bundleForName("JavaWebObjects"));
		}
		return langauges;
	}

}
